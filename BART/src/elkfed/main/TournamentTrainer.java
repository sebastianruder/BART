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
package elkfed.main;

import elkfed.config.ConfigProperties;
import elkfed.coref.CorefTrainer;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.algorithms.cand_rank.CandRankEncoder;
import elkfed.coref.features.pairs.FE_Alias;
import elkfed.coref.features.pairs.FE_Appositive;
import elkfed.coref.features.pairs.FE_Gender;
import elkfed.coref.features.pairs.mentiontype_old.FE_MentionType;
import elkfed.coref.features.pairs.mentiontype_old.FE_MentionType_Ante;
import elkfed.coref.features.pairs.FE_Number;
import elkfed.coref.features.pairs.FE_SentenceDistance;
import elkfed.coref.features.pairs.FE_StringMatch;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.coref.processors.TrainerProcessor;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.InstanceWriter;
import elkfed.ml.RankerSink;
import elkfed.ml.tournament.CandPairInstance;
import elkfed.ml.tournament.CombinerBoth;
import elkfed.ml.tournament.TournamentSink;
import elkfed.ml.weka.WEKAInstanceWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * implements Yang et al's candidate pair model
 * @author versley
 */
public class TournamentTrainer {
    public static List<PairFeatureExtractor> getExtractors() {
        List<PairFeatureExtractor> fes=
                new ArrayList<PairFeatureExtractor>();
        // basic information about the anaphor
        fes.add(new FE_MentionType());
        // agreement features
        fes.add(new FE_Gender());
        fes.add(new FE_Number());
        // specialized features for aliases / appositive constructions
        fes.add(new FE_Alias());
        fes.add(new FE_Appositive());
        // string matching features
        fes.add(new FE_StringMatch());
        // fes.add(new FE_LeftRightMatch());
        // fes.add(new FE_HeadMatch());
        // semantic class agreement
        fes.add(new FE_SemanticClass());
        fes.add(new FE_SentenceDistance());
        return fes;
    }

    /** returns the combiners; actually, any feature extractor for
     *  a CandPair triple is allowed here
     * @return
     */
    public static List<FeatureExtractor<CandPairInstance>> getCombiners() {
        List<FeatureExtractor<CandPairInstance>> combiners=
                new ArrayList<FeatureExtractor<CandPairInstance>>();
        List<FeatureDescription> fds_both=new ArrayList<FeatureDescription>();
        fds_both.add(FE_MentionType_Ante.FD_I_IS_INDEFINITE);
        fds_both.add(FE_MentionType_Ante.FD_I_IS_DEFINITE);
        fds_both.add(FE_MentionType.FD_I_IS_PRONOUN);
        fds_both.add(FE_MentionType_Ante.FD_I_IS_PN);
        //ante_M_ProperNP Cx is a mentioned proper NP
        //ante_ProperNP_APPOS Cx is a properNP modified by an appositive
        //ante_Appositive Cx is in an apposition structure
        //ante_NearestNP  Cx is the nearest candidate to the anaphor
        //ante_Embedded   Cx is an embedded NP
        //ante_Title      Cx is in title
        
        fds_both.add(FE_StringMatch.FD_IS_STRINGMATCH);
        fds_both.add(FE_Gender.FD_IS_GENDER);
        fds_both.add(FE_Number.FD_IS_NUMBER);
        fds_both.add(FE_Appositive.FD_IS_APPOSITIVE);
        fds_both.add(FE_Alias.FD_IS_ALIAS);
        combiners.add(new CombinerBoth(fds_both));
        
        List<FeatureDescription> fds_first=new ArrayList<FeatureDescription>();
        fds_first.add(FE_MentionType.FD_J_IS_DEFINITE);
        //ana_IndefNP     ana is an indefinite NP
        fds_first.add(FE_MentionType.FD_J_IS_PRONOUN);
        //ana_ProperNP    ana is properNP
        //ana_PronType    neutPL/SG, person 3rd,other
        //ana_FlexiblePron ana is a "flexible pronoun"???
        
        List<FeatureDescription> fds_diff=new ArrayList<FeatureDescription>();
        fds_diff.add(FE_SentenceDistance.FD_SENTDIST);
        //inter_Pdistance  distance between candidates in paragraphs
        return combiners;
    }
    public static void main(String[] args) {
        try {
            InstanceWriter iw_pro=
                    new WEKAInstanceWriter(
                        new FileWriter(
                            new File(
                                ConfigProperties.getInstance().getModelDir(),
                                "tournament_pro.arff"
                             )
                        )
                    );
            InstanceWriter iw_npR=
                    new WEKAInstanceWriter(
                        new FileWriter(
                            new File(
                                ConfigProperties.getInstance().getModelDir(),
                                "tournament_npR.arff"
                             )
                        )
                    );
            InstanceWriter iw_npC=
                    new WEKAInstanceWriter(
                        new FileWriter(
                            new File(
                                ConfigProperties.getInstance().getModelDir(),
                                "tournament_npC.arff"
                             )
                        )
                    );
            RankerSink pron_ranker=new TournamentSink(getCombiners(), iw_pro);
            RankerSink np_ranker=new TournamentSink(getCombiners(), iw_npR);
            CorefTrainer trainer=new CandRankEncoder(getExtractors(),
                    iw_npC,pron_ranker,np_ranker);
            TrainerProcessor proc=new TrainerProcessor(
                    trainer,
                    ConfigProperties.getInstance().getTrainingData(),
                    ConfigProperties.getInstance().getTrainingDataId(),
                    ConfigProperties.getInstance().getMentionFactory()
            );
            proc.createTrainingData();
            pron_ranker.close();
            iw_npC.close();
            np_ranker.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
