/*
 * FE_CorefChain.java
 *
 * Created on March 2nd, 2008
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.mentions.*;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;

import elkfed.nlp.util.Gender;

//For the StrMatch
/**
 * Extract coref chain features: morphosyntactic agreement
 *
 * @author kepa
 */
public class FE_CorefChainMS implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_GENDER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_is_gender");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_ARE_GENDER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_are_gender");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_NUMBER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_is_number");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_ARE_NUMBER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_are_number");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_I_CC_ANY_IS_GENDER);
        //      fds.add(FD_I_CC_ALL_ARE_GENDER);
        fds.add(FD_I_CC_ANY_IS_NUMBER);
//            fds.add(FD_I_CC_ALL_ARE_NUMBER);
    }

    public void extractFeatures(PairInstance inst) {
        // ante

        inst.setFeature(FD_I_CC_ANY_IS_GENDER, getAnyIsGender(inst));
        inst.setFeature(FD_I_CC_ALL_ARE_GENDER, getAllAreGender(inst));
        inst.setFeature(FD_I_CC_ANY_IS_NUMBER, getAnyIsNumber(inst));
        inst.setFeature(FD_I_CC_ALL_ARE_NUMBER, getAllAreNumber(inst));
    }

    /**
     **/
    public Boolean getAnyIsGender(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        Gender genderAnaphora = inst.getAnaphor().getGender();
        int matchGender = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (cc.get(idx).getGender().equals(genderAnaphora)) {
                        matchGender++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchGender > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAllAreGender(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        Gender genderAnaphora = inst.getAnaphor().getGender();
        int matchGender = 0;
        int sizeCorefChain = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            sizeCorefChain = cc.size();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (cc.get(idx).getGender().equals(genderAnaphora)) {
                        matchGender++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchGender == sizeCorefChain) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAnyIsNumber(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        boolean numberAnaphora = inst.getAnaphor().getNumber();
        int matchNumber = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (cc.get(idx).getNumber() == numberAnaphora) {
                        matchNumber++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchNumber > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAllAreNumber(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        boolean numberAnaphora = inst.getAnaphor().getNumber();
        int matchNumber = 0;
        int sizeCorefChain = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            sizeCorefChain = cc.size();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (cc.get(idx).getNumber() == numberAnaphora) {
                        matchNumber++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchNumber == sizeCorefChain) {
            return true;
        } else {
            return false;
        }
    }
}
