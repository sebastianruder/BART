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

import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.knowledge.SemanticClass;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import elkfed.coref.mentions.Mention;
import elkfed.config.ConfigProperties;


/**
 *
 * @author yannick
 */
public class ItalianLanguagePlugin extends AbstractLanguagePlugin {

    public static final String PREPART = "(?:ne|a|su|de|co)(?:l|ll[a\']|gli|i)";
    public static final String PUNCT = "[,:;\\.`\'\"\\-\\)\\(]+";
    protected static final Pattern left_unwanted = Pattern.compile(
            String.format("%s|%s", PREPART, PUNCT), Pattern.CASE_INSENSITIVE);
    protected static final Pattern right_unwanted = Pattern.compile(PUNCT);
    protected static final Pattern attr_node = Pattern.compile(
            "A[SPN]|ADJP");
    protected static final Pattern rel_node = Pattern.compile(
            "PX|SBAR|S-REL|VP");

    public ItalianLanguagePlugin() {
        super();
        readMapping(TableName.AdjMap, "adj_map_it.txt");
        readMapping(TableName.RoleMap, "role_map_it.txt");
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
            MentionType mentionType) {
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
        NodeCategory ncat=labelCat(node.label().value());
        if (startWord==endWord &&
                ( ncat==NodeCategory.CN ||
                ncat==NodeCategory.PRO ||
                ncat==NodeCategory.PN))
        {
            node=node.parent(sentTree);
        }
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



    private Boolean iscoordnp(Tree np) {
// helper -- checks that a parse np-tree is in fact coordination (contains CC on the highest level)
      if (np==null) return false;
      if (!np.value().equalsIgnoreCase("NP")) return false;
      Tree[] chlds=np.children();
      for (int i=0; i<chlds.length; i++) {
        if (chlds[i].value().equalsIgnoreCase("CC")) return true;
      }
      return false;
    }

    @Override
    public Tree[] calcParseExtra(Tree sentenceTree,
            int startWord, int endWord, Tree prsHead,
            HeadFinder StHeadFinder) {

       List<Tree> Leaves = sentenceTree.getLeaves();
        Tree startNode = Leaves.get(startWord);

        Tree endNode=null;

        if (endWord>=Leaves.size()) {
// for marks that do not respect sentence boundaries
         endNode=Leaves.get(Leaves.size()-1);
        }else{
         endNode = Leaves.get(endWord);
        }

        Tree prevNode = null;
        if (startWord>0) prevNode = Leaves.get(startWord-1);
        Tree nextNode = null;
        if (endWord < Leaves.size()-1) nextNode=Leaves.get(endWord+1);


       Tree[] result=new Tree[3];

//---------- calculate minimal np-like subtree, containing the head and included in the mention


       Tree HeadNode=prsHead;
       if (prsHead==null) {
// todo: this should be fixed somehow though
// todo (ctd): use getHeadIndex from NPHeadFinder, but need to reconstruct the markable
// todo (ctd): mind marks spanning over sentene boundaries

         result[0]=null;
         result[1]=null;
         result[2]=null;
         return result;
       }


      Tree mincand=prsHead;
       Tree t=mincand;
       Tree minnp=null;
       Tree maxnp=null;


       while(t!=null &&
             (prevNode == null || !t.dominates(prevNode)) &&
             (nextNode == null || !t.dominates(nextNode))) {
         if (t.value().equalsIgnoreCase("NP")) {
             mincand=t;
             t=null;
          }
          if (t!=null) t=t.parent(sentenceTree);
       }

       result[0]=mincand;

       t=mincand;
      while(t!=null && (t==mincand || !iscoordnp(t))) {

          if (t.value().equalsIgnoreCase("NP")){


            if (t.headTerminal(StHeadFinder)==HeadNode) {
              maxnp=t;
              if (minnp==null) minnp=t;
            }else{
              t=null;
            }
          }
          if (t!=null) t=t.parent(sentenceTree);
       }

       result[1]=minnp;
       result[2]=maxnp;
       return result;

    }


    private final static Pattern pat_NP=
            Pattern.compile("NP",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_CN=
            Pattern.compile("S[SNP]",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PN=
            Pattern.compile("SPN",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PRO=
            Pattern.compile("P[SNP]",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PP=
            Pattern.compile("PX",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PREP=
            Pattern.compile("E[SNP]?",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_S=
            Pattern.compile("S|SBAR|S-REL",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_VP=
            Pattern.compile("VP",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_DT=
            Pattern.compile("R[SNP]",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_DT2=
            Pattern.compile("D[SNP]",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_ADJ=
            Pattern.compile("A[SNP]",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_ADV=
            Pattern.compile("B",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_CC=
            Pattern.compile("C",Pattern.CASE_INSENSITIVE);
    private final static Pattern pat_PUNCT=
            Pattern.compile("XP.",Pattern.CASE_INSENSITIVE);
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
        } else if (pat_VP.matcher(cat).matches()) {
            return NodeCategory.VP;
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
        } else if (pat_PUNCT.matcher(cat).matches()) {
            return NodeCategory.PUNCT;
        } else {
            return NodeCategory.OTHER;
        }
    }

    @Override
    public SemanticClass getSemanticClass(String sem_type, String gender) {
        if (sem_type.equalsIgnoreCase("persona")) {
            if (gender.equalsIgnoreCase("fem")) {
                return SemanticClass.FEMALE;
            } else if (gender.equalsIgnoreCase("masc")) {
                return SemanticClass.MALE;
            } else {
                return SemanticClass.PERSON;
            }
        } else if (sem_type.equalsIgnoreCase("organizzazione")) {
            return SemanticClass.ORGANIZATION;
        } else if (sem_type.equalsIgnoreCase("gsp")||
                sem_type.equalsIgnoreCase("spaziale")) {
            return SemanticClass.LOCATION;
        } else if (sem_type.equalsIgnoreCase("temporale")) {
            return SemanticClass.TIME;
        } else if (sem_type.equalsIgnoreCase("ogetto")) {
            return SemanticClass.OBJECT;
        } else {
            return SemanticClass.UNKNOWN;
        }
    }

    public static void main(String[] args) {
        LanguagePlugin plugin=new ItalianLanguagePlugin();
        String[] test_strings={"brasiliano", "congolese", "francese"};
        for (String s: test_strings) {
            System.out.format("%s AdjMap => %s\n", s,
                    plugin.lookupAlias(s, TableName.AdjMap));
        }
    }

    public boolean isExpletiveWordForm(String string) {
        if (ConfigProperties.getInstance().getDbgPrint()) 
           System.out.println("ItalianLanguagePlugin.isExpletiveWordForm is not supported yet.");
        return false;
    }
}
