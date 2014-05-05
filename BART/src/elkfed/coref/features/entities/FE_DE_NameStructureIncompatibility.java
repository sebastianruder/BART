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

import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

import java.util.*;
import java.util.List;




public class FE_DE_NameStructureIncompatibility implements PairFeatureExtractor {

    public static final FeatureDescription<Boolean> FD_DE_NAMESTRINCOMPATIBILITY =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "DeNameStructureIncompatibility");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_DE_NAMESTRINCOMPATIBILITY);
    }

    public void extractFeatures(PairInstance inst) {
        //      if (inst.getAnaphor().getProperName() == true) {
        inst.setFeature(FD_DE_NAMESTRINCOMPATIBILITY, getNameStructureIncompatibility(inst));
    //    }
    }

    public boolean getNameStructureIncompatibility(PairInstance inst) {
        Boolean incompatibility = false;

        if (inst.getAnaphor().getProperName()) {
            if (inst.getAntecedent().getDiscourseEntity() != null && inst.getAnaphor().getDiscourseEntity() != null) {
                Integer counter = 0;

                DiscourseEntity de_ante = inst.getAntecedent().getDiscourseEntity();
                DiscourseEntity de_ana = inst.getAnaphor().getDiscourseEntity();

                HashMap<String, Set<Property>> de_ante_names = de_ante.getNames();
                HashMap<String, Set<Property>> de_ana_names = de_ana.getNames();

                if (de_ante_names.get("Role") != null &&
                        de_ana_names.get("Role") != null) {
//            System.out.println("Roles1");
                    if (((de_ante_names.get("Role").contains("mr.") ||
                            de_ante_names.get("Role").contains("sir")) &&
                            (de_ana_names.get("Role").contains("madam") ||
                            de_ana_names.get("Role").contains("miss") ||
                            de_ana_names.get("Role").contains("mrs") ||
                            de_ana_names.get("Role").contains("lady"))) ||
                            /* check for gender compatibility */
                            ((de_ana_names.get("Role").contains("mr.") ||
                            de_ana_names.get("Role").contains("sir")) &&
                            (de_ante_names.get("Role").contains("madam") ||
                            de_ante_names.get("Role").contains("miss") ||
                            de_ante_names.get("Role").contains("mrs") ||
                            de_ante_names.get("Role").contains("lady")))) {
//                incompatibility = true;
                        counter++;
                        System.out.println("Incompatibility: Role");
                    }
                }
                if (de_ante_names.get("Forename") != null && de_ana_names.get("Forename") != null &&
                        de_ante_names.get("Forename").size() > 0) {
//            System.out.println("Forename1");
                    for (Property ana_names_fn : de_ana_names.get("Forename")) {
                        System.out.println("Checking FN " + ana_names_fn.toString());
                        for (Property de_ante_name : de_ante_names.get("Forename")) {
                            System.out.println("DE-ANTE-FORE: " + de_ante_name.toString());
                        }
                        if (!(de_ante_names.get("Forename").contains(ana_names_fn))) {
                            counter++;
                            //incompatibility = true;
                            System.out.println("Incompatibility: Forename");
                            break;
                        }
                    }
                }
                if (de_ante_names.get("Surname") != null && de_ana_names.get("Surname") != null &&
                        de_ante_names.get("Surname").size() > 0) {
//            System.out.println("Surname1");
                    for (Property ana_names_sn : de_ana_names.get("Surname")) {
                        System.out.println("Checking SN " + ana_names_sn.toString());
                        for (Property de_ante_name : de_ante_names.get("Surname")) {
                            System.out.println("DE-ANTE-SURN: " + de_ante_name.toString());
                        }
                        if (!(de_ante_names.get("Surname").contains(ana_names_sn))) {
                            counter++;
                            System.out.println("Incompatibility: Lastname");
                            break;
                        }
                    }
                }

                if (de_ante_names.get("Surname") != null && de_ana_names.get("Surname") != null &&
                        de_ante_names.get("Surname").size() > 0) {
//            System.out.println("Surname1");
                    for (Property ana_names_sn : de_ana_names.get("Surname")) {
                        System.out.println("Checking SN " + ana_names_sn.toString());
                        for (Property de_ante_name : de_ante_names.get("Surname")) {
                            System.out.println("DE-ANTE-SURN: " + de_ante_name.toString());
                        }
                        if (!(de_ante_names.get("Surname").contains(ana_names_sn))) {
                            counter++;
                            System.out.println("Incompatibility: Lastname");
                            break;
                        }
                    }
                }

                // introduce atomic name incompatibility
                if (de_ana_names.get("AtomicName") != null) {
                    if (de_ante_names.get("Role") != null ||
                            de_ante_names.get("Forename") != null ||
                            de_ante_names.get("Surname") != null) {
                        counter++;
                    }
                    if (de_ante_names.get("AtomicName") != null) {
                        for (Property ana_names_an : de_ana_names.get("AtomicName")) {
                            System.out.println("Checking AN " + ana_names_an.toString());
                            for (Property de_ante_name : de_ante_names.get("AtomicName")) {
                                System.out.println("DE-ANTE-AN: " + de_ante_name.toString());
                            }
                            if (!(de_ante_names.get("AtomicName").contains(ana_names_an))) {
                                counter++;
                                System.out.println("Incompatibility: Atomic");
                                break;
                            }
                        }
                    }
                }


                if (counter > 0) {
                    incompatibility = true;
                    System.out.println("INCOMPATIBILITY FOUND!!!");
                }


            }
        }
        return incompatibility;
    }
}
        
        
