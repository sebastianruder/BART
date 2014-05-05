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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author yannick
 */
public class NGramFile {
    private final static int __BUF_SIZE=512;
    SortedMap<String,String> _fileIndex;
    File _dir;
    
    /** Creates a new instance of GoogleNGram */
    public NGramFile(File dir, File idxFile)
    throws FileNotFoundException, IOException {
        _dir=dir;
        _fileIndex=new TreeMap<String,String>();
        BufferedReader br=new BufferedReader(new java.io.FileReader(idxFile));
        String line;
        while ((line=br.readLine())!=null) {
            String[] vals=line.split("\t");
            if (vals[0].endsWith(".gz"))
                vals[0]=vals[0].substring(0,vals[0].length()-3);
            _fileIndex.put(vals[1],vals[0]);
        }
    }
    
    int query_exact(String item) throws FileNotFoundException, IOException {
        String fname=_fileIndex.get(item);
        if (fname==null) {
            String lastKey=_fileIndex.headMap(item).lastKey();
            fname=_fileIndex.get(lastKey);
        }
        //System.err.format("'%s': filename is '%s'\n",item,fname);
        String path=new File(_dir,fname).getAbsolutePath();
        RandomAccessFile f=new RandomAccessFile(path,
                "r");
        long pos1=0;
        long pos2=(int)f.length();
        byte[] buf=new byte[__BUF_SIZE];
        while (pos2-pos1>__BUF_SIZE/2) {
            long posM=(pos1+pos2)/2;
            f.seek(posM);
            f.read(buf);
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
        f.seek(pos1);
        f.read(buf);
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
            {
                f.close();
                return 0;
            }
            if (line.equals(item)) {
                int nl3=findCharacter(buf,nl2,(byte)'\n');
                int retval=Integer.parseInt(new String(buf,nl2+1,nl3-nl2-1));
                f.close();
                return retval;
            }
            off=nl2+1;
        }
    }
    
    void query_prefix(String item, Map<String,Integer> result,
            StringFilter filter)
            throws FileNotFoundException, IOException {
        String fname=_fileIndex.get(item);
        String last_fname=fname;
        if (fname==null) {
            String lastKey=_fileIndex.headMap(item).lastKey();
            fname=_fileIndex.get(lastKey);
            last_fname=fname;
        }
        //System.err.format("'%s': filename is '%s'\n",item,fname);
        String path=new File(_dir,fname).getAbsolutePath();
        RandomAccessFile f=new RandomAccessFile(path,
                "r");
        long pos1=0;
        long pos2=(int)f.length();
        byte[] buf=new byte[__BUF_SIZE];
        while (pos2-pos1>__BUF_SIZE/2) {
            long posM=(pos1+pos2)/2;
            f.seek(posM);
            f.read(buf);
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
        f.seek(pos1);
        // RandomAccessFile is slooow, so we use a FileReader/BufferedReader
        BufferedReader br=new BufferedReader(new FileReader(f.getFD()));
        String line;
        // read the rest of line that may be in that position
        line=br.readLine();
        line=br.readLine();
        while (line.compareTo(item)<0) {
            //System.err.println("skipping "+line);
            line=br.readLine();
            if (line==null) {
                // when we drop off the end of one file, we try to
                // continue with the next file
                String nextKey=_fileIndex.tailMap(last_fname).firstKey();
                last_fname=nextKey;
                String fname2=_fileIndex.get(nextKey);
                String path2=new File(_dir,fname2).getAbsolutePath();
                System.err.println("Continuing search to "+path2);
                br=new BufferedReader(new FileReader(path2));
                line=br.readLine();
            }
        }
        if (line==null) System.err.println("line==null: "+item);
        while (line.startsWith(item)) {
            int offset=line.indexOf('\t');
            String key=line.substring(0,offset);
            String val=line.substring(offset+1);
            if (filter==null || filter.wants(key))
                result.put(key,Integer.parseInt(val));
            line=br.readLine();
            if (line==null) {
                // when we drop off the end of one file, we try to
                // continue with the next file
                String nextKey=_fileIndex.tailMap(last_fname).firstKey();
                last_fname=nextKey;
                String fname2=_fileIndex.get(nextKey);
                String path2=new File(_dir,fname2).getAbsolutePath();
                System.err.println("Continuing search to "+path2);
                br=new BufferedReader(new FileReader(path2));
                line=br.readLine();
            }
        }
    }
    
    
    private static int findCharacter(byte[] buf, int off, byte ch) {
        for (int i=off; i<__BUF_SIZE; i++) {
            if (buf[i]==ch)
                return i;
        }
        return -1;
    }
    
    public static void main(String[] args) {
        try {
            NGramFile ngram=new NGramFile(new File("/mnt/stick/web_5gram_all/3gms/"),
                    new File("/mnt/stick/web_5gram_all/3gms/3gm.idx"));
            String[] testItems={"Wall Street Journal", "Peter and Alan",
            "worse than that",
            "Snafu and friends", // not present
            "Students realize they", // end of a file
            "Students realized that"}; // start of a file
            for (String item: testItems) {
                int result=ngram.query_exact(item);
                System.out.format("'%s' -> %d\n",item,result);
            }
            SortedMap<String,Integer> result=new TreeMap<String,Integer>();
            ngram.query_prefix("Students realize",result,null);
            System.out.println(result);
            result=new TreeMap<String,Integer>();
            ngram.query_prefix("Chicago",result,null);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
