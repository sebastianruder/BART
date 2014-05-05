/*
 * Copyright 2007 EML Research
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
package elkfed.coref.eval;

import elkfed.config.ConfigProperties;
import elkfed.mmax.util.CorefDocuments;
import elkfed.mmax.Corpus;
import elkfed.mmax.DiscourseUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import elkfed.mmax.minidisc.Markable;

import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.HashMap;
import static elkfed.mmax.MarkableLevels.*;

/** Contains helper classes and methods for coreference evaluation
 *
 * @author ponzetsp
 */
public class EvaluationHelper {

    /** Creates a new instance of EvaluationHelper.
     *  Private constructor as this class contains only static methods.
     */
    private EvaluationHelper() {
    }

    /** Gets the equivalence classes from the key markable level */
    public static EvaluationMarkableSet[] getKeyPartition(final Corpus corpus) {
        return getPartition(corpus, DEFAULT_COREF_LEVEL);
    }

    /** Gets the equivalence classes per-document from the key markable level */
    public static EvaluationMarkableSet[][] getDocumentClusteredKeyPartition(final Corpus corpus) {
        return getDocumentClusteredPartition(corpus, DEFAULT_COREF_LEVEL);
    }

    /** Gets the equivalence classes from the "response" markable level */
    public static EvaluationMarkableSet[] getResponsePartition(final Corpus corpus) {
        return getPartition(corpus, DEFAULT_RESPONSE_LEVEL);
    }

    /** Gets the equivalence classes per-document from the "response" markable level */
    public static EvaluationMarkableSet[][] getDocumentClusteredResponsePartition(final Corpus corpus) {
        return getDocumentClusteredPartition(corpus, DEFAULT_RESPONSE_LEVEL);
    }

    /** Gets the equivalence classes from a given markable level name */
    public static EvaluationMarkableSet[] getPartition(
            final Corpus corpus, final String markableLevelName) {
        final ArrayList<EvaluationMarkableSet> partition = new ArrayList<EvaluationMarkableSet>();
        // for each document
        for (MiniDiscourse document : corpus) {
            final List<List<Markable>> docPartition =
                    document.getMarkableLevelByName(markableLevelName).
                    getGroupedBy(COREF_SET_ATTRIBUTE);
            // roll our own data structure
            for (List<Markable> eqClass : docPartition) {
                partition.add(new EvaluationMarkableSet(eqClass, document));
            }
        }
        return (EvaluationMarkableSet[]) partition.toArray(new EvaluationMarkableSet[partition.size()]);
    }

    /**
     * Balances key and response so that every coreference set in the key
     * contains also the overlapping mentions as singletons that are
     * present in the response but not in the key and vice versa.
     * This is required for an appropriate computation of the CEAF score
     * with the CEAFAggrScorer implementation w/o perfect mentions. Enable
     * its usage by setting "balanceKeyAndResponse=true" in config.properties
     *
     * @author samuel
     * @param corpus
     */
    public static void balanceKeyAndResponse(Corpus corpus) {

        int corefset = 0;

        for (MiniDiscourse document : corpus) {

            MarkableLevel corefLevel = document.getMarkableLevelByName(DEFAULT_COREF_LEVEL);
            MarkableLevel responseLevel = document.getMarkableLevelByName(DEFAULT_RESPONSE_LEVEL);

            HashSet<String> seenMarkables = new HashSet<String>();

            ArrayList<Markable> responseLevelMarkables = new ArrayList<Markable> (responseLevel.getMarkables());
            ArrayList<Markable> corefLevelMarkables = new ArrayList<Markable> (corefLevel.getMarkables());

            responseMarkableLoop:
            for (Markable markable : responseLevelMarkables) {

                seenMarkables.add(markable.getAttributeValue("min_ids"));

                corefMarkableLoop:
                for (Markable corefMarkable : corefLevelMarkables){
                    if (corefMarkable.getAttributeValue("min_ids") != null && corefMarkable.getAttributeValue("min_ids").equals(markable.getAttributeValue("min_ids"))) {
                        continue responseMarkableLoop;
                    } else if (markable.getRightmostDiscoursePosition() <  corefMarkable.getLeftmostDiscoursePosition()) {
                        break corefMarkableLoop;
                    }
                }

//                System.out.println("ADDING " + markable + " to coref level");
                HashMap<String, String> attr = new HashMap<String, String>();
                attr.put(COREF_SET_ATTRIBUTE, "balancedset_" + Integer.toString(corefset++));
                if(markable.getAttributeValue("min_ids") != null) {
                attr.put("min_ids", markable.getAttributeValue("min_ids"));
                }
                corefLevel.addMarkable(markable.getLeftmostDiscoursePosition(), markable.getRightmostDiscoursePosition(), attr);
            }

            corefMarkableLoop:
            for (Markable markable : corefLevelMarkables) {

                if (seenMarkables.contains(markable.getAttributeValue("min_ids"))) {
                    continue corefMarkableLoop;
                }

//                System.out.println("ADDING " + markable + " to response level");
                HashMap<String, String> attr = new HashMap<String, String>();
                attr.put(COREF_SET_ATTRIBUTE, "balancedset_" + Integer.toString(corefset++));
                if(markable.getAttributeValue("min_ids") != null) {
                    attr.put("min_ids", markable.getAttributeValue("min_ids"));
                }
                responseLevel.addMarkable(markable.getLeftmostDiscoursePosition(), markable.getRightmostDiscoursePosition(), attr);

            }

            if (responseLevel.getMarkables().size() == corefLevel.getMarkables().size()) {
                responseLevel.saveMarkables();
            } else {
                throw new RuntimeException("response level ("  + responseLevel.getMarkables().size() +  ") and coref level ("  + corefLevel.getMarkables().size() +  ") not successfully balanced");
            }
        }
    }

