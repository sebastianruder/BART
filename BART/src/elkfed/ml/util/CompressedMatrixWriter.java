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

import gnu.trove.map.hash.TDoubleIntHashMap;
import gnu.trove.procedure.TDoubleIntProcedure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * transforms a matrix file written by SparseMatrixWriter
 * by compressing the values column
 * 
 * @author yannickv
 */
public class CompressedMatrixWriter {
    // allocate space for 4k values in the buffers
    final int BUF_SIZE = 4096;
    long n_read=0;
    long n_written = 0;
    final String _prefix;
    private Alphabet<Double> most_frequent;
    private ByteBuffer _buf_val_in;
    private ByteBuffer _buf_val_out;
    private ByteBuffer _buf_off_in;
    private ByteBuffer _buf_off_out;
    private DoubleBuffer _dbuf_val_in;
    private LongBuffer _lbuf_off_in;
    private FileChannel _f_values_in;
    private FileChannel _f_values_out;
    private FileChannel _f_offsets;
    private FileChannel _f_offsets_out;

    public CompressedMatrixWriter(String prefix)
            throws FileNotFoundException, IOException {
        _prefix = prefix;
        _f_values_in = new FileInputStream(prefix + "_val.bin").getChannel();
        _f_offsets = new FileInputStream(prefix + "_off.bin").getChannel();
        
        _buf_val_in = ByteBuffer.allocate(BUF_SIZE * 8);
        _dbuf_val_in = _buf_val_in.asDoubleBuffer();
        _buf_val_out = ByteBuffer.allocate(BUF_SIZE * 4);
        _buf_val_out.order(ByteOrder.LITTLE_ENDIAN);
        _buf_off_in = ByteBuffer.allocate(BUF_SIZE * 8);
        _buf_off_out = ByteBuffer.allocate(BUF_SIZE * 8);
        _lbuf_off_in = _buf_off_in.asLongBuffer();
    }
    public static final Comparator<Map.Entry<Integer, Double>> cmp =
            new Comparator<Map.Entry<Integer, Double>>() {

                public int compare(Entry<Integer, Double> arg0, Entry<Integer, Double> arg1) {
                    int k0 = arg0.getKey();
                    int k1 = arg1.getKey();
                    if (k0 > k1) {
                        return -1;
                    } else if (k0 < k1) {
                        return 1;
                    } else {
                        double val0 = arg0.getValue();
                        double val1 = arg1.getValue();
                        return Double.compare(val0, val1);
                    }
                }
            };

    public void find_most_frequent() throws IOException {
        final TDoubleIntHashMap counts = new TDoubleIntHashMap();
        System.err.println("Looking for most frequent items");
        _f_values_in.position(0);
        _buf_val_in.position(0);
        int b_read = _f_values_in.read(_buf_val_in);
        _dbuf_val_in.position(0);
        _dbuf_val_in.limit(b_read / 8);
        // read the first 50MB or so
        for (int i = 0; i < (50 << 20); i++) {
            if (!_dbuf_val_in.hasRemaining()) {
                _buf_val_in.position(0);
                b_read = _f_values_in.read(_buf_val_in);
                if (b_read==-1) {
                    System.err.println("less than 50MB");
                    break;
                }
                _dbuf_val_in.position(0);
                _dbuf_val_in.limit(b_read / 8);
            }
            counts.adjustOrPutValue(_dbuf_val_in.get(), 1, 1);
            if (counts.size()>4000000) {
                System.err.println("pruning counts.");
                counts.forEachEntry(new TDoubleIntProcedure() {
                    public boolean execute(double arg0, int arg1) {
                        if (arg1==1) counts.remove(arg0);
                        return true;
                    }
                });
            }
        }
        System.err.format("%d different values\n", counts.size());
        final List<Map.Entry<Integer, Double>> highestCounts =
                new ArrayList<Map.Entry<Integer, Double>>();
        counts.forEachEntry(new TDoubleIntProcedure() {

            int lowest = 0;
            int count = 0;

            public boolean execute(double dbl, int count) {
                if (count >= lowest) {
                    highestCounts.add(
                            new AbstractMap.SimpleEntry<Integer, Double>(count, dbl));
                    if (highestCounts.size() == 256) {

                        Collections.sort(highestCounts, cmp);
                        int sz = highestCounts.size();
                        while (sz > 128) {
                            sz -= 1;
                            highestCounts.remove(sz);
                        }
                        lowest = highestCounts.get(sz - 1).getKey();
                    }
                }
                return true;
            }
        });
        Collections.sort(highestCounts, cmp);
        System.err.format("%d largest\n", highestCounts.size());
        System.err.format("%d <= count <= %d\n",
                highestCounts.get(highestCounts.size() - 1).getKey(),
                highestCounts.get(0).getKey());

        most_frequent = new Alphabet<Double>();
        for (int i = 0; i < 128; i++) {
            most_frequent.lookupIndex(highestCounts.get(i).getValue());
        }
        most_frequent.stopGrowth();
    }
    private static final ByteBuffer bbuf = ByteBuffer.allocate(16);
    private static final FloatBuffer fbuf = bbuf.asFloatBuffer();

