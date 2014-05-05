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

package elkfed.mmax.pipeline;

import static elkfed.lang.EnglishLinguisticConstants.PRONOUN;
import static elkfed.mmax.MarkableLevels.DEFAULT_MARKABLE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_SEMROLE_LEVEL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.config.ConfigProperties;
import elkfed.mmax.Corpus;
import elkfed.mmax.CorpusFactory;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MarkableQuery;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.util.NPHeadFinder;

/** A Semantic tagger assigns semantic tags to markables
 *
 * @author ponzetsp
 */
public final class SemTagger extends PipelineComponent
{   
    /** The String used to separate the tokens from their positional
     *  index in the leaves of the trees.
     */
    private static final String INDEX_SEPARATOR = "~";
    
    /** The semantic roles of the current doc */
    private List<Markable> semroles;
    
    /** The markables of the current doc */
    private List<Markable> markables; 
    
    /** The head finder we use */
    private HeadFinder headFinder;
    
    /** The parse information of the current doc */
    private List<Tree> parseTrees;
    private List<Integer> parseStart;
    private List<Integer> parseEnd;
    
    /** Creates a new instance of SemTagger */
    public SemTagger()
    {
        super();
        
        this.semroles = new ArrayList<Markable>();
        this.markables = new ArrayList<Markable>();
        
        this.headFinder = new CollinsHeadFinder();
        
        this.parseTrees=new ArrayList<Tree>();
        this.parseStart=new ArrayList<Integer>();
        this.parseEnd=new ArrayList<Integer>();
    }

    /** Method added to modify the semantic role attribute without
     *  running the entire pipeline
     */
    public static void run(File dataDir, String corpusID)
    {
        for (File mmaxFile : dataDir.listFiles(new MMAX2FilenameFilter()))
        {
            Corpus data = 
                CorpusFactory.getInstance().createCorpus(mmaxFile, corpusID);
            new SemTagger().annotate(data.get(0));
        }
    }
    
    protected void annotateDocument() {
        initDocument();
        addSemanticRoleAttribute();
    }
    
    /** Sets the list of semantic roles of a given document */
    private void initDocument()
    {
        // reset the pool of semantic roles and markables of the corrent doc
        this.semroles.clear();
        this.markables.clear();
        
        this.parseTrees.clear();
        this.parseStart.clear();
        this.parseEnd.clear();
        
        // and get the new ones
        MarkableLevel semrole_level=currentDocument
                .getMarkableLevelByName(DEFAULT_SEMROLE_LEVEL);
        MarkableQuery q=new MarkableQuery(semrole_level);
        q.addAttCondition("tag", "target", MarkableQuery.OP_NE);
        this.semroles = q.execute(semrole_level,MiniDiscourse.DISCOURSEORDERCMP);
        this.markables = currentLevel.getMarkables(MiniDiscourse.DISCOURSEORDERCMP);
        
        for (Markable parseMarkable :
                DiscourseUtils.getMarkables(currentDocument, DEFAULT_PARSE_LEVEL))
        {
            Tree currParseTree = null;
            currParseTree = Tree.valueOf(parseMarkable.getAttributeValue(
			    PipelineComponent.TAG_ATTRIBUTE));
			 normalizeTree(currParseTree);
            
            parseTrees.add(currParseTree);
            parseStart.add(parseMarkable.getLeftmostDiscoursePosition());
            parseEnd.add(parseMarkable.getRightmostDiscoursePosition());
        }
    }
    
    /** Prepare the markable level for on-the-fly fixes:
     *
     *  1. set all semrole attributes to ""
     *  2. makes markable level of current doc dirty to force saving later
     */
    private void prepareMarkableLevel()
    {
        for (Markable markable : markables)
        { markable.setAttributeValue(DEFAULT_SEMROLE_LEVEL, ""); }
        currentLevel.setIsDirty(true);
    }
    
    /** Add semantic role information to markables */ 
    private void addSemanticRoleAttribute()
    {        
        // for each semantic role look for a matching markable
        for (Markable semrole : semroles)
        {
            int semroleHead = findSemanticRoleHeadIndex(semrole);
            
            boolean markableFound = false;
            final Iterator<Markable> markableIt = markables.iterator();
            
            while (!markableFound && markableIt.hasNext())
            {
                Markable markable = markableIt.next();
                int markableHead = markable.getLeftmostDiscoursePosition() +
                        NPHeadFinder.getInstance().getHeadIndex(markable);
                
                if (
                            // either exact match
                            exactMatch(semrole, markable)
                    ||
                        (
                            // if not pronoun
                            !isPronoun(markable)
                         &&
                            semroleHead == markableHead
                        )
                    )
                {
                    addSemRole(semrole, markable);
                    markableFound = true;
                }
            }
        }
    }
    