    /**
     * Gets the equivalence classes from a given markable level name.
     * By setting the option "addSingletonsToKey=true" in config.properties
     * the singletons from the response will be added to the coref level
     * so the partitions of key and response contain the same set of mentions
     * This is required for an appropriate computation of the CEAF score
     * with the CEAFAggrScorer implementation w/o perfect mentions.
     * 
     * @param corpus
     * @param markableLevelName
     * @return partioned document
     */
    public static EvaluationMarkableSet[][] getDocumentClusteredPartition(
            final Corpus corpus, final String markableLevelName) {
        final ArrayList<EvaluationMarkableSet[]> partition = new ArrayList<EvaluationMarkableSet[]>();
        // for each document
        for (MiniDiscourse document : corpus) {
            if (ConfigProperties.getInstance().getAddSingletons() &&
                    markableLevelName.equalsIgnoreCase(DEFAULT_COREF_LEVEL)) {
                int corefset = 0;
                MarkableLevel corefLevel = document.getMarkableLevelByName(DEFAULT_COREF_LEVEL);
                markableLoop:
                for (Markable markable : document.getMarkableLevelByName(DEFAULT_MARKABLE_LEVEL).getMarkables()) {
                    for (Markable corefMarkable : corefLevel.getMarkablesAtDiscourseElementID(markable.getDiscourseElementIDs()[0])) {
                        if (corefMarkable.getAttributeValue("min_ids") != null && corefMarkable.getAttributeValue("min_ids").equals(markable.getAttributeValue("min_ids"))) {
                            continue markableLoop;
                        }
                    }
                    HashMap<String, String> attr = new HashMap<String, String>();
                    attr.put(COREF_SET_ATTRIBUTE, Integer.toString(corefset++));
                    attr.put("min_ids", markable.getAttributeValue("min_ids"));
                    corefLevel.addMarkable(markable.getLeftmostDiscoursePosition(), markable.getRightmostDiscoursePosition(), attr);
                }
            }
            final ArrayList<EvaluationMarkableSet> documentPartition = new ArrayList<EvaluationMarkableSet>();
            final List<List<Markable>> docPartition =
                    document.getMarkableLevelByName(markableLevelName).
                    getGroupedBy(COREF_SET_ATTRIBUTE);
            // roll our own data structure
            for (List<Markable> eqClass : docPartition) {
                documentPartition.add(new EvaluationMarkableSet(eqClass, document));
            }
            partition.add((EvaluationMarkableSet[]) documentPartition.toArray(new EvaluationMarkableSet[documentPartition.size()]));
        }
        return (EvaluationMarkableSet[][]) partition.toArray(new EvaluationMarkableSet[partition.size()][]);
    }

