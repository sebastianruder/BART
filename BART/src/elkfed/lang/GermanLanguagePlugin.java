/*
 *  Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
package elkfed.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.LanguagePlugin.TableName;
import elkfed.mmax.MarkableLevels;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.nlp.util.Gender;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author yannick
 * @author samuel
 */
public class GermanLanguagePlugin extends AbstractLanguagePlugin {

    // comparative particles and pre-determiner adverbs
    public static final String KOKOM_ADV = "als|wie|auch|besonders";
    public static final String PUNCT = "[,:;\\.`\'\"\\-\\)\\(]+";

    private static final Pattern left_unwanted = Pattern.compile(String.format("%s|%s", KOKOM_ADV, PUNCT), Pattern.CASE_INSENSITIVE);
    private static final Pattern right_unwanted = Pattern.compile(PUNCT);

    private static final Pattern attr_node = Pattern.compile("ADJX");
    private static final Pattern rel_node = Pattern.compile("PX|R-SIMPX");

    //HEURISTIC: determine for non ambiguous relativ pronouns gender or number 
    private static final Pattern pronoun_relativ_any_sing = Pattern.compile("der|dessen|dem|", Pattern.CASE_INSENSITIVE);
    //
    private static final Pattern pronoun_relativ_male_sing = Pattern.compile("den|welchem|welchen", Pattern.CASE_INSENSITIVE);
    //
    private static final Pattern pronoun_relativ_neut_sing = Pattern.compile("das|welches", Pattern.CASE_INSENSITIVE);
    //
    private static final Pattern pronoun_relativ_any_plur = Pattern.compile("denen", Pattern.CASE_INSENSITIVE);

