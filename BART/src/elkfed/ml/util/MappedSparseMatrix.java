/*
 * Copyright 2010 Yannick Versley / Univ. Tuebingen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elkfed.ml.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * realizes a sparse matrix through
 * memory-mapped buffers.
 * @author yannickv
 */
public class MappedSparseMatrix implements SparseMatrix {
    private static Logger _logger=Logger.getAnonymousLogger();
    final long _map_max;
    long n_items;
    long n_cells;
    long mapped_start;
    long mapped_end;
    int generation;
    private ByteBuffer _buf_row;
    private ByteBuffer _buf_val;
    private ByteBuffer _buf_off;
    private IntBuffer _ibuf_row;
    private DoubleBuffer _dbuf_val;
    private LongBuffer _lbuf_off;
    private FileChannel _f_rows;
    private FileChannel _f_values;
    private FileChannel _f_offsets;

    /** creates a sparse matrix that is linked to
     * the files pointed to by <i>prefix</i>
     * @param prefix the file prefix used.
     * @param map_max maximal number of bytes to
     *   use when mapping files.
     */
    public MappedSparseMatrix(String prefix, long mem_max) throws FileNotFoundException, IOException {
        long proposed=mem_max/12;
        if (proposed*8>Integer.MAX_VALUE) {
            proposed=Integer.MAX_VALUE/8;
        }
        _map_max=proposed;

        _f_rows = new FileInputStream(prefix + "_row.bin").getChannel();
        _f_values = new FileInputStream(prefix + "_val.bin").getChannel();
        _f_offsets = new FileInputStream(prefix + "_off.bin").getChannel();
        _buf_off=_f_offsets.map(FileChannel.MapMode.READ_ONLY, 0,
                _f_offsets.size());
        _lbuf_off=_buf_off.asLongBuffer();
        n_items=_f_offsets.size()/8;
        n_cells=_lbuf_off.get((int)n_items-1);
        if (n_cells!=_f_rows.size()/4) {
            _logger.warning(String.format("disagreeing size: n_cells=%d len(rows)=%d",
                    n_cells,_f_rows.size()/4));
        }
        if (n_cells!=_f_values.size()/8) {
            _logger.warning(String.format("disagreeing size: n_cells=%d len(vals)=%d",
                    n_cells,_f_values.size()/8));
        }
        mapped_start=mapped_end=0;
        generation=0;
    }

    public MappedSparseMatrix(String prefix) throws FileNotFoundException, IOException {
        /* use 50MB buffer by default */
        this(prefix, 50<<20);
    }

    private void ensureMapped(long start, long end) {
        if (mapped_start<=start &&
                mapped_end>=end) {
            return;
        }
        long new_end=start+_map_max;
        if (new_end<end) {
            _logger.warning("ensureMapped: slice is larger than "+_map_max);
            new_end=end;
        }
        if (new_end>n_cells) {
            new_end=n_cells;
        }
        try {
            System.err.format("remap: start=%d end=%d new_end=%d\n",start,end,new_end);
            // null all the buffers so they can be deallocated
            // before the address space for the new ones gets requested
            _buf_row=_buf_val=null;
            _ibuf_row=null; _dbuf_val=null;
            _buf_row=_f_rows.map(FileChannel.MapMode.READ_ONLY, start*4,
                    (int)(new_end-start)*4);
            _buf_row.order(ByteOrder.LITTLE_ENDIAN);
            _ibuf_row=_buf_row.asIntBuffer();
            _buf_val=_f_values.map(FileChannel.MapMode.READ_ONLY, start*8,
                    (int)(new_end-start)*8);
            _dbuf_val=_buf_val.asDoubleBuffer();
            mapped_start=start;
            mapped_end=new_end;
            generation++;
        } catch (IOException e) {
            throw new RuntimeException("cannot mmap buffer",e);
        }
    }

    public List<SparseVector> subList(int start, int end) {
        ArrayList<SparseVector> lst=new ArrayList<SparseVector>();
        getRange(start,end,lst);
        return lst;
    }

    public void getRange(int start, int end,
            ArrayList<SparseVector> lst) {
        long cell_first=_lbuf_off.get(start);
        long cell_last=_lbuf_off.get(end);
        ensureMapped(cell_first,cell_last);
        int local_end=(int)(cell_first-mapped_start);
        _ibuf_row.position(local_end);
        _dbuf_val.position(local_end);
        for (int k=start; k<end; k++) {
            int local_start=local_end;
            local_end=(int)(_lbuf_off.get(k+1)-mapped_start);
            int[] keys=new int[local_end-local_start];
            _ibuf_row.get(keys);
            double[] vals=new double[local_end-local_start];
            _dbuf_val.get(vals);
            lst.add(new SparseVector(keys,vals));
        }
    }

