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

import gnu.trove.list.array.TIntArrayList;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * writes sparse vectors to disk so they can be used
 * as a CSR array.
 * 
 * @author yannickv
 */
public class SparseMatrixWriter {
    // 4k elements => 16k/32k byte buffers

    final int BUF_SIZE = 4096;
    long n_written = 0;
    final String _prefix;
    private ByteBuffer _buf_row;
    private ByteBuffer _buf_val;
    private ByteBuffer _buf_off;
    private IntBuffer _ibuf_row;
    private DoubleBuffer _dbuf_val;
    private LongBuffer _lbuf_off;
    private FileChannel _f_rows;
    private FileChannel _f_values;
    private FileChannel _f_offsets;

    public SparseMatrixWriter(String prefix)
            throws FileNotFoundException, IOException {
        _prefix=prefix;
        _f_rows = new FileOutputStream(prefix + "_row.bin").getChannel();
        _f_values = new FileOutputStream(prefix + "_val.bin").getChannel();
        _f_offsets = new FileOutputStream(prefix + "_off.bin").getChannel();
        // allocate space for 2k values in the buffer
        _buf_row = ByteBuffer.allocate(BUF_SIZE * 4);
        _buf_row.order(ByteOrder.LITTLE_ENDIAN);
        _ibuf_row = _buf_row.asIntBuffer();
        _buf_val = ByteBuffer.allocate(BUF_SIZE * 8);
        _dbuf_val = _buf_val.asDoubleBuffer();
        _buf_off = ByteBuffer.allocate(BUF_SIZE * 8);
        _lbuf_off = _buf_off.asLongBuffer();
        _lbuf_off.put(0);
        flush_off_buf();
    }

    public void write(SparseVector vec) throws IOException {
        int size = vec._keys.length;
        if (_dbuf_val.remaining() >= size) {
            _dbuf_val.put(vec._vals);
            _ibuf_row.put(vec._keys);
        } else {
            int k = 0;
            flush_bufs();
            while (size - k >= BUF_SIZE) {
                _dbuf_val.put(vec._vals, k, BUF_SIZE);
                _ibuf_row.put(vec._keys, k, BUF_SIZE);
                flush_bufs();
                k += BUF_SIZE;
            }
            _dbuf_val.put(vec._vals, k, size - k);
            _ibuf_row.put(vec._keys, k, size - k);
        }
        n_written += size;
        if (!_lbuf_off.hasRemaining()) {
            flush_off_buf();
        }
        _lbuf_off.put(n_written);
    }

    public void close() throws IOException {
        flush_all_bufs();
        _f_values.close();
        _f_rows.close();
        _f_offsets.close();
    }

    private void flush_bufs() throws IOException {
        //System.out.println("flush; val limit="+_dbuf_val.limit()+
        //        " val position="+_dbuf_val.position());
        _buf_val.rewind();
        _buf_val.limit(_dbuf_val.position() * 8);
        _f_values.write(_buf_val);
        _dbuf_val.rewind();
        _buf_row.rewind();
        _buf_row.limit(_ibuf_row.position() * 4);
        _f_rows.write(_buf_row);
        _ibuf_row.rewind();
    }

    private void flush_all_bufs() throws IOException {
        flush_bufs();
        flush_off_buf();
    }

    private void flush_off_buf() throws IOException {
        _buf_off.rewind();
        _buf_off.limit(_lbuf_off.position() * 8);
        _f_offsets.write(_buf_off);
        _lbuf_off.clear();
        System.err.println(_prefix+": wrote "+n_written+" cells");
    }

    public static void convert_obj(String[] args) {
        ObjectInputStream is = null;
        SparseMatrixWriter w = null;
        try {
            String prefix = args[0];
            int n_written=0;
            TIntArrayList vectors = new TIntArrayList();
            vectors.add(n_written);
            is = new ObjectInputStream(new FileInputStream(prefix + ".obj"));
            w = new SparseMatrixWriter(prefix);
            while (true) {
                try {
                    List<SparseVector> ls;
                    ls = (List<SparseVector>) is.readObject();
                    for (SparseVector v: ls) {
                        w.write(v);
                    }
                    n_written+=ls.size();
                    vectors.add(n_written);
                    ls = (List<SparseVector>) is.readObject();
                    for (SparseVector v: ls) {
                        w.write(v);
                    }
                    n_written+=ls.size();
                    vectors.add(n_written);
                } catch (EOFException e) {
                    break;
                }
            }
            is.close();
            w.close();
            ObjectOutputStream v_out=new ObjectOutputStream(
                    new FileOutputStream(prefix+"_ex_pairs.bin"));
            v_out.writeObject(vectors);
            v_out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
       try {
           SparseMatrixWriter w = new SparseMatrixWriter(args[0]);
           int k=0;
           for (int i=0; i<10000; i++) {
               int[] keys=new int[100+i];
               double[] vals=new double[100+i];
               for (int j=0; j<100+i; j++) {
                    keys[j]=k;
                    vals[j]=(k%4<2?k%3:100.0/((k%20)+j));
                    k+=1;
               }
               w.write(new SparseVector(keys,vals));
           }
           w.close();
       } catch (Exception ex) {
           ex.printStackTrace();
           System.exit(1);
       }
    }
}
