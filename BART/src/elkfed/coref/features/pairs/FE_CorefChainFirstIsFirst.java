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
import elkfed.mmax.util.NPHeadFinder;
import elkfed.knowledge.SemanticClass;

import elkfed.util.DateParser;


//For the StrMatch
import elkfed.mmax.minidisc.Markable;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.lang.EnglishLinguisticConstants.*;

/**
 * Extract coref chain features
 *
 * @author massimo
 */
public class FE_CorefChainFirstIsFirst implements PairFeatureExtractor {

    public static final FeatureDescription<Integer> FD_I_CC_LENGTH =
            new FeatureDescription<Integer>(FeatureType.FT_SCALAR, "ante_corefchain_length");
    public static final FeatureDescription<Boolean> FD_I_CC_FIRST_IS_FIRST =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_first_is_first");
    public static final FeatureDescription<Boolean> FD_I_CC_FIRST_IS_PN =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_first_is_pn");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_FIRST =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "ante_corefchain_any_is_first");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_GENDER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_is_gender");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_ARE_GENDER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_are_gender");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_NUMBER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_is_number");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_ARE_NUMBER =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_are_number");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_STRING_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_string_match");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_STRING_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_string_match");
    public static final FeatureDescription<Boolean> FD_I_CC_ALL_NonPro_STRING_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "all_nonpro_string_match");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_NonPro_STRING_MATCH =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_nonpro_string_match");
    public static final FeatureDescription<Boolean> FD_I_CC_SEM_CLASS_COMPAT =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "sem_class_compat");
    public static final FeatureDescription<Boolean> FD_I_CC_ANY_IS_ALIAS =
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "any_is_alias");

    public void describeFeatures(List<FeatureDescription> fds) {
//        fds.add(FD_I_CC_LENGTH);
        fds.add(FD_I_CC_FIRST_IS_FIRST);
//        fds.add(FD_I_CC_FIRST_IS_PN);
//        fds.add(FD_I_CC_ANY_IS_FIRST);
//        fds.add(FD_I_CC_ANY_IS_GENDER);
//      //      fds.add(FD_I_CC_ALL_ARE_GENDER);
//     fds.add(FD_I_CC_ANY_IS_NUMBER);
      //    fds.add(FD_I_CC_ALL_ARE_NUMBER);
        //     fds.add(FD_I_CC_ALL_STRING_MATCH);
        //    fds.add(FD_I_CC_ANY_STRING_MATCH);
        //     fds.add(FD_I_CC_ALL_NonPro_STRING_MATCH);
        //     fds.add(FD_I_CC_ANY_NonPro_STRING_MATCH);
//       fds.add(FD_I_CC_SEM_CLASS_COMPAT);
     //  fds.add(FD_I_CC_ANY_IS_ALIAS);

    }

    public void extractFeatures(PairInstance inst) {
        // ante
        inst.setFeature(FD_I_CC_LENGTH, getChainLength(inst));
        inst.setFeature(FD_I_CC_FIRST_IS_FIRST,
                inst.getAntecedent().getDiscourseEntity().firstMention_isFirstMention());
        inst.setFeature(FD_I_CC_FIRST_IS_PN,
                inst.getAntecedent().getDiscourseEntity().firstMention_isProperName());
        inst.setFeature(FD_I_CC_ANY_IS_FIRST,
                inst.getAntecedent().getDiscourseEntity().anyMention_isFirstMention());

        inst.setFeature(FD_I_CC_ANY_IS_GENDER, getAnyIsGender(inst));
        inst.setFeature(FD_I_CC_ALL_ARE_GENDER, getAllAreGender(inst));
        inst.setFeature(FD_I_CC_ANY_IS_NUMBER, getAnyIsNumber(inst));
        inst.setFeature(FD_I_CC_ALL_ARE_NUMBER, getAllAreNumber(inst));
        inst.setFeature(FD_I_CC_ALL_STRING_MATCH, getAllStrMatch(inst));
        inst.setFeature(FD_I_CC_ANY_STRING_MATCH, getAnyStrMatch(inst));
        //     inst.setFeature(FD_I_CC_ALL_NonPro_STRING_MATCH, getAllNoPronStrMatch(inst));
        //     inst.setFeature(FD_I_CC_ANY_NonPro_STRING_MATCH, getAnyNoPronStrMatch(inst));
     //   inst.setFeature(FD_I_CC_SEM_CLASS_COMPAT, getSemClassCompat(inst));
     //   inst.setFeature(FD_I_CC_ANY_IS_ALIAS, anyIsAlias(inst));

    }

    /**
     **/
    public Integer getChainLength(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                return cc.size();
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
                return 0;
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
            return 0;
        }
    }

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

    public Boolean getAllStrMatch(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        int matchString = 0;
        int sizeCorefChain = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            sizeCorefChain = cc.size();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (getMarkableString(cc.get(idx).getMarkable()).
                            equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable()))) {
                        matchString++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchString == sizeCorefChain) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAnyStrMatch(PairInstance inst) {
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        int matchString = 0;
        int sizeCorefChain = 0;
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            sizeCorefChain = cc.size();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    if (getMarkableString(cc.get(idx).getMarkable()).
                            equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable()))) {
                        matchString++;
                        idx++;
                    } else {
                        idx++;
                    }
                }
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (matchString > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAllNoPronStrMatch(PairInstance inst) {
        int matchString = 0;
        int sizeCorefChain = 0;
        int numberNoPron = 0;
        if (!inst.getAnaphor().getPronoun()) {
            DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
            if (de != null) {
                Vector<Mention> cc = de.getcorefChain();
                sizeCorefChain = cc.size();
                if (cc != null) {
                    int idx = 0;
                    while (idx < cc.size()) {
                        if (!cc.get(idx).getPronoun()) {
                            numberNoPron++;
                            if (getMarkableString(cc.get(idx).getMarkable()).
                                    equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable()))) {
                                idx++;
                                matchString++;
                                numberNoPron++;
                            } else {
                                idx++;
                                numberNoPron++;
                            }
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
        } else {
        }
        if (matchString > 0 && matchString == numberNoPron) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getAnyNoPronStrMatch(PairInstance inst) {
        int matchString = 0;
        int sizeCorefChain = 0;
        int numberNoPron = 0;
        if (!inst.getAnaphor().getPronoun()) {
            DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
            if (de != null) {
                Vector<Mention> cc = de.getcorefChain();
                sizeCorefChain = cc.size();
                if (cc != null) {
                    int idx = 0;
                    while (idx < cc.size()) {
                        if (!cc.get(idx).getPronoun()) {
                            numberNoPron++;
                            if (getMarkableString(cc.get(idx).getMarkable()).
                                    equalsIgnoreCase(getMarkableString(inst.getAnaphor().getMarkable()))) {
                                idx++;
                                matchString++;
                                numberNoPron++;
                            } else {
                                idx++;
                                numberNoPron++;
                            }
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
        } else {
        }
        if (matchString > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean getSemClassCompat(PairInstance inst) {
        int compatYes = 0;
        int compatNo = 0;
        int compatUnknown = 0;
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    //if semantic class of anaphor or member of coref chain is unknown
                    if (inst.getAnaphor().getSemanticClass().equals(SemanticClass.UNKNOWN) ||
                            cc.get(idx).getSemanticClass().equals(SemanticClass.UNKNOWN)) {
                        System.out.println("  ==> CHECKPOINT 1: 1 " + inst.getAnaphor().getSemanticClass() + " 2 " + cc.get(idx).getSemanticClass());
                        if (NPHeadFinder.getInstance().getHeadLemma(inst.getAnaphor().getMarkable()).
                                equals(NPHeadFinder.getInstance().getHeadLemma(cc.get(idx).getMarkable()))) {
                            idx++;
                            compatYes++;
                            System.out.println("  ==> CHECKPOINT 2");
                        } else {
                            idx++;
                            compatUnknown++;
                            compatNo++;
                            System.out.println("  ==> CHECKPOINT 3");
                        }
                    } //if both semantic classes are the same or one is the parent
                    //of the other.
                    else if ((SemanticClass.isaPerson(inst.getAnaphor().getSemanticClass()) &&
                            SemanticClass.isaPerson(cc.get(idx).getSemanticClass())) ||
                            (SemanticClass.isaObject(inst.getAnaphor().getSemanticClass()) &&
                            SemanticClass.isaObject(cc.get(idx).getSemanticClass())) /*                       ||
                            (SemanticClass.isaOrganization(inst.getAnaphor().getSemanticClass())
                            &&
                            SemanticClass.isaOrganization(cc.get(idx).getSemanticClass()))
                            ||
                            (SemanticClass.isaLocation(inst.getAnaphor().getSemanticClass())
                            &&
                            SemanticClass.isaLocation(cc.get(idx).getSemanticClass()))
                            ||
                            (SemanticClass.isaTimex(inst.getAnaphor().getSemanticClass())
                            &&
                            SemanticClass.isaTimex(cc.get(idx).getSemanticClass()))
                            ||
                            (SemanticClass.isaNumerical(inst.getAnaphor().getSemanticClass())
                            &&
                            SemanticClass.isaNumerical(cc.get(idx).getSemanticClass()))
                           */ ) {
                        System.out.println("  ==> CHECKPOINT 4: 1" + inst.getAnaphor().getSemanticClass() + " 2 " + cc.get(idx).getSemanticClass());
                        idx++;
                        compatYes++;
                    } else {
                        idx++;
                        compatNo++;
                        System.out.println("  ==> CHECKPOINT 5");
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
//        if ((compatYes > 0) && (compatNo == 0)) {
        if (compatYes > 0) {

            return true;
        } else {
            return false;
        }
    }

    public Boolean anyIsAlias(PairInstance inst) {
        int isAlias = 0;
        int isNoAlias = 0;
        DiscourseEntity de = inst.getAntecedent().getDiscourseEntity();
        if (de != null) {
            Vector<Mention> cc = de.getcorefChain();
            if (cc != null) {
                int idx = 0;
                while (idx < cc.size()) {
                    final Date antDate =
                            DateParser.getInstance().parse(cc.get(idx).getMarkableString());
                    final Date anaDate =
                            DateParser.getInstance().parse(inst.getAnaphor().getMarkableString());
                    if (antDate != null && anaDate != null) {
                        // normalize date String and compare
                        boolean compDate = compareDate(antDate, anaDate);
                        if (compDate == true) {
                            isAlias++;
                        }
                        idx++;
                    } else {
                        final String enamexClass = cc.get(idx).getEnamexType();
                        if (enamexClass.equals("person")) {
                            // compare last token
                            boolean compName = compareName(inst);
                            if (compName == true) {
                                isAlias++;
                            }
                        } else if (enamexClass.equals("organization")) {
                            // form acronyms and compare
                            boolean compOrg = compareName(inst);
                            if (compOrg == true) {
                                isAlias++;
                            }
                        } else if (enamexClass.equals("location")) {
                            // form acronyms and compare or starts with check
                            boolean compLoc = compareName(inst);
                            if (compLoc == true) {
                                isAlias++;
                            }
                        } else {
                            // for the others just compare the strings
                            if (cc.get(idx).getMarkableString().toLowerCase().
                                    equalsIgnoreCase(inst.getAnaphor().getMarkableString())) {
                                isAlias++;
                            }
                        }
                        idx++;
                    }
                }
            } else {
                System.out.println("Null coref chain  for DE " + de.getId());
            }
        } else {
            System.out.println("Null discourse entity for mention " + inst.getAntecedent().getHeadString());
        }
        if (isAlias > 0) {
            return true;
        } else {
            return false;
        }
    }


// Methodes from FE_Alias.java and ...    
    protected String getMarkableString(final Markable markable) {
        final String[] tokens = markable.getDiscourseElements();
        final String[] pos =
                markable.getAttributeValue(DEFAULT_POS_LEVEL).split(" ");
        final StringBuffer clean = new StringBuffer();

        // if it's just one token there is nothing to remove
        // (e.g. demonstrative pronouns)
        if (tokens.length > 1) {
            for (int token = 0; token < tokens.length; token++) {
                if (!tokens[token].toLowerCase().matches(ARTICLE) &&
                        !tokens[token].toLowerCase().matches(DEMONSTRATIVE) &&
                        !tokens[token].toLowerCase().matches(PUNCTUATION_MARK) &&
                        !tokens[token].toLowerCase().matches(SAXON_GENITIVE) &&
                        !pos[token].toLowerCase().matches(DETERMINER_POS)) {
                    clean.append(" ").append(tokens[token]);
                }
            }
            try {
                return clean.deleteCharAt(0).toString();
            } catch (StringIndexOutOfBoundsException e) {
                // insane exception handling... this is to take
                // of an expression such as "The A"... NLP sucks!
                return "";
            }
        } else {
            return tokens[0];
        }
    }

    private static boolean compareDate(Date date1, Date date2) {
        final GregorianCalendar cal1 = new GregorianCalendar();
        cal1.setTime(date1);
        final GregorianCalendar cal2 = new GregorianCalendar();
        cal2.setTime(date2);

        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) ||
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    /** Uses a date parser to normalize, extract and compare two date String */
    private static boolean compareName(PairInstance inst) {
        final String[] antecedentTokens = inst.getAntecedent().getMarkable().getDiscourseElements();
        final String[] anaphoraTokens = inst.getAnaphor().getMarkable().getDiscourseElements();
        if (antecedentTokens[antecedentTokens.length - 1].equalsIgnoreCase(anaphoraTokens[anaphoraTokens.length - 1])) {
            return true; // instance.setFeature(feature, Boolean.T.getInt()); 
        } else {
            return false; // instance.setFeature(feature, Boolean.F.getInt()); 
        }
    }

    /** Just an alias */
    private static boolean compareOrg(PairInstance inst) {
        // first check for an abbreviation
        if (isAbbreviation(inst)) {
            return true;
        } else if ( //                    !inst.getAntecedent().getMarkableString().equalsIgnoreCase(
                //                      inst.getAnaphor().getMarkableString())
                //                &&
                orgStartsWith(
                inst.getAntecedent().getMarkableString().toLowerCase(),
                inst.getAnaphor().getMarkableString().toLowerCase())) {
            return true;
        }
        return false;
    }

    /** From Soon et Al.(2001): For organization names, the alias function also
     *  checks for acronym match such as IBM and International Business Machines
     *  Corp. In this case, the longer string is chosen to be the one that is
     *  converted into the acronym form. The first step is to remove all
     *  postmodifiers such as Corp. and Ltd. Then, the acronym function
     *  considers each word in turn, and if the first letter is capitalized, it
     *  is used to form the acronym. Two variations of the acronyms are
     *  produced: one with a period after each letter, and one without.
     *
     *  @return whether a match was found
     */
    private static boolean isAbbreviation(PairInstance inst) {
        // first check they differ just by periods --- i.e. "IBM" and "I.B.M."
        if ( //                !inst.getAntecedent().getMarkableString().equalsIgnoreCase
                //                    (inst.getAnaphor().getMarkableString())
                //            &&
                (inst.getAntecedent().getMarkableString().replaceAll("\\.", "").
                equalsIgnoreCase(inst.getAnaphor().getMarkableString()) ||
                inst.getAnaphor().getMarkableString().replaceAll("\\.", "").
                equalsIgnoreCase(inst.getAntecedent().getMarkableString()))) {
            return true; //instance.setFeature(feature, Boolean.T.getInt()); return true; 
        } // else acronyms! First generate the acronym from the longer string
        else if (inst.getAntecedent().getMarkableString().length() >
                inst.getAnaphor().getMarkableString().length()) {
            // antecedent is longer: generate from antecedent
            final String[] acronyms = getAcronym(inst.getAntecedent());

            // does the anaphora match one of the acronyms
            if (acronyms[0].equalsIgnoreCase(inst.getAnaphor().getMarkableString()) ||
                    acronyms[1].equalsIgnoreCase(inst.getAnaphor().getMarkableString()) ||
                    acronyms[2].equalsIgnoreCase(inst.getAnaphor().getMarkableString())) {
                return true; // instance.setFeature(feature, Boolean.T.getInt()); return true; 
            } else {
                return false; // instance.setFeature(feature, Boolean.F.getInt()); return false; 
            }
        } else if (inst.getAntecedent().getMarkableString().length() <
                inst.getAnaphor().getMarkableString().length()) {
            // anaphora is longer: generate from anaphora
            final String[] acronyms = getAcronym(inst.getAnaphor());

            // does the anaphora match one of the acronyms
            if (acronyms[0].equalsIgnoreCase(inst.getAntecedent().getMarkableString()) ||
                    acronyms[1].equalsIgnoreCase(inst.getAntecedent().getMarkableString()) ||
                    acronyms[2].equalsIgnoreCase(inst.getAntecedent().getMarkableString())) {
                return true; // instance.setFeature(feature, Boolean.T.getInt()); return true; 
            } else {
                return false; // instance.setFeature(feature, Boolean.F.getInt()); return false; 
            }
        }
        // instance.setFeature(feature, Boolean.F.getInt()); 
        return false;
    }

    /** For a location to be an alias of another, either one is an abbreviation of
     *  the other --- i.e. "NJ" and "N.J." --- or one starts with the other 
     *  --- i.e. "California" and "Calif." or "Washington" and "Washington, D.C.".
     */
    private static boolean compareLoc(PairInstance inst) {
        // first check for an abbreviation
        if (isAbbreviation(inst)) {
            return true;
        } // no luck: check whether one starts with the other
        else if ( //                    !inst.getAntecedent().getMarkableString().equalsIgnoreCase(
                //                        inst.getAnaphor().getMarkableString())
                //                &&
                startsWith(
                inst.getAntecedent().getMarkableString().toLowerCase(),
                inst.getAnaphor().getMarkableString().toLowerCase())) {
            return true;
        }
        return false;
    }

    /** Check whether one LOC NE starts with the other */
    private static boolean startsWith(String ne1, String ne2) {
        return startsWith(ne1, ne2, "\\.");
    }

    /** Check whether one ORG NE starts with the other */
    private static boolean orgStartsWith(String ne1, String ne2) {
        return startsWith(ne1, ne2, COMPANY_DESIGNATOR);
    }

    /** Check whether one ORG NE starts with the other */
    private static boolean startsWith(String ne1, String ne2, String toRemove) {
        return ne1.replaceAll(toRemove, "").
                startsWith(ne2) ||
                ne2.replaceAll(toRemove, "").
                startsWith(ne1);
    }

    /** Creates 3 acronyms:
     *
     *  1. all tokens, w/o company designator --- i.e. Home Depot Inc. / Home Depot
     *  2. all caps w/o period --- Intelligent Business Machines / IBM
     *  3. all caps w period --- Intelligent Business Machines / I.B.M.
     */
    private static String[] getAcronym(final Mention mention) {
        final StringBuffer firstAcronym = new StringBuffer();
        final StringBuffer secondAcronym = new StringBuffer();
        final StringBuffer thirdAcronym = new StringBuffer();

        final String[] tokens = mention.getMarkableString().split(" ");

        if (tokens.length == 0) {
            throw new RuntimeException("Tokens empty:" + mention.getMarkableString() + " " + mention.toString());
        }
        for (int token = 0; token < tokens.length; token++) {
            if (!tokens[token].toLowerCase().matches(COMPANY_DESIGNATOR)) {
                firstAcronym.append(tokens[token]).append(" ");
                if (Character.isUpperCase(tokens[token].charAt(0))) {
                    secondAcronym.append(tokens[token].substring(0, 1));
                    thirdAcronym.append(tokens[token].substring(0, 1)).append(".");
                } // handle ampersand...
                else if (tokens[token].equals("&")) {
                    secondAcronym.append(" & ");
                    thirdAcronym.append(" & ");
                }
            }
        }
        if (firstAcronym.length() == 0) {
            System.err.println("firstAcronym empty:" + mention.getMarkableString() + " " + mention.toString());
        } else {
            firstAcronym.deleteCharAt(firstAcronym.length() - 1);
        }
        final String[] acronyms = {
            firstAcronym.toString(),
            secondAcronym.toString(),
            thirdAcronym.toString()
        };
        return acronyms;
    }
}