    public void getRangeLazy(int start, int end,
            ArrayList<? super LazyVector> lst) {
        long cell_first=_lbuf_off.get(start);
        long cell_last=_lbuf_off.get(end);
        ensureMapped(cell_first, cell_last);
        for (int k=start; k<end; k++) {
            lst.add(new LazyVector(k));
        }
    }

    public int size() {
        return (int)n_items;
    }

    public boolean isEmpty() {
        return n_items==0;
    }

    public SparseVector get(int start) {
        long cell_first=_lbuf_off.get(start);
        long cell_last=_lbuf_off.get(start+1);
        ensureMapped(cell_first,cell_last);
        int local_start=(int)(cell_first-mapped_start);
        _ibuf_row.position(local_start);
        _dbuf_val.position(local_start);
        int len=(int)(cell_last-cell_first);
        int[] keys=new int[len];
        _ibuf_row.get(keys);
        double[] vals=new double[len];
        _dbuf_val.get(vals);
        return new SparseVector(keys,vals);
    }

    public LazyVector getLazy(int start) {
        long cell_first=_lbuf_off.get(start);
        long cell_last=_lbuf_off.get(start+1);
        ensureMapped(cell_first,cell_last);
        return new LazyVector(start);
    }

    public class LazyVector implements IVector {
        final int local_offset;
        final int local_end;
        final int my_generation;

        LazyVector(int pos) {
            my_generation=generation;
            long start=_lbuf_off.get(pos);
            long end=_lbuf_off.get(pos+1);
            assert start>=mapped_start;
            assert end<=mapped_end;
            local_offset=(int)(start-mapped_start);
            local_end=(int)(end-mapped_start);
        }

        public void addTo(double[] vec, double factor) {
            assert my_generation==generation;
            for (int i=local_offset;i<local_end;i++) {
                vec[_ibuf_row.get(i)]+=factor*_dbuf_val.get(i);
            }
        }

        public double dotProduct(double[] vec) {
            assert my_generation==generation;
            double result=0;
            for (int i=local_offset;i<local_end;i++) {
                result+=vec[_ibuf_row.get(i)]*_dbuf_val.get(i);
            }
            return result;
        }

        public void put(KVFunc func) {
            assert my_generation==generation;
            for (int i=local_offset;i<local_end;i++) {
                func.put(_ibuf_row.get(i),_dbuf_val.get(i));
            }
        }
    }

    public static void main(String[] args) {
       try {
           MappedSparseMatrix m = new MappedSparseMatrix(args[0], 800000);
           for (int round = 0; round < 3; round++) {
               int k = 0;
               ArrayList<SparseVector> xs = new ArrayList<SparseVector>();

               for (int i = 0; i < 10000; i++) {
                   if (i % 1000 == 0) {
                       xs.clear();
                       m.getRange(i, i + 1000, xs);
                   }
                   SparseVector x1 = xs.get(i % 1000);
                   SparseVector x2 = m.get(i);
                   if (x1._keys.length != 100 + i) {
                       System.err.format("[%d] len(x1)=%d should be: %d\n",
                               i, x1._keys.length, 100 + i);
                   }
                   if (x2._keys.length != 100 + i) {
                       System.err.format("[%d] len(x2)=%d should be: %d\n",
                               i, x2._keys.length, 100 + i);
                   }
                   for (int j = 0; j < 100 + i; j++) {
                       if (x1._keys[j] != k) {
                           System.err.format("[%d] x1.keys[%d]=%d should be: %d\n",
                                   i, j, x1._keys[j], k);
                       }
                       if (x2._keys[j] != k) {
                           System.err.format("[%d] x2.keys[%d]=%d should be: %d\n",
                                   i, j, x2._keys[j], k);
                       }
                       double wanted_val = (k % 4 < 2 ? k % 3 : 100.0 / ((k % 20) + j));
                       if (Math.abs(x1._vals[j] - wanted_val) > 1e-5) {
                           System.err.format("[%d] x1.vals[%d]=%f should be: %f\n",
                                   i, j, x1._vals[j], wanted_val);
                       }
                       if (Math.abs(x2._vals[j] - wanted_val) > 1e-5) {
                           System.err.format("[%d] x2.vals[%d]=%f should be: %f\n",
                                   i, j, x2._vals[j], wanted_val);
                       }
                       k += 1;
                   }
               }
           }
       } catch (Exception ex) {
           ex.printStackTrace();
           System.exit(1);
       }
    }
}
