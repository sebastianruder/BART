/*
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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

/**
 *
 * @author yannick
 */
public class LoadedSparseMatrix implements SparseMatrix
{
    private final static int BUF_SIZE=4096;
    private FileChannel _f_rows;
    private FileChannel _f_values;
    private FileChannel _f_offsets;
    private ByteBuffer _buf_row;
    private ByteBuffer _buf_val;
    private ByteBuffer _buf_off;
    private IntBuffer _ibuf_row;
    private DoubleBuffer _dbuf_val;
    private LongBuffer _lbuf_off;
    private long cur_pos;

    int n_vectors;
    private ArrayList<SparseVector> vectors;

    /** creates a sparse matrix that is linked to
     * the files pointed to by <i>prefix</i>
     * @param prefix the file prefix used.
     * @param map_max maximal number of bytes to
     *   use when mapping files.
     */
    public LoadedSparseMatrix(String prefix)
            throws FileNotFoundException, IOException
    {
        _f_rows = new FileInputStream(prefix + "_row.bin").getChannel();
        _f_values = new FileInputStream(prefix + "_val.bin").getChannel();
        _f_offsets = new FileInputStream(prefix + "_off.bin").getChannel();
        n_vectors=(int)(_f_offsets.size()/8);
        _buf_off=_f_offsets.map(FileChannel.MapMode.READ_ONLY, 0,
                _f_offsets.size());
        _lbuf_off=_buf_off.asLongBuffer();
        _buf_val=ByteBuffer.allocate(BUF_SIZE*8);
        _dbuf_val=_buf_val.asDoubleBuffer();
        int sz=_f_values.read(_buf_val);
        _dbuf_val.limit(sz/8);
        _buf_row=ByteBuffer.allocate(BUF_SIZE*4);
        _buf_row.order(ByteOrder.LITTLE_ENDIAN);
        _ibuf_row=_buf_row.asIntBuffer();
        sz=_f_rows.read(_buf_row);
        _ibuf_row.limit(sz/4);
        vectors=new ArrayList<SparseVector>(n_vectors);
        cur_pos=0;
    }

    private SparseVector materialize(int len) throws IOException {
        int[] keys=new int[len];
        double[] vals=new double[len];
        int k=0;
        int sz=_ibuf_row.remaining();
        while (k+sz<len) {
            _ibuf_row.get(keys, k, sz);
            _dbuf_val.get(vals, k, sz);
            k+=sz;
            _buf_row.clear();
            sz=_f_rows.read(_buf_row);
            _ibuf_row.position(0);
            _ibuf_row.limit(sz/4);
            _buf_val.clear();
            sz=_f_values.read(_buf_val);
            _dbuf_val.position(0);
            _dbuf_val.limit(sz/8);
            sz=_ibuf_row.remaining();
        }
        _ibuf_row.get(keys,k,len-k);
        _dbuf_val.get(vals,k,len-k);
        return new SparseVector(keys,vals);
    }
    
    public List<SparseVector> subList(int start, int end) 
    {
        if (end>n_vectors) {
            throw new IndexOutOfBoundsException();
        }
        while (vectors.size()<end) {
            int len=(int)(_lbuf_off.get(vectors.size()+1)-cur_pos);
            try {
                vectors.add(materialize(len));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            cur_pos+=len;
        }
        return vectors.subList(start, end);
    }

    public void getRange(int start, int end,
            ArrayList<SparseVector> lst) {
        List<SparseVector> lst2=subList(start,end);
        lst.addAll(lst2);
    }

    public SparseVector get(int start) {
        if (start >= n_vectors) {
            throw new IndexOutOfBoundsException();
        }
        while (vectors.size() <= start) {
            int len = (int) (_lbuf_off.get(vectors.size()+1) - cur_pos);
            try {
            vectors.add(materialize(len));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            cur_pos += len;
        }
        return vectors.get(start);
    }

    public static void main(String[] args) {
        try {
            LoadedSparseMatrix m = new LoadedSparseMatrix(args[0]);
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
