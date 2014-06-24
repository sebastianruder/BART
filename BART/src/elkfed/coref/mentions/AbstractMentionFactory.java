/*
 *   Copyright 2007 Project ELERFED
 *   Copyright 2009 Yannick Versley / CiMeC Univ. Trento
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
package elkfed.coref.mentions;

import static elkfed.mmax.MarkableLevels.COREF_SET_ATTRIBUTE;
import static elkfed.mmax.MarkableLevels.DEFAULT_MARKABLE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreeTransformer;
import elkfed.coref.utterances.Utterance;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.PipelineComponent;
import elkfed.mmax.util.CorefDocuments;

/**
 *
 * @author yannick
 */
public abstract class AbstractMentionFactory implements MentionFactory {

    static protected Logger logger = Logger.getLogger("elkfed.mentions.factory");
    protected boolean _perfectBoundaries;
    protected Markable _currentText;

    /**
     * Creates a new instance of DefaultMentionFactory
     */
    public AbstractMentionFactory() {
        this(false);
    }

    protected abstract boolean keepMarkable(Markable m);

    protected void reportMapping(Markable m_markable,
            Markable m_coref) {
        if (logger.isLoggable(Level.FINE)) {
            if (m_markable != null && m_coref != null) {
                logger.fine("Mapped: " + m_markable.toString() +
                        "(markable:" + m_markable.getID() +
                        "/coref:" + m_coref.getID() + ")");
            } else if (m_markable != null && m_coref == null) {
                logger.fine("Non-Gold: " + m_markable.toString() +
                        "(markable:" + m_markable.getID() + ")");
            }
        }
    }

    /**
     * Creates a new instance of DefaultMentionFactory
     */
    public AbstractMentionFactory(boolean perfectBoundaries) {
        _perfectBoundaries = perfectBoundaries;
    }

    /** Sets whether to use perfect boundaries */
    public void setPerfectBoundaries(boolean perfectBoundaries) {
        _perfectBoundaries = perfectBoundaries;
    }

