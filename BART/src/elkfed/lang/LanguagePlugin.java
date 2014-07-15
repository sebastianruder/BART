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
package elkfed.lang;

import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.mmax.minidisc.Markable;
import java.util.List;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import elkfed.coref.mentions.Mention;

/** Interface for a piece of software that
 * - extracts information from a Markable and puts it into
 *   a Mention
 * - implements language-specific rules to adjust mention boundaries
 *   to aid the evaluation
 * @author versley
 */
public interface LanguagePlugin {

    /** returns an array of lists of trees.
     * The first list contains all the projections, with the lowest projections
     * as the first element and the highest as the last element.
     * The second list contains all adjectival modifiers
     * (i.e., typically premodifiers)
     * The thirs list contains all 'relational' modifiers
     * (i.e., PPs, relative clauses, SBARs, ...)
     * @param sentTree
     * the tree representation of the sentence
     * @param startWord
     * the start offset in the sentence
     * @param end
     * the end offset in the sentence
     * @return
     */
    public Tree[] calcParseExtra(Tree sentTree,
            int startWord, int endWord,
            Tree prsHead, HeadFinder StHeadFinder);

    public List<Tree>[] calcParseInfo(Tree sentTree,
            int startWord, int endWord,
            MentionType mentionType);

    public String enamexType(Markable markable);

    /** returns the part of the markable string that is part
     * of the chunk NP
     */
    public String markableString(Markable markable);

    public String markablePOS(Markable markable);

    /** returns a MentionType describing the markable */
    MentionType calcMentionType(Markable markable);
    /** gets the head(s) of the markable */
    String getHead(Markable markable);
    /** gets the head(s) of the markable -
     * this exists due to slight differences to the
     * English version, which cannot figure out multi-token heads
     */
    
    /**
     * Gets the POS of the head of the markable
     *
     * @author samuel
     * @param markable
     * @return the POS of the head of the markable 
     */
    public String getHeadPOS(Markable markable);

    public String getHeadLemma(Markable markable);

    String getHeadOrName(Markable markable);
    /** returns true iff <i>tok</i> at the left boundary
     * should not be part of the markable
     * @param tok token string
     */
    boolean unwanted_left(String tok);
    /** returns true iff <i>tok</i> at the right boundary
     * should not be part of the markable
     * @param tok token string
     */
    boolean unwanted_right(String tok);
    
    /** maps a grammar label to a NodeCategory */
    NodeCategory labelCat(String cat);

    public boolean isExpletiveWordForm(String string);

/* rule-based expletive detection -- now for "it" (Eng) only */
    public boolean isExpletiveRB(Mention m);

    static enum TableName {
        DemonymMap,
        RoleMap,
        SpeechVerbMap
    }

    public String lookupAlias(String original, TableName table);
    
    public boolean isInAnimateList(String string);
    
    public boolean isInInanimateList(String string);
    
    public boolean isInNeutralList(String string);
    
    public boolean isInMaleList(String string);
    
    public boolean isInFemaleList(String string);
    
    public boolean isInStopwordList(String string);
    
    public boolean isInSpeechVerbList(String string);
    
}