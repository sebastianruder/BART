/*
 * FE_HeadStrudelSimilarity.java
 *
 * Created on July 12, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.features.pairs;

import elkfed.coref.*;
import elkfed.coref.mentions.Mention;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.knowledge.StrudelDatabase;

/**
Feature used to compute Strudel similarity
 * @author kepa
 */
public class FE_StrudelNoRelatedness implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_IS_STRUDELNORELATED =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "StrudelNoRelated");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_STRUDELNORELATED);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_STRUDELNORELATED, getStrNoRelated(inst));
    }

    private String getHeadStr4Strudel(Mention m) {
        String str;
        if (m.getNumber()) {
            str = m.getHeadString();
        } else {
            str = m.getHeadOrName();
        }
        return str;
    }

    private String getHeadStr4Strudel2(Mention m) {
        String str = null;
        if (m.getHeadString().equals("men") ||
                m.getHeadString().equals("Men") ||
                m.getHeadString().equals("Man")) {
            str = "man";
        } else if (m.getHeadString().equals("women") ||
                m.getHeadString().equals("Women") ||
                m.getHeadString().equals("Woman")) {
            str = "woman";
        } else {
            str = m.getHeadString();
        }
        return str;
    }

    private String getHeadStr4Strudel3(Mention m) {
        String str = null;
        if (m.getHeadString().equals("men") ||
                m.getHeadString().equals("Men") ||
                m.getHeadString().equals("Man")) {
            str = m.getHeadString();
        } else if (m.getHeadString().equals("women") ||
                m.getHeadString().equals("Women") ||
                m.getHeadString().equals("Woman")) {
            str = m.getHeadString();
        } else {
            str = m.getHeadString();
        }
        return str;
    }

    private boolean getStrNoRelated(PairInstance inst) {
        boolean unrelated = false;
        String typeAna;
        String typeAnte;

        if (inst.getAnaphor().getPronoun() ||
                inst.getAnaphor().getProperName() ||
                inst.getAnaphor().isEnamex()) {
            typeAna = type2string(inst.getAnaphor().getSemanticClass().toString());

        } else {
//            typeAna = inst.getAnaphor().getHeadString();
//              typeAna = inst.getAntecedent().getHeadOrName();
            typeAna = getHeadStr4Strudel(inst.getAnaphor());
        }
        if (inst.getAntecedent().getPronoun() ||
                inst.getAntecedent().getProperName() ||
                inst.getAntecedent().isEnamex()) {
            typeAnte = type2string(inst.getAntecedent().getSemanticClass().toString());
        } else {
//            typeAnte = inst.getAntecedent().getHeadString();
//              typeAnte = inst.getAntecedent().getHeadOrName();
                       typeAnte = getHeadStr4Strudel(inst.getAntecedent());
        }
        if (!typeAna.equals("UNKNOWN") && !typeAnte.equals("UNKNOWN")) {
            if (typeAna.equals(typeAnte)) {
                unrelated = false;
            } else if (StrudelDatabase.getInstance().consultStrudelDB(typeAna + "-n", typeAnte + "-n") < 0.2) {
                unrelated = true;
            } else {
                unrelated = false;
            }
        }

        //    System.out.println("PAIR: " +inst.getAnaphor().getHeadString() + "-n" +" " + inst.getAntecedent().getHeadString() + "-n");
        //       System.out.println("PAIR: " +inst.getAnaphor().getHeadOrName() + "-n" +" " + inst.getAntecedent().getHeadOrName() + "-n");
        //        System.out.println("PAIR: " +getHeadStr4Strudel(inst.getAnaphor()) + "-n" +" " + getHeadStr4Strudel(inst.getAntecedent()) + "-n");
        //         System.out.println("PAIR: " +getHeadStr4Strudel2(inst.getAnaphor()) + "-n" +" " + getHeadStr4Strudel2(inst.getAntecedent()) + "-n");
        //     }
        //return similarity;
        return unrelated;
    }

    private String type2string(String semtype) {
//    String semtype = null;
        String newtype = null;
        if (semtype.equals("MALE")) {
            newtype = "man";
        } else if (semtype.equals("FEMALE")) {
            newtype = "woman";
        } else if (semtype.equals("GPE")) {
            newtype = "place";
        } else if (semtype.equals("UNKNOWN")) {
            newtype = "UNKNOWN";
        } else {
            newtype = semtype.toLowerCase();
        }
        return newtype;

    }
}
