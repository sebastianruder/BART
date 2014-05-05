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

import edu.stanford.nlp.util.StringUtils;
import elkfed.config.ConfigProperties;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.FeatureVector;
import elkfed.ml.util.IVector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
//import qtag.Tagger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yannick
 */
public class GoogleNGram {
    private static GoogleNGram _instance;
//    private static Tagger _tagger;
//    
//    public static Tagger getTagger()
//    {
//        if (_tagger==null)
//        {
//            try {
//                _tagger=new Tagger(
//                    "./models/pos/qtag-eng");
//            }
//            catch(Exception e)
//            {
//                throw new RuntimeException("cannot construct tagger",e);
//            }
//        }
//        return _tagger;
//    }
    
    public static class DummyMap implements Map<String,Integer> {
        private final FeatureVector<String> feats;
        private final int offset;
        private final int start, end;
        
        public DummyMap(FeatureVector<String>f, int o, int s, int e) {
            feats=f;
            offset=o;
            start=s; end=e;
        }
        
        public int size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Integer get(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Integer put(String key, Integer value) {
            int prevIndex=offset;
            if (key.charAt(prevIndex)!=' ') return null;
            for (int i=0;i<end;i++) {
                int nextIndex=key.indexOf(' ',prevIndex+1);
                if (nextIndex==-1) nextIndex=key.length();
                if (i>=start) {
                    feats.addFeatureValue(key.substring(prevIndex+1,nextIndex),
                            value);
                }
            }
            return null;
        }

        public Integer remove(Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putAll(Map<? extends String, ? extends Integer> m) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<String> keySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<Integer> values() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<Entry<String, Integer>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static GoogleNGram getInstance()
    {
        if (_instance==null)
        {
            try {
            _instance=new GoogleNGram(
                    new File(ConfigProperties.getInstance().getNGramDir()));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Cannot find NGramDir",e);
            }
        }
        return _instance;
    }

    protected NGramFile[] _files;
    protected UnigramFile _unigrams;

    public int query_exact(String query)
    {
        String[] words=query.split(" ");
        int len=words.length;
        if (len==1)
        {
            try {
                return (int)_unigrams.query_exact(query);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot read unigram file",ex);
            }
        }
        else if (len>=2 && len <=5)
        {
            try {
                return _files[len-2].query_exact(query);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("Cannot read ngram file",ex);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot read ngram file",ex);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot do >5 -grams");
        }
    }
    
    public double pattern_ratio(String part1, String pattern, String part2)
    {
        double c1=query_exact(part1+" "+pattern+" "+part2);
        double c2=query_exact(pattern);
        double c3=query_exact(part1+" "+pattern);
        double c4=query_exact(pattern+" "+part2);
//        System.err.format("p(%s|%s,%s)=%f; p(%s|%s)=%f",
//                part1,pattern,part2,c1/c4,
//                part1,pattern,c3/c2);
        double ratio=c1*c2/
                (c3*c4);
        return ratio;
    }
    
    public SortedMap<String,Integer> query_prefix(String prefix,
            StringFilter filter)
    {
        String[] words=prefix.split(" ");
        int len=words.length;
        if (len>=5)
        {
            throw new UnsupportedOperationException("Cannot do >5 -grams");
        }
        SortedMap<String,Integer> result=new TreeMap<String,Integer>();
        String wanted=prefix+" ";
        for (int i=len+1; i<=5; i++)
        {
            try {
                _files[i-2].query_prefix(wanted,result,filter);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("Cannot read ngram file",ex);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot read ngram file",ex);
            }
        }
        return result;
    }
    
    public IVector query_rcollocates(Alphabet features, String word) {
        try {
            FeatureVector result = new FeatureVector(features);
            _files[1].query_prefix(word, new DummyMap(result,word.length(),0,2),null);
            _files[3].query_prefix(word, new DummyMap(result,word.length(),2,4),null);
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoogleNGram.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            Logger.getLogger(GoogleNGram.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    public SortedMap<String,Integer> query_anchored(String prefix, String suffix)
    {
        final String wantedsuffix=" "+suffix;
        StringFilter filter=new StringFilter() {
            public boolean wants(String item)
            { return item.endsWith(wantedsuffix); }
        };
        SortedMap<String,Integer> result=query_prefix(prefix,filter);
        return result;
    }
    
    public SortedMap<String,Integer> query_rotePatterns(String prefix, String suffix)
    {
        SortedMap<String,Integer> result0=query_anchored(prefix, suffix);
        SortedMap<String,Integer> result=new TreeMap<String,Integer>();
        int prefix_len=prefix.length()+1;
        int suffix_len=suffix.length()+1;
        for (String key: result0.keySet())
        {
            int end=key.length()-suffix_len;
            if (prefix_len<end)
            {
                String key2=key.substring(prefix_len,end);
                result.put(key2,result0.get(key));
            }
            else if (prefix_len==end-1)
            {
                result.put("",result0.get(key));
            }
        }
        return result;
    }
    
    /** Creates a new instance of GoogleNGram */
    private GoogleNGram(File directory)
        throws FileNotFoundException, IOException
    {
        _files=new NGramFile[4];
        for (int i=2; i<=5; i++)
        {
            File subdir=new File(directory,String.format("%dgms",i));
            _files[i-2]=new NGramFile(subdir,
                    new File(subdir,String.format("%dgm.idx",i)));
        }
        _unigrams=new UnigramFile(new File(directory,"1gms/vocab").getCanonicalPath());
    }
    
    public static interface StringTransformer
    {
        String transformString(String orig);
    }
    
    private static class FreqShorten implements StringTransformer
    {
        private UnigramFile _uni;
        private FreqShorten(UnigramFile uni)
        {
            _uni=uni;
        }
        public String transformString(String orig)
        {
            String[] tokens=orig.split(" ");
            try {
                for (int i=0; i<tokens.length; i++)
                {
                    if (_uni.query_exact(tokens[i])<100000000L)
                    {
                        tokens[i]="**";
                    }
                }
                String processed=StringUtils.join(tokens," ");
                return processed;
            } catch (IOException e)
            { throw new RuntimeException("IOException",e); }
        }
    }
    
//    private static class POSShorten implements StringTransformer
//    {
//        private final String[] _front;
//        private final String[] _back;
//        private final Tagger _tagger;
//        private final UnigramFile _uni;
//        public POSShorten(UnigramFile uni, Tagger tagger,
//                String front, String back)
//        {
//            _uni=uni;
//            _tagger=tagger;
//            _front=front.split(" ");
//            _back=back.split(" ");
//        }
//
//        public String transformString(String orig) {
//            String[] tokens=orig.split(" ");
//            List<String> sentence = new ArrayList<String>();
//            for (String word : _front)
//            { sentence.add(word); }
//            for (String word : tokens)
//            { sentence.add(word); }
//            for (String word : _back)
//            { sentence.add(word); }            
//            String[] taggedSentence = _tagger.tag(sentence);
//            try {
//                for (int i=0; i<tokens.length; i++)
//                {
//                    if (_uni.query_exact(tokens[i])<100000000L)
//                    {
//                        tokens[i]=String.format("[%s]",
//                                taggedSentence[_front.length+i]);
//                    }
//                }
//                String processed=StringUtils.join(tokens," ");
//                return processed;
//            } catch (IOException e)
//            { throw new RuntimeException("IOException",e); }
//        }
//    }
    
    public static SortedMap<String,Integer> simplifyForms(SortedMap<String,Integer> orig,
            StringTransformer xform)
    {
        TreeMap<String,Integer> results=new TreeMap<String,Integer>();
        for (String key: orig.keySet())
        {
            String key2=xform.transformString(key);
            if (key2!=null)
            {
                int val0;
                try {
                    val0=results.get(key2);
                }
                catch (NullPointerException e)
                {
                    val0=0;
                }
                results.put(key2,val0+orig.get(key));
            }
        }
        return results;
    }
    
    public static void extractPatternVector(String s1, String s2,
            String prefix, FeatureVector<String> token)
    {
        GoogleNGram ngram=getInstance();
        SortedMap<String,Integer> result;
        result=ngram.query_rotePatterns(s1,s2);
//        result=simplifyForms(result, new POSShorten(ngram._unigrams,
//                getTagger(), s1, s2));
//        result=simplifyForms(result, new FreqShorten(ngram._unigrams));
        StringBuffer buf=new StringBuffer();
        buf.append(prefix);
        final int prefix_len=buf.length();
        for (String key: result.keySet())
        {
            buf.setLength(prefix_len);
            buf.append(key);
            token.setFeatureValue(buf.toString(),
                    1+Math.log(result.get(key)/40.0));
        }
    }
 
    public static void main(String[] args)
    {
        try {
            GoogleNGram ngram=getInstance();
            String[] testItems={"hardship", "and the", "that the",
                        "John Smith", "the cities of",
                        "Paris and other cities",
                        "Bill Clinton and other presidents"};
            for (String item: testItems) {
                int result=ngram.query_exact(item);
                System.out.format("'%s' -> %d\n",item,result);
            }
            System.out.println(ngram.query_prefix("Bill Clinton and other",null));
            System.out.format("Bill Clinton and other presidents -> %f\n",
                    ngram.pattern_ratio("Bill Clinton","and other","presidents"));
            System.out.println(ngram.query_prefix("Monsanto and other",null));
            System.out.format("Monsanto and other companies -> %f\n",
                    ngram.pattern_ratio("Monsanto","and other","companies"));
            Alphabet myWords=new Alphabet();
            System.out.format("RCollocates of died: %s\n",
                    ngram.query_rcollocates(myWords, "died"));
            System.out.format("RCollocates of destroyed: %s\n",
                    ngram.query_rcollocates(myWords, "destroyed"));
//            System.out.println(ngram.query_rotePatterns("teacher", "pupil"));
//            System.out.println(ngram.query_rotePatterns("house", "mansion"));
//            System.out.println(ngram.query_rotePatterns("mansion", "house"));
//            System.out.println(ngram.query_rotePatterns("companies", "Sony"));
//            System.out.println(ngram.query_rotePatterns("company was","by"));
//            System.out.println(ngram.query_rotePatterns("Sony was","by"));            
//            System.out.println(ngram.query_rotePatterns("company did not","the"));  
//            Tagger tagger=getTagger();
//            System.out.println(simplifyForms(ngram.query_rotePatterns("Iraq","country"),
//                    new POSShorten(ngram._unigrams,tagger, "Iraq", "country")));
//            System.out.println(simplifyForms(ngram.query_rotePatterns("Iraq","business"),
//                    new POSShorten(ngram._unigrams,tagger, "Iraq", "country")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
