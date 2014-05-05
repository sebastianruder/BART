/*
 * Copyright 2007 Project ELERFED
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

package elkfed.ml.svm;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import elkfed.ml.FeatureDescription;
import elkfed.ml.Instance;
import elkfed.ml.InstanceWriter;
import elkfed.ml.util.Alphabet;
import elkfed.ml.util.FeatureVector;
import elkfed.ml.util.KVFunc;

/** Used to write instances to a file in SVMLight format.
 *
 * @author vae2101
 */
public class SVMLightInstanceWriter implements InstanceWriter {
    
    //Specs taken from http://ai-nlp.info.uniroma2.it/moschitti/TK1.2-software/Tree-Kernel.htm
    
    private Writer _file;
    private List<FeatureDescription> _fds;
    protected Alphabet<String> dict;
    protected File _dictFile;
    final protected boolean _testingMode;
    
    
    public SVMLightInstanceWriter(Writer output, File dictFile, boolean mode) {
        dict = new Alphabet<String>();
        _testingMode=mode;
        if (_testingMode) {
            dict.load(dictFile);
        } else {
            // SVMLight requires indices to start at 1
            dict.lookupIndex("*DUMMY*");
        }
        _file = output;
        _dictFile=dictFile;
    }
    
    public SVMLightInstanceWriter(Writer output, File dictFile) {
        this(output,dictFile,false);
    }
    
    public void setHeader(List<FeatureDescription> fds) {
        _fds = fds;
    }
    
    public int writeTree(StringBuilder sb, Instance inst) {
        int nTreeFeatures=0;
        for (int i=0; i < _fds.size()-1; i++) {
            switch (_fds.get(i).type) {
                case FT_TREE_STRING:
                    sb.append(" ");
                    writeBT(sb);
                    String valS=(String)inst.getFeature(_fds.get(i));
                    if (valS==null)
                        sb.append(" ");
                    else
                        sb.append(valS);
                    nTreeFeatures++;
                    break;
                case FT_TREE_TREE:
                    sb.append(" ");
                    writeBT(sb);
                    Tree valT=(Tree)inst.getFeature(_fds.get(i));
                    if (valT==null)
                        sb.append(" ");
                    else
                        valT.toStringBuilder(sb);
                    nTreeFeatures++;
                    break;
                default:
                    break;
            }
        }
        return nTreeFeatures;
    }
    
    public void writeVector(final StringBuilder sb, Instance inst){
        FeatureVector<String> indexToValue = makeFV(inst);
        
        // dump sparse vector to buffer
        // since we use a TreeMap, the entries come out in
        // ascending order, which is what we need for SVMlight-TK
        indexToValue.put(new KVFunc() {
            public void put(int key, double value) {
                sb.append(" " + key + ":" + value);
            }
        });
    }

    public FeatureVector<String> makeFV(final Instance inst)
    {
        FeatureVector<String> indexToValue = new FeatureVector(dict);
        
        for (int i=0; i < _fds.size()-1; i++) {
            //sb.append(" ");
            FeatureDescription fd=_fds.get(i);
            switch (fd.type) {
                case FT_BOOL:
                    String outcome;
                    if (inst.getFeature(fd).equals(Boolean.TRUE)) {
                        outcome="+";
                    } else {
                        outcome="-";
                    }
                    indexToValue.setFeatureValue(fd.name+outcome,+1.0);
                    break;
                case FT_SCALAR:
                    Number valN=(Number)inst.getFeature(fd);
                    if (valN!=null)
                        indexToValue.setFeatureValue(fd.name,
                            valN.doubleValue());
                    break;
                case FT_NOMINAL_ENUM:    //same treatment as FT_STRING
                case FT_STRING:
                    Object valO=inst.getFeature(fd);
                    if (valO!=null)
                        indexToValue.setFeatureValue(fd.name+ "=" +
                            valO.toString(),1.0);
                    break;
                case FT_TREE_STRING:
                case FT_TREE_TREE:
                    break;
                default:
                    throw new RuntimeException("unknown feature type:"+_fds.get(i).type);
            }
        }
        return indexToValue;
    }
    
    public void write(Instance inst) throws IOException {
        
        StringBuilder sb = new StringBuilder();
        write(inst, sb);
        
        _file.write(sb.toString() + "\n");
    }

    public void write(final Instance inst, final StringBuilder sb) throws NullPointerException {
        //set target (-1 or 1 )
    
        try
        {
            if ((Boolean)inst.getFeature((_fds.get(_fds.size()-1))))
                sb.append("+1");
            else
                sb.append("-1");
        }
        catch (NullPointerException e)
        {
            if (_testingMode)
                sb.append("0");
            else
                throw e;
        }
        
        //write the trees
        int nTrees=writeTree(sb,inst);
        if (nTrees>0) writeET(sb);
        
        //write the feature vector
        writeVector(sb,inst);
    }
    
    public void writeBT(StringBuilder sb) {
        sb.append("|BT|");
    }
    
    public void writeET(StringBuilder sb) {
        sb.append("|ET|");
    }
    
    
    public void writeBV(StringBuilder sb) {
        sb.append("|BV|");
    }
    
    
    public void writeEV(StringBuilder sb) {
        sb.append("|EV|");
    }
    
    
    public void close() throws IOException {
        _file.close();
        if (!_testingMode)
            dict.save(_dictFile);
    }
    
    public void flush() throws IOException {
        _file.flush();
        if (!_testingMode)
            dict.save(_dictFile);
    }
}