    public List<Mention> extractMentions(MiniDiscourse doc) throws IOException {
        _currentText = CorefDocuments.getInstance().getText(doc);
        ArrayList<Tree> parseTrees = new ArrayList<Tree>();
        ArrayList<Integer> parseStart = new ArrayList<Integer>();
        ArrayList<Integer> parseEnd = new ArrayList<Integer>();
        ArrayList<Utterance> utterances = new ArrayList<Utterance>();
        // extract parses and create utterances for each sentence
        for (Markable parseMarkable : DiscourseUtils.getMarkables(doc, DEFAULT_PARSE_LEVEL)) {
            Tree currParseTree = postProcess(Tree.valueOf(parseMarkable.getAttributeValue(PipelineComponent.TAG_ATTRIBUTE)));
            Utterance currUtt = new Utterance(currParseTree);
            parseTrees.add(currParseTree);
            parseStart.add(parseMarkable.getLeftmostDiscoursePosition());
            parseEnd.add(parseMarkable.getRightmostDiscoursePosition());
            currUtt.setLeftBoundary(parseMarkable.getLeftmostDiscoursePosition());
            currUtt.setRightBoundary(parseMarkable.getRightmostDiscoursePosition());
            utterances.add(currUtt);
        }
        ArrayList<Mention> inTextMarkables = new ArrayList<Mention>();
        for (Markable m_markable : DiscourseUtils.getMarkables(doc, DEFAULT_MARKABLE_LEVEL)) {
            if (keepMarkable(m_markable)) {
                Markable m_coref = CorefDocuments.getInstance().markableIsaCorefElement(doc, m_markable);
                Mention mention = new Mention(m_markable, doc);
                if (m_coref != null) {
                    mention.setSetID(m_coref.getAttributeValue(COREF_SET_ATTRIBUTE));
                    reportMapping(m_markable, m_coref);
                } else {
                    reportMapping(m_markable, null);
                }
                // find the parse tree that this markable is in
                int startPos = m_markable.getLeftmostDiscoursePosition();
                int endPos = m_markable.getRightmostDiscoursePosition();
                int endPosP = m_markable.getRightmostDiscoursePosition();
                mention.setStartWord(startPos);
                mention.setEndWord(endPos);
                if (m_markable.getAttributeValue("min_ids") != null) {
                    String[] spans = MarkableHelper.parseRanges(m_markable.getAttributeValue("min_ids"));
                    startPos = doc.DiscoursePositionFromDiscourseElementID(spans[0]);
                    endPos = doc.DiscoursePositionFromDiscourseElementID(spans[spans.length - 1]);
                }
                Boolean found=false;
                for (int i = 0; i < parseTrees.size() && !found ; i++) {
                    final int sentStart = parseStart.get(i);
                    final int sentEnd = parseEnd.get(i);

/*
                    if (startPos >= sentStart && endPos <= sentEnd) {
* gold/carafe markables may disrespect sentence boundaries :((
* they should still receive at least some sentence information though
*/
                    if (startPos >= sentStart && startPos <= sentEnd) {
                        found=true;
                        int startOff = startPos - sentStart;
                        int endOff = endPosP - sentStart;
                        Utterance utt = utterances.get(i);
                        mention.setSentenceStart(sentStart);
                        mention.setSentenceEnd(sentEnd);
                        mention.setParseInfo(parseTrees.get(i), startOff, endOff);
                        mention.setUtterance(utt);
                    }
                }
                mention.createDiscourseEntity();
                // in perfect-boundaries mode, we only create markables that
                // we can find in the key
                if (!_perfectBoundaries || m_coref != null) {
                    inTextMarkables.add(mention);
                }
                //sort utterances
                Collections.sort(utterances);
                //sort CFs within utterances
                for (int i = 0; i < utterances.size(); i++) {
                    Collections.sort(utterances.get(i).getCFs());
                }
                //Assign numbers to CFs
                for (int i = 0; i < utterances.size(); i++) {
                    ArrayList<Mention> CFs = utterances.get(i).getCFs();
                    for (int j = 0; j < CFs.size(); j++) {
                        CFs.get(j).setUttPos(j);
                        if (CFs.get(j).getIsFirstMention()) {
                            CFs.get(j).getDiscourseEntity().set_firstMention_isFirstMention(true);
                        }
                    }
                }
            }
        }
        return inTextMarkables;
    }

    private Tree postProcess(Tree tree) {
        TreeTransformer normaliser = new PossessivePronounTransformer();
        return tree.transform(normaliser);
    }

    private class PossessivePronounTransformer implements TreeTransformer {
        /* comment(yv):
         * I currently have no idea why we need this and what
         * would be a plausible generation to multiple languages/tagsets.
         * (We can link mentions to preterminals, so there's no need to
         *  explicitly add NP nodes that are not NPs).
         */

        public Tree transformTree(Tree tree) {
            Tree result = tree;
            Label label = tree.label();
            if (label instanceof TaggedWord) {
                String posTag = ((TaggedWord) label).tag();
                if (posTag.equals("PRP$")) {
                    //if possessive pronoun
                    //XXX: If parent() is not implemented (i.e., returns null)
                    //this would not work!
                    //This tries to ignore possessive pronouns that are already
                    //within their own NP as in ((his) car)
                    Tree parent = tree.parent();
                    Tree[] children = parent.children();
                    if (children.length > 1) {
                        //create a new NP node to attach the pronoun to
                        TreeFactory treeFactory = tree.treeFactory();
                        int indx = parent.objectIndexOf(tree);
                        List aux = new ArrayList();
                        aux.add(tree);
                        Tree pronounNP = treeFactory.newTreeNode(new StringLabel("NP"), aux);
                        parent.insertDtr(pronounNP, indx);
                        parent.setChild(indx, pronounNP);
                    }
                }
            }
            return result;
        }
    }
}