    /** Gives the size of the intersection between a key and a response equivalence class,
     *  as well as the number of OPTional keys that have a response provided
     */
    public static int[] getIntersectionSize(
            final EvaluationMarkableSet keyMarkableSet, final EvaluationMarkableSet responseMarkableSet) {
        final Set<Markable> optional = new HashSet<Markable>();
        final Set<Markable> intersected = new HashSet<Markable>();
        // if they don't belong to the same document, don't even bother
        if (!responseMarkableSet.getDocument().equals(keyMarkableSet.getDocument())) {
            return new int[]{0, 0};
        } // else check whether how many overlap
        else {
            for (Markable keyMarkable : keyMarkableSet.getMarkableSet()) {
                for (Markable responseMarkable : responseMarkableSet.getMarkableSet()) {
                    Markable candidate =
                            CorefDocuments.getInstance().corefOverlapsMarkable(
                            keyMarkableSet.getDocument(), keyMarkable, responseMarkable);
                    if (candidate != null) {
                        if (keyMarkable.getAttributeValue(STATUS_ATTRIBUTE, "").equals(STATUS_OPTIONAL)) {
                            optional.add(keyMarkable);
                        }
                        intersected.add(candidate);
                    }
                }
            }
            return new int[]{intersected.size(), optional.size()};
        }
    }

    /** Given a markable in the key, returns whether there is a markable in
     *  the response whose whose direct antecedent is in the same coreference
     *  class as the key pronoun
     */
    public static boolean hasBeenCorrectlyResolved(
            final Markable keyMarkable,
            final EvaluationMarkableSet keyMarkableSet,
            final EvaluationMarkableSet[] responsePartition) {
        // for each equivalence class in the response
        for (EvaluationMarkableSet responseEqClass : responsePartition) {
            // if they don't belong to the same document, don't even bother
            if (responseEqClass.getDocument().equals(keyMarkableSet.getDocument())) {
                // else find the markable on the response level corresponding to
                // the markable in the key and its relative direct antecedent
                for (Markable responseMarkable : responseEqClass.getMarkableSet()) {
                    if (CorefDocuments.getInstance().corefOverlapsMarkable(
                            keyMarkableSet.getDocument(), keyMarkable, responseMarkable) != null) {
                        Markable responseAntecedent =
                                responseEqClass.getDocument().
                                getMarkableLevelByName(DEFAULT_RESPONSE_LEVEL).
                                getMarkableByID(responseMarkable.getAttributeValue(DIRECT_ANT_ATTRIBUTE));

                        // no direct antecedent?! then look for a response markable which
                        // points to the pronoun itself as antecedent!
                        // *NOTE* LEGACY CODE: never used...
                        /*
                        if (responseAntecedent == null)
                        {
                        MMAX2QueryTree tree = null;
                        try
                        {
                        tree = new MMAX2QueryTree("(*dir_antecedent={" +  responseMarkable.getID() + "})",
                        keyMarkableSet.getDocument().
                        getMarkableLevelByName(MUCMarkableLevels.RESPONSE.getName(),false));
                        }
                        catch (MMAX2QueryException mmax2qe) { mmax2qe.printStackTrace(); }
                        responseAntecedent = ((ArrayList<Markable>)
                        tree.execute(new DiscourseOrderMarkableComparator())).get(0);
                        }
                         */

                        // if still no direct antecedent, no correct resolution was performed
                        if (responseAntecedent == null) {
                            return false;
                        }
                        // else check whether there is in the key set a markable overlapping
                        // the response markable antecedent
                        for (Markable key : keyMarkableSet.getMarkableSet()) {
                            if (CorefDocuments.getInstance().corefOverlapsMarkable(
                                    keyMarkableSet.getDocument(), key, responseAntecedent) != null) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Gives the size of the OPT-status coreference markable set members */
    public static int getOPTSize(final EvaluationMarkableSet keyMarkableSet) {
        int optElements = 0;
        for (Markable keyMarkable : keyMarkableSet.getMarkableSet()) {
            if (keyMarkable.getAttributeValue(STATUS_ATTRIBUTE, "").equals(STATUS_OPTIONAL)) {
                optElements++;
            }
        }
        return optElements;
    }
}
