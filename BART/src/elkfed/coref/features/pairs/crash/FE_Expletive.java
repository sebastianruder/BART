/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
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
package elkfed.coref.features.pairs.crash;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.expletives.EF_Tree;
import elkfed.expletives.ExpletiveInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.svm.SVMClassifierFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** uses an expletive classifier to filter out cases of expletive 'it'
 *
 * @author versley
 */
public class FE_Expletive implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_EXPLETIVE=
            new FeatureDescription<Boolean>(
            FeatureType.FT_BOOL,"EXPL");
    private Mention mention_cache;
    private boolean classification_cache;
    List<FeatureExtractor<ExpletiveInstance>> fes;
    OfflineClassifier cf;
    
    public static List<FeatureExtractor<ExpletiveInstance>> getExtractors() {
        List<FeatureExtractor<ExpletiveInstance>> fes=
                new ArrayList<FeatureExtractor<ExpletiveInstance>>();
        fes.add(new EF_Tree());
        //fes.add(new EF_LRVerb());
        //fes.add(new EF_POS());
        return fes;
    }
    
    public FE_Expletive() {
        fes=getExtractors();
        try {
            cf=SVMClassifierFactory.getInstance().getClassifier("expl","","");
            List<FeatureDescription>fds=new ArrayList<FeatureDescription>();
            for (FeatureExtractor fe: fes) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            cf.setHeader(fds);
        } catch (IOException ex) {
            ex.printStackTrace();
            cf=null;
            throw new RuntimeException("Expletive model not found",ex);
        }
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_EXPLETIVE);
    }

    public boolean is_nonref_it(Mention m) {
        if (!m.getMarkableString().equalsIgnoreCase("it")) {
            return false;
        }
        if (mention_cache==m) {
            return classification_cache;
        } else {
            boolean result=false;
            ExpletiveInstance inst=new ExpletiveInstance(m.getSentenceTree(),
                    m.getLowestProjection(),m.toString()+m.getMarkable().getID());
            if (cf!=null) {
                for (FeatureExtractor fe: fes) {
                    fe.extractFeatures(inst);
                }
                List l_res=new ArrayList();
                cf.classify(Arrays.asList(new Instance[]{inst}), l_res);
                result=(Boolean)l_res.get(0);
            }
            mention_cache=m;
            classification_cache=result;
            return result;
        }
        
    }
    
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_EXPLETIVE,
                is_nonref_it(inst.getAntecedent()) ||
                is_nonref_it(inst.getAnaphor()));
    }

}
