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
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public class MappedCompressedMatrix implements SparseMatrix {
    private static Logger _logger=Logger.getAnonymousLogger();
    final long _map_max;
    long n_items;
    long n_cells;
    int mvector_start;
    int mvector_end;
    long mapped_start;
    long mapped_end;
    long mappedC_start;
    long mappedC_end;
    int generation;
    private ByteBuffer _buf_row;
    private ByteBuffer _buf_val;
    private ByteBuffer _buf_off;
    private ByteBuffer _buf_offC;
    private IntBuffer _ibuf_row;
    private LongBuffer _lbuf_off;
    private LongBuffer _lbuf_offC;
    private FileChannel _f_rows;
    private FileChannel _f_values;
    private FileChannel _f_offsets;
    private FileChannel _f_offsetsC;
    private final double[] most_frequent;

    /** creates a sparse matrix that is linked to
     * the files pointed to by <i>prefix</i>
     * @param prefix the file prefix used.
     * @param map_max maximal number of bytes to
     *   use when mapping files.
     */
    public MappedCompressedMatrix(String prefix, long mem_max) throws FileNotFoundException, IOException {
        long proposed=mem_max;
        // limit mapped portions to 1GB, so that address space
        // does not get clogged up
        if (proposed>Integer.MAX_VALUE/2) {
            proposed=Integer.MAX_VALUE/2;
        }
        _map_max=proposed;

        _f_rows = new FileInputStream(prefix + "_row.bin").getChannel();
        _f_values = new FileInputStream(prefix + "_valC.bin").getChannel();
        _f_offsets = new FileInputStream(prefix + "_off.bin").getChannel();
        _f_offsetsC = new FileInputStream(prefix + "_offC.bin").getChannel();
        _buf_off=_f_offsets.map(FileChannel.MapMode.READ_ONLY, 0,
                _f_offsets.size());
        _lbuf_off=_buf_off.asLongBuffer();
        _buf_offC=_f_offsetsC.map(FileChannel.MapMode.READ_ONLY, 0,
                _f_offsetsC.size());
        _lbuf_offC=_buf_offC.asLongBuffer();

        n_items=_f_offsets.size()/8;
        n_cells=_lbuf_off.get((int)n_items-1);
        if (n_cells!=_f_rows.size()/4) {
            _logger.warning(String.format("disagreeing size: n_cells=%d len(rows)=%d",
                    n_cells,_f_rows.size()/4));
        }
        mapped_start=mapped_end=0;
        generation=0;
        ObjectInputStream in=new ObjectInputStream(
                new FileInputStream(prefix+"_dictC.obj"));
        try {
            Alphabet<Double> alph=(Alphabet<Double>)in.readObject();
            most_frequent=new double[alph.size()];
            for (int i=0; i<alph.size(); i++) {
                most_frequent[i]=alph.lookupObject(i);
            }
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("cannot read alphabet",ex);
        } finally {
            in.close();
        }
    }

    public MappedCompressedMatrix(String prefix) throws FileNotFoundException, IOException {
        /* use 50MB buffer by default */
        this(prefix, 50<<20);
    }

    private void ensureMapped(int start_v, int end_v) {
        int new_end_v;
        if (mvector_start<=start_v &&
                mvector_end>=end_v) {
            return;
        }
        long usage=(_lbuf_off.get(end_v)-_lbuf_off.get(start_v))*8
                +(_lbuf_offC.get(end_v)-_lbuf_offC.get(start_v));
        if (usage>_map_max) {
            _logger.warning("ensureMapped: slice ("+usage+") is larger than "+_map_max);
            new_end_v=end_v;
        } else {
            System.err.format("%d-%d usage=%d desired=%d\n",start_v, end_v, usage,_map_max);
            new_end_v=mvector_end+
                    (int)((usage-_map_max)*(end_v-start_v)/usage);
            long new_usage=(_lbuf_off.get(new_end_v)-_lbuf_off.get(start_v))*8
                +(_lbuf_offC.get(new_end_v)-_lbuf_offC.get(start_v));
            while (new_usage>_map_max) {
                new_end_v=(end_v+new_end_v)/2;
                new_usage=(_lbuf_off.get(new_end_v)-_lbuf_off.get(start_v))*8
                +(_lbuf_offC.get(new_end_v)-_lbuf_offC.get(start_v));
            }
        }
        try {
            System.err.format("remap: start_v=%d end_v=%d new_end_v=%d\n",
                    start_v,end_v,new_end_v);
            mvector_start=start_v;
            mvector_end=new_end_v;
            long start=_lbuf_off.get(start_v);
            long new_end=_lbuf_off.get(new_end_v);
            // null all the buffers so they can be deallocated
            // before the address space for the new ones gets requested
            _buf_row=_buf_val=null;
            _ibuf_row=null;
            _buf_row=_f_rows.map(FileChannel.MapMode.READ_ONLY, start*4,
                    (int)(new_end-start)*4);
            _buf_row.order(ByteOrder.LITTLE_ENDIAN);
            _ibuf_row=_buf_row.asIntBuffer();
            mappedC_start=_lbuf_offC.get(mvector_start);
            mappedC_end=_lbuf_offC.get(new_end_v);
            mapped_start=_lbuf_off.get(mvector_start);
            mapped_end=_lbuf_off.get(mvector_end);
            _buf_val=_f_values.map(FileChannel.MapMode.READ_ONLY, mappedC_start,
                    (int)(mappedC_end-mappedC_start));
            _buf_val.order(ByteOrder.LITTLE_ENDIAN);
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
        ensureMapped(start,end);
        int local_start=(int)(_lbuf_off.get(start) -
                mapped_start);
        int local_startC=(int)(_lbuf_offC.get(start) -
                mappedC_start);
        _ibuf_row.position(local_start);
        _buf_val.position(local_startC);
        int local_end=local_start;
        for (int k=start; k<end; k++) {
            local_start=local_end;
            local_end=(int)(_lbuf_off.get(k+1)-mapped_start);
            int[] keys=new int[local_end-local_start];
            _ibuf_row.get(keys);
            double[] vals=new double[local_end-local_start];
            for (int i=0; i<vals.length; i++) {
                /* put contents of getDoubleVal() here
                 * because the JIT compiler doesn't seem
                 * to inline it.
                 * actually meant: vals[i]=getDoubleVal();
                 */
                byte b = _buf_val.get();
                if (b < 0) {
                    // MSB set -> this is a mutilated float
                    short b01=_buf_val.getShort();
                    byte b2=_buf_val.get();
                    int fbits=(b01<<16)|
                            ((b2&0xff)<<8)|
                            ((b&0x7f)<<1);
                    vals[i] = Float.intBitsToFloat(fbits);
                } else {
                    // MSB not set -> this is a table entry
                    vals[i] = most_frequent[b];
                }
            }
            lst.add(new SparseVector(keys,vals));
        }
    }

    public SparseVector get(int start) {
        ensureMapped(start, start + 1);
        int local_start = (int) (_lbuf_off.get(start) -
                mapped_start);
        int local_end = (int) (_lbuf_off.get(start + 1) -
                mapped_start);
        int local_startC = (int) (_lbuf_offC.get(start) -
                mappedC_start);
        _ibuf_row.position(local_start);
        _buf_val.position(local_startC);
        int[] keys = new int[local_end - local_start];
        _ibuf_row.get(keys);
        double[] vals = new double[local_end - local_start];
        for (int i = 0; i < vals.length; i++) {
            /* put contents of getDoubleVal() here
             * because the JIT compiler doesn't seem
             * to inline it.
             * actually meant: vals[i]=getDoubleVal();
             */
            byte b = _buf_val.get();
            if (b < 0) {
                // MSB set -> this is a mutilated float
                short b01 = _buf_val.getShort();
                byte b2 = _buf_val.get();
                int fbits = (b01 << 16) |
                        ((b2 & 0xff) << 8) |
                        ((b & 0x7f) << 1);
                vals[i] = Float.intBitsToFloat(fbits);
            } else {
                // MSB not set -> this is a table entry
                vals[i] = most_frequent[b];
            }
        }
        return new SparseVector(keys, vals);
    }

    public int size() {
        return (int)n_items;
    }

    public boolean isEmpty() {
        return n_items==0;
    }

    public static void main(String[] args) {
       try {
           String arg;
           if (args.length >= 1) arg=args[0];
           else arg="test";
           MappedCompressedMatrix m = new MappedCompressedMatrix(arg, 800000);
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