    private final static Pattern pat_NP =
            Pattern.compile("NX", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_CN =
            Pattern.compile("NN", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PN =
            Pattern.compile("NE", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PRO =
            Pattern.compile("PIS|PPER|PDS|PRELS", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PP =
            Pattern.compile("PX", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PREP =
            Pattern.compile("APP[RO]|APZR", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_S =
            Pattern.compile("SIMPX|R-SIMPX", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_DT =
            Pattern.compile("ART|PIAT|PDAT", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_DT2 =
            Pattern.compile("PPOSAT|PIDAT", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_ADJ =
            Pattern.compile("ADJ[AD]", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_ADV =
            Pattern.compile("ADV", Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_CC =
            Pattern.compile("KON", Pattern.CASE_INSENSITIVE);

    public GermanLanguagePlugin() {
        readMapping(TableName.DemonymMap, "demonyms_de.txt");
        /* DISCLAIMER: All German lists are produced by running the
         * English ones through Google translate.
         */
        readList(animate_list, "animate_unigrams_de.txt");
        readList(inanimate_list, "inanimate_unigrams_de.txt");
        readList(neutral_list, "neutral_unigrams_de.txt");
        readList(male_list, "male_unigrams_de.txt");
        readList(female_list, "female_unigrams_de.txt");
        readList(stopword_list, "stopwords_de.txt");
        
    }
    
    public boolean unwanted_left(String tok) {
        return left_unwanted.matcher(tok).matches();
    }

    public boolean unwanted_right(String tok) {
        return right_unwanted.matcher(tok).matches();
    }

    @Override
    public List<Tree>[] calcParseInfo(Tree sentTree,
            int startWord, int endWord,
            MentionType markableType) {
        /* now, the *proper* way to do this would be to find the
         * head(s) and look for everything non-head. Instead,
         * we just look for children that look like modifiers,
         * which means that we get weird results
         * (i) for appositions
         * (ii) for elliptic NPs (e.g., 'la gialla'),
         *      where the head 'gialla' also gets recruited as a modifier
         */
        List<Tree>[] result = new List[3];
        List<Tree> projections = new ArrayList<Tree>();
        List<Tree> premod = new ArrayList<Tree>();
        List<Tree> postmod = new ArrayList<Tree>();
        result[0] = projections;
        result[1] = premod;
        result[2] = postmod;
        Tree node = calcLowestProjection(sentTree, startWord, endWord);
        projections.add(node);
        for (Tree n : node.children()) {
            String cat = n.value();
            if (attr_node.matcher(cat).matches()) {
                premod.add(n);
            } else if (rel_node.matcher(cat).matches()) {
                postmod.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeCategory labelCat(String cat) {
        if (pat_NP.matcher(cat).matches()) {
            return NodeCategory.NP;
        } else if (pat_CN.matcher(cat).matches()) {
            return NodeCategory.CN;
        } else if (pat_PN.matcher(cat).matches()) {
            return NodeCategory.PN;
        } else if (pat_PRO.matcher(cat).matches()) {
            return NodeCategory.PRO;
        } else if (pat_PP.matcher(cat).matches()) {
            return NodeCategory.PP;
        } else if (pat_PREP.matcher(cat).matches()) {
            return NodeCategory.PREP;
        } else if (pat_S.matcher(cat).matches()) {
            return NodeCategory.S;
        } else if (pat_DT.matcher(cat).matches()) {
            return NodeCategory.DT;
        } else if (pat_DT2.matcher(cat).matches()) {
            return NodeCategory.DT2;
        } else if (pat_ADJ.matcher(cat).matches()) {
            return NodeCategory.ADJ;
        } else if (pat_ADV.matcher(cat).matches()) {
            return NodeCategory.ADV;
        } else if (pat_CC.matcher(cat).matches()) {
            return NodeCategory.CC;
        } else {
            return NodeCategory.OTHER;
        }
    }

    @Override
    public void add_pronoun_features(String pron_type, MentionType result) {//FIXME wegen mmax conversion
        result.features.add(MentionType.Features.isPronoun);
        if (pron_type.equals("pro.refl")) {
            result.features.add(MentionType.Features.isReflexive);
        } else if (pron_type.equals("pro.per1") || pron_type.equals("pro.per2")) {
            result.features.add(MentionType.Features.isFirstSecondPerson);
            result.features.add(MentionType.Features.isPersPronoun);
        } else if (pron_type.equals("pro.per3") || pron_type.startsWith("pro.per")) {//FIXME ?
            result.features.add(MentionType.Features.isPersPronoun);
        } else if (pron_type.equals("pro.rel")) {
            result.features.add(MentionType.Features.isRelative);
        } else if (pron_type.equals("pro.dem")) {
            result.features.add(MentionType.Features.isDemonstrative);
        } else {
            throw new UnsupportedOperationException("Unknown pronoun type " + pron_type);
        }
    }

    @Override
    public MentionType calcMentionType(Markable markable) {//FIXME wegen mmax conversion
        String markable_type = markable.getAttributeValue(MARKABLE_TYPE);
        MentionType result = super.calcMentionType(markable);
        String mention_type = markable.getAttributeValue(MENTION_TYPE);
        String sem_type = markable.getAttributeValue(SEM_TYPE,"any");
        String headString = getHead(markable);

        if (markable_type.equals("poss")) {
            result.features.add(MentionType.Features.isPossPronoun);
        }

        if ("pro.rel".equals(mention_type)) {
            add_pronoun_features("pro.rel", result);
            result.gender = Gender.UNKNOWN;

            //FIXME: currently the mmax conversion has no gender/number guess for relative pronouns
            if (pronoun_relativ_any_sing.matcher(headString).matches()) {
                result.gender = Gender.UNKNOWN;
                result.features.add(MentionType.Features.isSingular);
            } else if (pronoun_relativ_male_sing.matcher(headString).matches()) {
                result.gender = Gender.MALE;
                result.features.add(MentionType.Features.isSingular);
            } else if (pronoun_relativ_neut_sing.matcher(headString).matches()) {
                result.gender = Gender.NEUTRAL;
                result.features.add(MentionType.Features.isSingular);
            } else if (pronoun_relativ_any_plur.matcher(headString).matches()) {
                result.gender = Gender.UNKNOWN;
                result.features.add(MentionType.Features.isPlural);
            }
//            System.err.format("FIXED mention_type=any: '%s' GENDER='%s'\n", markable.toString(), result.gender);
        }

        //FIXME: Current mmax conversion has mention_type=="any" for demonstrative pronouns
        if ("any".equals(mention_type)) {
            if (getHeadPOS(markable).matches(GermanLinguisticConstants.DEMONSTRATIVE_POS)) {
                add_pronoun_features("pro.dem", result);
            }
        }

        if ("nam".equals(mention_type)) {
            //HACK: In my current mmax conversion of TueBa-D/Z most of the persons
            //are assigned "nil" instead of MALE/FEMALE but have a gender. But not all 
            //"nil" are persons. As persons seldomly have a leading article the heuristic
            //is to assign MALE/FEMALE semantic class based on the gender
            if (sem_type.equalsIgnoreCase("nil") && !markable.getDiscourseElements()[0].toLowerCase().matches(GermanLinguisticConstants.LEADING_ARTICLE)) {
                if (result.gender == Gender.MALE) {
                    result.semanticClass = SemanticClass.MALE;
//                    System.out.format("NIL '%s' (first is '%s') is MALE \n", markableString(markable), markable.getDiscourseElements()[0]);
                }
                if (result.gender == Gender.FEMALE) {
                    result.semanticClass = SemanticClass.FEMALE;
//                    System.out.format("NIL '%s' (first is '%s') is FEMALE \n", markableString(markable), markable.getDiscourseElements()[0]);
                }
            }
        }

        return result;
    }

    /**
     * This overrides the default implementation to take care of
     * multi token heads in named entities
     *
     * @param markable
     * @return head string
     */
    @Override
    public String getHead(Markable markable) {
        String markable_type = markable.getAttributeValue(MARKABLE_TYPE);
        String mention_type = markable.getAttributeValue(MENTION_TYPE);
        if (markable_type.startsWith("v.")) {
            return markable.getAttributeValue(HEAD_WORD, "0");
        } else {
            String head_posId;
            int[] positions;
            MiniDiscourse doc = markable.getMarkableLevel().getDocument();
            MarkableLevel lemmas = doc.getMarkableLevelByName("lemma");

            //FIXME wegen mmax conversion
            if ("nam".equals(mention_type)) {

                MarkableLevel pos = doc.getMarkableLevelByName(MarkableLevels.DEFAULT_POS_LEVEL);

                head_posId = markable.getAttributeValue(HEAD_POS);
                String[] head_ranges = MarkableHelper.parseRanges(head_posId);
                int[] head_positions = doc.getPositions(head_ranges);

                if (!pos.getMarkablesAtDiscoursePosition(head_positions[0]).get(0).getAttributeValue("tag").toUpperCase().matches(GermanLinguisticConstants.PROPER_NOUN_POS)) {
                    head_posId = markable.getAttributeValue(HEAD_POS);
                    if (head_posId == null) {
                        return "*NULL*";
                    }
                    String[] ranges = MarkableHelper.parseRanges(head_posId);
                    positions = doc.getPositions(ranges);
                } else {

                    // get all adjacent NE tagged markables as well for the head string

                    TIntArrayList ne_positions = new TIntArrayList();

                    String min_posId = markable.getAttributeValue(MIN_IDS);
                    String[] ranges = MarkableHelper.parseRanges(min_posId);
                    int[] min_id_positions = doc.getPositions(ranges);

                    for (int posMarkable : min_id_positions) {
                        if (posMarkable >= head_positions[0]) {
                            if (pos.getMarkablesAtDiscoursePosition(posMarkable).get(0).getAttributeValue("tag").toUpperCase().matches(GermanLinguisticConstants.PROPER_NOUN_POS)) {
                                ne_positions.add(posMarkable);
                            } else {
                                break;
                            }
                        }
                    }
                    positions = ne_positions.toArray();
                }

            } else {
                head_posId = markable.getAttributeValue(HEAD_POS);
                if (head_posId == null) {
                    return "*NULL*";
                }
                try {
                	positions = new int[] {Integer.parseInt(head_posId)};
                } catch (NumberFormatException ex) {
                    String[] ranges = MarkableHelper.parseRanges(head_posId);
                    System.err.println("head range of "+markable.getID()+":"+Arrays.toString(ranges));
                    positions = doc.getPositions(ranges);                	
                }
            }

            StringBuffer buf = new StringBuffer();
            for (int pos : positions) {
                buf.append(' ');
                try {
                    buf.append(lemmas.getMarkablesAtDiscoursePosition(pos).get(0).getAttributeValue("tag"));
                } catch (Exception e) {
                    System.out.println(head_posId);
                    System.out.println(Arrays.toString(positions));
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            return buf.substring(1);
        }
    }

    @Override
    public SemanticClass getSemanticClass(String sem_type, String gender) {
        if (sem_type.equalsIgnoreCase("PER")) {
            if (gender.equalsIgnoreCase("fem")) {
                return SemanticClass.FEMALE;
            } else if (gender.equalsIgnoreCase("masc")) {
                return SemanticClass.MALE;
            } else {
                return SemanticClass.PERSON;
            }
        } else if (sem_type.equalsIgnoreCase("ORG")) {
            return SemanticClass.ORGANIZATION;
        } else if (sem_type.equalsIgnoreCase("LOC")) {
            return SemanticClass.LOCATION;
        } else if (sem_type.equalsIgnoreCase("TMP")) {
            return SemanticClass.DATE;
        } else if (sem_type.equalsIgnoreCase("EVT")) {
            return SemanticClass.EVENT;
        } else {
            return SemanticClass.UNKNOWN;
        }
    }

    @Override
    public boolean isExpletiveWordForm(String string) {
        return string.equalsIgnoreCase("es");
    }
    public Tree[] calcParseExtra(Tree sentenceTree,
            int startWord, int endWord, Tree prsHead,
            HeadFinder StHeadFinder) {
    	return new Tree[3];
    	
    
    }

}
