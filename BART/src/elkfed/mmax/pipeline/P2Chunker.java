/*
 * Copyright 2007 Project ELERFED
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



import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;

import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.minidisc.Markable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.Set;
import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;
import static elkfed.mmax.pipeline.MarkableCreator.MAXSPAN_ATTRIBUTE;

/**
 * loads parse trees from the mmax data, creates chunks
 * NB: the possessive bug fixed: "NP's NP" added as chunks
 * @author olga
 */
public class P2Chunker extends PipelineComponent {
            

    /** The attributes of the default markable level*/
    protected Map<String,String> markableLevelAttributes;
    
    /** Markable levels for the chunking tags */
    protected MarkableLevel chunkLevel;
    
    /** The attributes of the markable level for chunking */
    protected final Map<String,String> chunkAttributes;
    
    /** The String used to separate the tokens from their positional
     *  index in the leaves of the trees.
     */
    private static final String INDEX_SEPARATOR = "&&&&&&&&&&&&&&&&&&&&&&&&"; //"IND~~~IND"; - this leads to completely wrong results!
    private static final String NPSTATUS_SEPARATOR = "^^^^^^^^^^^^^^^^^^^";//"STAT^^^STAT";
//npstatus:0 -- embedding, 1 -- basic, not embedded, 2 -- basic, embedded

     private ModCollinsHeadFinder _headFinder;
     private ModCollinsHeadFinder getHeadFinder() {
         if (_headFinder==null)
           _headFinder= new ModCollinsHeadFinder();
         return _headFinder;
     }

    /** Creates a new instance of Parser */
    public P2Chunker() {
                
        this.chunkAttributes = new HashMap<String,String>();
        this.chunkAttributes.put("mmax_level", DEFAULT_CHUNK_LEVEL);
    }
        
    /*
     * Annotates the corpus given in <code>data</code> by calling
     * annotateDocument() and addMarkables() for each document in the corpus.
     * 
     * @param data  the corpus to be annotated
     */

    public void annotate(MiniDiscourse doc)
    {
            this.currentDocument = doc;
            this.currentLevel = currentDocument.getMarkableLevelByName(levelName);
            this.chunkLevel = currentDocument.getMarkableLevelByName(DEFAULT_CHUNK_LEVEL);
            
            
            annotateDocument();
            addMarkables();
            
            chunkLevel.saveMarkables();
    }
    
    protected  void annotateDocument() {};
    
