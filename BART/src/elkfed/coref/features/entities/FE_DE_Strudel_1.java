/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elkfed.coref.features.entities;

/**
 *
 * @author kepa
 */
import elkfed.coref.*;

import elkfed.coref.discourse_entities.*;

import elkfed.coref.mentions.Mention;
import elkfed.knowledge.StrudelDatabase;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;

//For the StrMatch
public class FE_DE_Strudel_1 implements PairFeatureExtractor {


//FileReader wordPairs =
    public static final FeatureDescription<Double> FD_DE_STRUDEL1 =
            new FeatureDescription<Double>(FeatureType.FT_SCALAR, "DeStrudel1");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_STRUDEL1);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_STRUDEL1, getDEStrudel1(inst));
    }

    public double getDEStrudel1(PairInstance inst) {
        double incompatibility = 0;
        double similarity = 0;
        double pair_similarity = 0;
        double pair_similarity2 = 0;

        DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
        DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

        Set<Property> de_ante_heads = de_ante.getTypes();
        Set<Property> de_ana_heads = de_ana.getTypes();


        for (Property ana_head : de_ana_heads) {
            for (Property ante_head : de_ante_heads) {
                if (!ana_head.giveString().equals("UNKNOWN") && !ante_head.giveString().equals("UNKNOWN")) {
//                    pair_similarity = 0.0473348164509753; //average of DB
                    pair_similarity = 0.2; //boundary for no relatedness
                    System.out.println("Consulting " + ana_head.giveString() + "-n " + ante_head.giveString() + "-n");
                    if (ana_head.giveString().equals(ante_head.giveString())) {
                        pair_similarity = 1;
                    } else if (StrudelDatabase.getInstance().consultStrudelDB(ana_head.giveString() + "-n", ante_head.giveString() + "-n") < 0) {
//                System.out.println("Consulting " + ana_head.giveString() + "-n " + ante_head.giveString() + "-n");
                        pair_similarity = 0;
                    } else {
                        pair_similarity = StrudelDatabase.getInstance().consultStrudelDB(ana_head.giveString() + "-n", ante_head.giveString() + "-n");
//               System.out.println("Consulting " + ana_head.giveString() + "-n " + ante_head.giveString() + "-n");
                    }
                }
                if (pair_similarity > pair_similarity2) {
                    pair_similarity2 = pair_similarity;
                }
            }
            if (pair_similarity2 > similarity) {
                similarity = pair_similarity2;
            }
        }

        incompatibility = 1 - similarity;
        return incompatibility;
    }
}
     