    /** Checks whether two markables match in BOTH boudary */
    private boolean exactMatch(Markable markable1, Markable markable2)
    {
        return (
                    markable1.getLeftmostDiscoursePosition()
                  ==
                    markable2.getLeftmostDiscoursePosition()
                &&
                    markable1.getRightmostDiscoursePosition()
                  ==
                    markable2.getRightmostDiscoursePosition()
        );
    }
    
    /** Checks whether a markable is a pronoun */
    private boolean isPronoun(Markable markable)
    {
        return (
                    markable.getDiscourseElementIDs().length == 1
                &&
                    new StringBuffer(markable.toString()).
                        deleteCharAt(markable.toString().length()-1).deleteCharAt(0).toString().
                    toLowerCase().matches(PRONOUN)
        );
    }
    
    /** Adds the semantic role information to a markable */
    private void addSemRole(Markable semroleMarkable, Markable markable)
    {
        StringBuffer role = new StringBuffer();
        if (!markable.getAttributeValue(DEFAULT_SEMROLE_LEVEL, "").equals(""))
        { role.append(markable.getAttributeValue(DEFAULT_SEMROLE_LEVEL)).append("_"); }
        markable.setAttributeValue(DEFAULT_SEMROLE_LEVEL, role.
                append(semroleMarkable.getAttributeValue(TAG_ATTRIBUTE)).
                append("/").append(semroleMarkable.getAttributeValue("target_lemmata")).
                toString());
    }

    protected void addMarkables() {
        // this component does not add markables per-se;
    }
    
    /** Returns the markable level for semantic parsing data */
    public String getLevelName() {
        return DEFAULT_MARKABLE_LEVEL;
    }
    
    /** Finds the index of the head of a (non-basal) semantic role phrase */
    private int findSemanticRoleHeadIndex(Markable semroleMarkable) {
    
        // 1. Get the syntactic tree semroleMarkable is contained into
        final int srStart=semroleMarkable.getLeftmostDiscoursePosition();
        final int srEnd=semroleMarkable.getRightmostDiscoursePosition();
        
        for (int i=0; i<parseTrees.size(); i++)
        {
            final int sentStart=parseStart.get(i);
            final int sentEnd=parseEnd.get(i);
            if (srStart>=sentStart && srEnd<=sentEnd)
            {
                // GOTCHA!
                Tree tree = parseTrees.get(i);
                
                // 2. Find the lowest node containing the markable at its leaves
                final int srOnset=srStart-sentStart;
                final int srOffset=srEnd-sentStart;
                
                final List<Tree> leaves = tree.getLeaves();
                final Tree startNode = leaves.get(srOnset);
                final Tree endNode = leaves.get(srOffset);
        
                Tree parentNode = startNode;
                while (parentNode != null && !parentNode.dominates(endNode)) {
                    parentNode = parentNode.parent(tree);
                }
        
                Tree lowestProjection = null;
                if (parentNode == null) {
                    lowestProjection = startNode;
                }
                else {
                    lowestProjection = parentNode;
                }
                
                // 3. Find the head and return its index
                Tree headWord = lowestProjection.headTerminal(headFinder);
                return Integer.valueOf(headWord.label().value().split(INDEX_SEPARATOR)[1])+sentStart;
            }
        }
        return -1;
    }
    
    /** Given a tree, uppercases all its non-terminal label (so that the
     *  CollinsHeadFinder can be applied to it) and add a positional index to
     *  the terminal nodes.
     * 
     * @param tree the tree whose labels are to be uppercased
     */
    private void normalizeTree(Tree tree) {
        int leaveIndex = 0;
        for (Iterator<Tree> treeIt = tree.iterator(); treeIt.hasNext();) {
            Tree currentTree = treeIt.next();
            Label nodeLabel = currentTree.label();
            if (currentTree.isLeaf()) {
                nodeLabel.setValue(nodeLabel.value() + INDEX_SEPARATOR + leaveIndex);
                leaveIndex++;
            } else {
                nodeLabel.setValue(nodeLabel.value().toUpperCase());
            }
        }
    }
    
    /** Just for testing */
    public static void main(String[] args) {
        run(ConfigProperties.getInstance().getTrainingData(), "sample");
    }
}
