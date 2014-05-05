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
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import elkfed.ml.Instance;
import elkfed.ml.OfflineClassifier;
import elkfed.ml.TriValued;
import elkfed.ml.stacking.StackingClassifierFactory;
import elkfed.ml.svm.SVMClassifierFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * returns true iff one of the mentions is a pronoun,
 * both are in the same sentence and neither is a reflexive
 * @author versley
 */
public class FE_SSPron implements PairFeatureExtractor {
    List<PairFeatureExtractor> fes;
    OfflineClassifier cf;
    
    public static List<PairFeatureExtractor> getExtractors() {
        List<PairFeatureExtractor> fes=new ArrayList<PairFeatureExtractor>();
        fes.add(new FE_Gender());
        fes.add(new FE_Number());
        fes.add(new FE_SemanticClass());
        //fes.add(new FE_First_Mention());
        //fes.add(new FE_FirstSecondPerson());
        fes.add(new FE_TreeFeature());
        //fes.add(new FE_SynPos());
        fes.add(new FE_StringMatch());
        return fes;
    }
    
    public static FeatureDescription<TriValued> FD_SSPRON=
            new FeatureDescription<TriValued>(FeatureType.FT_NOMINAL_ENUM,
                TriValued.class,"SSPRON");
    
    public FE_SSPron() {
        fes=getExtractors();
        try {
            cf=new StackingClassifierFactory(
                    SVMClassifierFactory.getInstance()).getClassifier("ss_pron","","");
            List<FeatureDescription>fds=new ArrayList<FeatureDescription>();
            for (FeatureExtractor fe: fes) {
                fe.describeFeatures(fds);
            }
            fds.add(PairInstance.FD_POSITIVE);
            cf.setHeader(fds);
        } catch (IOException ex) {
            ex.printStackTrace();
            cf=null;
        }
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SSPRON);
    }

    public void extractFeatures(PairInstance inst) {
        Mention ana=inst.getAnaphor();
        Mention ante=inst.getAntecedent();
        if (ana.getSentId()!=ante.getSentId() ||
                !(ana.getPronoun() ||
                    ante.getPronoun()) ||
                ana.getReflPronoun() ||
                ante.getReflPronoun()) {
            inst.setFeature(FD_SSPRON, TriValued.UNKNOWN);
        } else {
            boolean result=false;
            if (cf!=null) {
                for (FeatureExtractor fe: fes) {
                    fe.extractFeatures(inst);
                }
                List l_res=new ArrayList();
                cf.classify(Arrays.asList(new Instance[]{inst}), l_res);
                result=(Boolean)l_res.get(0);
            }
            inst.setFeature(FD_SSPRON,
                    (result ? TriValued.TRUE : TriValued.FALSE));
        }
    }

}