    /** Add parser, part of speech, and chunk markables */
    protected void addMarkables() {
        
        final StringBuffer markableBuffer = new StringBuffer();
        List<Markable> sentences = null;
        
       for (Markable parseMarkable :
                DiscourseUtils.getMarkables(currentDocument, DEFAULT_PARSE_LEVEL)) {

            int start=parseMarkable.getLeftmostDiscoursePosition();
            int end=parseMarkable.getRightmostDiscoursePosition();

        /** Retrieve chunk tags from the parse tree and add chunk markables */


/* traverse parse-tree (real tree, not string), extract basic NPs and poss */

            Tree pTree = null;
            pTree = Tree.valueOf(parseMarkable.getAttributeValue(
			    PipelineComponent.TAG_ATTRIBUTE));
			 normalizeTree(pTree);
 
            if (pTree==null) continue;
           
//add all basic nps
        for (Iterator<Tree> treeIt = pTree.iterator(); treeIt.hasNext();) {
            Tree nod = treeIt.next();
            if (nod.value().equals("NP"+NPSTATUS_SEPARATOR+"1") ||
                nod.value().equals("NP"+NPSTATUS_SEPARATOR+"2")) {
             markableBuffer.setLength(0);
             addChunkMarkable(nod,pTree,start,false);
            }
         }


            List<Tree> Leaves = pTree.getLeaves();
           
// add NPs embedding possessives
            for (Tree l: Leaves) {
               if (l.value().toLowerCase().startsWith("'s")) {

                 if (l.parent(pTree)!=null && 
l.parent(pTree).value().equals("POS") &&
l.parent(pTree).parent(pTree)!=null &&
l.parent(pTree).parent(pTree).value().startsWith("NP") &&
l.parent(pTree).parent(pTree).parent(pTree)!=null &&
l.parent(pTree).parent(pTree).parent(pTree).value().equals("NP"+NPSTATUS_SEPARATOR+"0")) {
Tree nod=l.parent(pTree).parent(pTree).parent(pTree);
markableBuffer.setLength(0);
addChunkMarkable(nod,pTree,start,true);

               }
 

            }

      }
  }
}
        
private void addChunkMarkable(Tree nod, Tree pTree,int start, Boolean checkup) {

  // register new chunk markable, setting maxspan if needed
    List<Tree> lv=nod.getLeaves();
    int npstart=
    Integer.valueOf(lv.get(0).label().value().split(INDEX_SEPARATOR)[1]);
    int npend=
    Integer.valueOf(lv.get(lv.size()-1).label().value().split(INDEX_SEPARATOR)[1]);
    npstart+=start;
    npend+=start;

    final Map<String,String> cAttributes =
      new HashMap<String,String>(chunkAttributes);
      cAttributes.put(TAG_ATTRIBUTE, "np");

//store maxspan for embedded nps (either basic or explicitly marked for doing so)
         
    if (checkup || nod.value().equals("NP"+NPSTATUS_SEPARATOR+"2")) {
      Tree p=nod;
      Tree head=p.headTerminal(getHeadFinder());
      Tree lastmax=null;
      while(p!=null) {
        p=p.parent(pTree);
        if (p!=null && p.value().startsWith("NP")) {
           if ((p.headTerminal(getHeadFinder())==head) && (!iscoordnp(p)))
              lastmax=p;
           else
              p=null;
        }
      }
      if (lastmax!=null) {
        List<Tree> lvm=lastmax.getLeaves();
        int maxstart=
         Integer.valueOf(lvm.get(0).label().value().split(INDEX_SEPARATOR)[1]);
        int maxend=
         Integer.valueOf(lvm.get(lvm.size()-1).label().value().split(INDEX_SEPARATOR)[1]);
        maxstart+=start+1;
        maxend+=start+1;
        cAttributes.put(MAXSPAN_ATTRIBUTE, "word_" + maxstart +"..word_"+maxend);
      }


   }

   chunkLevel.addMarkable(npstart,npend,cAttributes);
}


    
           
    /** Returns the markable level for parsing data */
    public String getLevelName() {
        return DEFAULT_PARSE_LEVEL;
    }
    

    public void checkLevels(Set<String> processedLevels,
            Set<String> goldLevels) {
        processedLevels.add(DEFAULT_CHUNK_LEVEL);

    }

    private void normalizeTree(Tree tree) {
// for leaves -- add positions
// for nps -- add whether they are basic or not

        int leaveIndex = 0;
        for (Iterator<Tree> treeIt = tree.iterator(); treeIt.hasNext();) {
            Tree currentTree = treeIt.next();
            Label nodeLabel = currentTree.label();
            if (currentTree.isLeaf()) {
                nodeLabel.setValue(nodeLabel.value() + INDEX_SEPARATOR + leaveIndex);
                leaveIndex++;
            } else {

               if (currentTree.value().toLowerCase().startsWith("np")) {

                 Boolean found=false;

//adjust this np for keeping (if not already discarded
                 if (!currentTree.value().endsWith("0") &&
                     !currentTree.value().endsWith("2"))
                    currentTree.label().setValue("NP" + NPSTATUS_SEPARATOR + "1");


//adjust upper np for discarding

                 Tree p=currentTree;
                 Tree head=p.headTerminal(getHeadFinder());
                 while(p!=null && !found) {
                   p=p.parent(tree);
                   if (p!=null && 
                       p.value().toLowerCase().startsWith("np") &&
                       p.headTerminal(getHeadFinder())==head && (!iscoordnp(p))) {
                      found=true;
                      p.label().setValue("NP" + NPSTATUS_SEPARATOR + "0");
                      currentTree.label().setValue("NP" + NPSTATUS_SEPARATOR + "2");
                   }
                 }

                }else{
                  nodeLabel.setValue(nodeLabel.value().toUpperCase());
                }
            }
        }
    }
    private Boolean iscoordnp(Tree np) {
// helper -- checks that a parse np-tree is in fact coordination (contains CC on the highest level)
      if (np==null) return false;
      if (!np.value().startsWith("NP")) return false;
      Tree[] chlds=np.children();
      for (int i=0; i<chlds.length; i++) {
        if (chlds[i].value().equalsIgnoreCase("CC")) return true;
      }
      return false;
    }


}
