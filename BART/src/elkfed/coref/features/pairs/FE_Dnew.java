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
package elkfed.coref.features.pairs;

import java.util.ArrayList;
import java.util.List;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import elkfed.ml.OfflineClassifier;

/** 
 *
 * @author olga, based on expletive stuff by yannick
 */

public class FE_Dnew implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_DNEW=
            new FeatureDescription<Boolean>(
            FeatureType.FT_BOOL,"DNEW");
    private Mention mention_cache;
    private Mention mention_cache_dold;
    private boolean classification_cache;
    private boolean classification_cache_dold;
    List<PairFeatureExtractor> fes;
    OfflineClassifier cf;
    List<FeatureDescription>fds;


  
    public FE_Dnew(List<PairFeatureExtractor> fesdnew,OfflineClassifier modeldnew) {
      fes=fesdnew;
      cf=modeldnew;

      fds=new ArrayList<FeatureDescription>();
      for (FeatureExtractor fe: fes) {
        fe.describeFeatures(fds);
      }            
      fds.add(PairInstance.FD_POSITIVE);
      cf.setHeader(fds);
    }

    public void describeFeatures(List<FeatureDescription> fdds) {
        fdds.add(FD_DNEW);
    }

    public boolean is_dnew(Mention m) {
      return is_dnew(m,0);
    }
    public boolean is_dold(Mention m) {
      return is_dnew(m,0);
    }
    public boolean is_dnew(Mention m, double thres) {
        if (mention_cache==m) {
            return classification_cache;
        } else {
            boolean result=false;
            PairInstance m_inst=new PairInstance(m,m);
            List<PairInstance> cands=new ArrayList<PairInstance>();
            if (cf!=null) {
                for (PairFeatureExtractor fe: fes) {
                    fe.extractFeatures(m_inst);
                }
 
                m_inst.setFeature(PairInstance.FD_POSITIVE,true);

//System.out.println("dnew feature fake pair is " + m_inst);

                cands.add(m_inst);
                List l_res=new ArrayList();
                List l_conf=new ArrayList();

                cf.classify(cands, l_res, l_conf);
                result=(Boolean)l_res.get(0);

System.out.println( " Result of dnew classification: " + (Boolean)l_res.get(0));
System.out.println( " Confidence for dnew classification: " + (Double)l_conf.get(0));

                if (result==true && (Double)l_conf.get(0)<thres) result=false;
            }
            mention_cache=m;
            classification_cache=result;
            return result;
        }

    }

    public boolean is_dold(Mention m, double thres) {
        if (mention_cache_dold==m) {
            return classification_cache_dold;
        } else {
            boolean result=false;
            PairInstance m_inst=new PairInstance(m,m);
            List<PairInstance> cands=new ArrayList<PairInstance>();
            if (cf!=null) {
                for (PairFeatureExtractor fe: fes) {
                    fe.extractFeatures(m_inst);
                }
 
                m_inst.setFeature(PairInstance.FD_POSITIVE,true);


                cands.add(m_inst);
                List l_res=new ArrayList();
                List l_conf=new ArrayList();

                cf.classify(cands, l_res, l_conf);
                result=(Boolean)l_res.get(0);
                if (result==false && (Double)l_conf.get(0)>thres) result=true;
                  else result=false;
                  
            }
            mention_cache_dold=m;
            classification_cache_dold=result;
            return result;
        }

    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DNEW,
                is_dnew(inst.getAntecedent()) ||
                is_dnew(inst.getAnaphor()));
    }

}
