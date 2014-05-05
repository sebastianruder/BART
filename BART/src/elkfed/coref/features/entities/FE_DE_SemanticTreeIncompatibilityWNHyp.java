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
import elkfed.coref.features.pairs.*;

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;

import edu.brandeis.cs.steele.wn.IndexWord;
import edu.brandeis.cs.steele.wn.PointerTarget;
import edu.brandeis.cs.steele.wn.PointerType;
import edu.brandeis.cs.steele.wn.Synset;
import elkfed.knowledge.WNInterface;
import java.util.HashSet;
import java.util.Set;

//For the StrMatch
public class FE_DE_SemanticTreeIncompatibilityWNHyp implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_SEMANTICTREECOMPATIBILITY_WNHYP =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeSemanticTreeIncompatibilityWNHYP");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_SEMANTICTREECOMPATIBILITY_WNHYP);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_DE_SEMANTICTREECOMPATIBILITY_WNHYP, getDESemanticTreeIncompatibilityWNHyp(inst));
    }

    public boolean getDESemanticTreeIncompatibilityWNHyp(PairInstance inst) {
        boolean incomp = false;
        Integer incompatibility = 0;
//        if (inst.getAntecedent().getDiscourseEntity() != null && inst.getAnaphor().getDiscourseEntity() != null) {
        if (inst.getAnaphor().getDefinite() || inst.getAnaphor().getIndefinite() || inst.getAnaphor().getPronoun()) {

            DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
            DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

            Set<Property> de_ante_att = de_ante.getAttributes();
            Set<Property> de_ana_att = de_ana.getAttributes();

            Set<Property> de_ante_rel = de_ante.getRelations();
            Set<Property> de_ana_rel = de_ana.getRelations();

            Set<Property> de_ante_heads = de_ante.getTypes();
            Set<Property> de_ana_heads = de_ana.getTypes();


            if (inst.getAnaphor().getPronoun() &&
                    (inst.getAnaphor().getSemanticClass() != inst.getAntecedent().getSemanticClass())) {
                incompatibility++;
            }


            for (Property ana_head : de_ana_heads) {
                int synhyp = 0;
                int a;
                if (!de_ante_heads.contains(ana_head)) {
                    for (Property ante_head : de_ante_heads) {
                        if (inst.getAntecedent().getProperName()) {
                            a = isHypernym(ante_head.getPredicate(), ana_head.getPredicate());
                        } else {
                            a = isHypernym(ana_head.getPredicate(), ante_head.getPredicate());
                        }


//                        int a = isSynonymOrHypernym(ante_head.getPredicate(), ana_head.getPredicate());
//                        int b = isSynonymOrHypernym(ana_head.getPredicate(), ante_head.getPredicate());

                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
                        System.out.println(">>\t" + "ana: " + ana_head.getPredicate());
                        System.out.println(">>\t" + "ant: " + ante_head.getPredicate());
//                        System.out.println(">>\t" + "a:" + a + "  b:" + b);
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");

                        if (a == 1) {
                            synhyp = 1;
                        }
                    //                         System.out.println(">> COMPATIBLE");
                    //                         System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
                    //                         break;
                    //                     }
                    }
                    if (synhyp == 0) {
                        incompatibility++;
//                        System.out.println(">> INCOMPATIBLE");
//                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
                        break;
                    }
                }
            }

            if (de_ante_att.size() > 0 && de_ana_att.size() > 0) {
                for (Property ana_att : de_ana_att) {
                    if (!(de_ante_att.contains(ana_att))) {
                        incompatibility++;
                        break;
                    }
                }
            }
            if (de_ante_rel.size() > 0 && de_ana_rel.size() > 0) {
                for (Property ana_rel : de_ana_rel) {
                    for (Property ante_rel : de_ante_rel) {
                        if (ana_rel.getPredicate().toString().equals(ante_rel.getPredicate().toString()) &&
                                !(ana_rel.getArgument().toString().equals(ante_rel.getArgument().toString()))) {
                            incompatibility++;
                            break;
                        }
                    }
                }
            }
        }
        if (incompatibility > 0) {
            incomp = true;
        }
        return incomp;
    }

    public static int isHypernym(String head1, String head2) {
        WNInterface wn = WNInterface.getInstance();
        IndexWord word1 = wn.lookupNoun(head1);
        IndexWord word2 = wn.lookupNoun(head2);
        Set<Synset> synsets1 = new HashSet<Synset>();
        if (word1 == null || word2 == null) {
            return 0;
        } else {

            for (Synset s : word1.getSenses()) {
                synsets1.add(s);
            }

            HashSet<PointerTarget> visited = new HashSet<PointerTarget>();
            for (Synset s : word2.getSenses()) {
                if (wn.reachable(s, PointerType.HYPERNYM, synsets1, visited)) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
     