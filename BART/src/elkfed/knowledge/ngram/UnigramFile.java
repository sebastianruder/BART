/*
 * Copyright 2007 Yannick Versley / Univ. Tuebingen
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

package elkfed.knowledge.ngram;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author yannick
 */
public class UnigramFile {
    public static long total_tokens=1024908267229L;
    private final static int __BUF_SIZE=512;
    protected RandomAccessFile _f;
    protected Map<String,Long> _cache;

    /** Creates a new instance of UnigramFile */
    public UnigramFile(String filename) throws FileNotFoundException
    {
        _f=new RandomAccessFile(filename,"r");
        _cache=new HashMap<String,Long>();
    }
    
    public long query_exact(String item)
        throws IOException
    {
        if (_cache.containsKey(item))
            return _cache.get(item);
                long pos1=0;
        long pos2=(int)_f.length();
        byte[] buf=new byte[__BUF_SIZE];
        while (pos2-pos1>__BUF_SIZE/2) {
            long posM=(pos1+pos2)/2;
            _f.seek(posM);
            _f.read(buf);
            int nl1=findCharacter(buf,0,(byte)'\n');
            if (nl1==-1)
                throw new RuntimeException("Cannot find newline...");
            int nl2=findCharacter(buf,nl1+1,(byte)'\t');
            if (nl2==-1)
                throw new RuntimeException("Cannot find tab...");
            String line=new String(buf,nl1+1,nl2-nl1-1);
            if (line.compareTo(item)>0)
                pos2=posM;
            else
                pos1=posM;
        }
        _f.seek(pos1);
        _f.read(buf);
        int off=0;
        while(true) {
            int nl1=findCharacter(buf,off,(byte)'\n');
            if (nl1==-1)
                throw new RuntimeException("Cannot find newline...");
            int nl2=findCharacter(buf,nl1+1,(byte)'\t');
            if (nl2==-1)
                throw new RuntimeException("Cannot find newline...");
            String line=new String(buf,nl1+1,nl2-nl1-1);
            //System.err.println(line);
            if (line.compareTo(item)>0)
                return 0;
            if (line.equals(item)) {
                int nl3=findCharacter(buf,nl2,(byte)'\n');
                long retval=Long.parseLong(new String(buf,nl2+1,nl3-nl2-1));
                _cache.put(item,retval);
                return retval;
            }
            off=nl2+1;
        }
    }
    
    private static int findCharacter(byte[] buf, int off, byte ch) {
        for (int i=off; i<__BUF_SIZE; i++) {
            if (buf[i]==ch)
                return i;
        }
        return -1;
    }
}