    private int writeDouble(double d) {
        //System.err.println("writeDouble("+d+")");
        int k = most_frequent.lookupIndex(d);
        if (k >= 0) {
            //System.err.println("most_freq =>"+k);
            _buf_val_out.put((byte) k);
            return 1;
        } else {
            int fbits=Float.floatToIntBits((float)d);
            _buf_val_out.put((byte) ((fbits >> 1) | 0x80));
            _buf_val_out.putShort((short)(fbits>>16));
            _buf_val_out.put((byte)(fbits>>8));
            return 4;
        }
    }

    public void copy_values() throws IOException {
        _f_values_out = new FileOutputStream(_prefix + "_valC.bin").getChannel();
        _f_offsets_out = new FileOutputStream(_prefix + "_offC.bin").getChannel();
        _f_values_in.position(0);
        _f_offsets.position(0);
        _buf_val_in.clear();
        int b_read = _f_values_in.read(_buf_val_in);
        _dbuf_val_in.position(0);
        _dbuf_val_in.limit(b_read / 8);
        _buf_off_in.clear();
        b_read = _f_offsets.read(_buf_off_in);
        _lbuf_off_in.position(0);
        _lbuf_off_in.limit(b_read/8);
        long val_pos = 0;
        int n_offsets = (int) (_f_offsets.size() / 8);
        for (int i = 0; i < n_offsets; i++) {
            if (!_lbuf_off_in.hasRemaining()) {
                _buf_off_in.clear();
                int sz = _f_offsets.read(_buf_off_in);
                _lbuf_off_in.position(0);
                _lbuf_off_in.limit(sz / 8);
            }
            long next_val = _lbuf_off_in.get();
            if (i%1000==0) {
                System.err.format("\rread: %d MiB wrote %d MiB ratio=%f i=%d next_val=%d",
                        n_read>>20, n_written>>20, (double)n_read/(double)n_written,
                        i, next_val);
            }
            while (val_pos < next_val) {
                if (!_dbuf_val_in.hasRemaining()) {
                    _buf_val_in.clear();
                    b_read = _f_values_in.read(_buf_val_in);
                    if (b_read==-1) {
                        System.err.println("EOF in values file.");
                        break;
                    }
                    n_read+=b_read;
                    _dbuf_val_in.position(0);
                    _dbuf_val_in.limit(b_read / 8);
                }
                if (_buf_val_out.remaining() < 8) {
                    _buf_val_out.flip();
                    _f_values_out.write(_buf_val_out);
                    _buf_val_out.clear();
                }
                val_pos++;
                n_written += writeDouble(_dbuf_val_in.get());
            }
            if (_buf_off_out.remaining() < 8) {
                _buf_off_out.flip();
                _f_offsets_out.write(_buf_off_out);
                _buf_off_out.clear();
            }
            _buf_off_out.putLong(n_written);
        }
        System.err.println();
        _buf_val_out.flip();
        _f_values_out.write(_buf_val_out);
        _buf_val_out.clear();
        _buf_off_out.flip();
        _f_offsets_out.write(_buf_off_out);
        _buf_off_out.clear();
        _f_offsets_out.close();
        _f_values_out.close();
        ObjectOutputStream os=new ObjectOutputStream(
                new FileOutputStream(_prefix+"_dictC.obj"));
        os.writeObject(most_frequent);
        os.close();
    }

    public static void main(String[] args) {
        try {
            CompressedMatrixWriter w = new CompressedMatrixWriter(args[0]);
            w.find_most_frequent();
            System.err.println("10 most frequent values:");
            for (int i = 0; i < 10; i++) {
                System.err.format("[%d] %f\n", i, w.most_frequent.lookupObject(i));
            }
            w.copy_values();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
