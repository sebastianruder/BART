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
import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.mmax.MarkableLevels;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.nlp.util.Gender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;


import java.util.Map;

import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;

/**
 *
 * @author yannick
 */
public abstract class AbstractLanguagePlugin implements LanguagePlugin {

    public static final String MARKABLE_TYPE = "markable_type";
    public static final String MENTION_TYPE = "mention_type";
    public static final String GENDER = "gend";
    public static final String NUMBER = "num";
    public static final String SEMCLASS = "sem_type";
    public static final String HEAD_POS = "head_pos";
    public static final String MIN_IDS = "min_ids";
    public static final String HEAD_WORD = "head_word";
    public static final String SEM_TYPE = "sem_type";
    protected List<String> animate_list = new ArrayList<String>();
    protected List<String> inanimate_list = new ArrayList<String>();
    protected List<String> neutral_list = new ArrayList<String>();
    protected List<String> male_list = new ArrayList<String>();
    protected List<String> female_list = new ArrayList<String>();

    protected final Map<LanguagePlugin.TableName,Map<String,String>> aliasTables =
            new EnumMap(LanguagePlugin.TableName.class);

    public void readMapping(LanguagePlugin.TableName table, String fname)
    {
        Map<String,String> map;
        try {
            File names_dir=new File(ConfigProperties.getInstance().getRoot(),"names");
            BufferedReader br=new BufferedReader(new FileReader(new File(names_dir,fname)));
            map=new HashMap<String,String>();
            String line;
            while ((line=br.readLine())!=null)
            {
                String[] entries=line.split("\t");
                String[] aliases=entries[1].split("; *");
                for (String alias: aliases) {
                    map.put(alias,entries[0]);
                }
            }
            br.close();
            aliasTables.put(table, map);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    public void readList(List<String> list, String fname) {
    	try {
            File names_dir = new File(ConfigProperties.getInstance().getRoot(),"names");
            BufferedReader br = new BufferedReader(new FileReader(new File(names_dir,fname)));
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();
        }
    	catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void add_pronoun_features(String pron_type,
            MentionType result) {
        result.features.add(MentionType.Features.isPronoun);
        if (pron_type.equals("pro.refl")) {
            result.features.add(MentionType.Features.isReflexive);
        } else if (pron_type.equals("pro.rel")) {
            result.features.add(MentionType.Features.isRelative);
        } else if (pron_type.equals("pro.per1") ||
                pron_type.equals("pro.per2")) {
            result.features.add(MentionType.Features.isFirstSecondPerson);
        } else if (pron_type.equals("pro.per3")) {
            result.features.add(MentionType.Features.isPersPronoun);
        } else {
            throw new UnsupportedOperationException("Unknown pronoun type " + pron_type);
        }
    }

    public void add_nominal_features(String nom_type,
            MentionType result) {
        result.features.add(MentionType.Features.isNominal);
        if (nom_type.equals("nom.def")) {
            result.features.add(MentionType.Features.isDefinite);
        } else if (nom_type.equals("nom.indef")) {
            result.features.add(MentionType.Features.isIndefinite);
        } else {
            throw new UnsupportedOperationException("Unknown nominal type " + nom_type);
        }
    }

    public MentionType calcMentionType(Markable markable) {
        String markable_type = markable.getAttributeValue(MARKABLE_TYPE,"any");
        String mention_type = markable.getAttributeValue(MENTION_TYPE,"any");//Olga: quickfix here
        String gend = markable.getAttributeValue(GENDER, "any");
        String num = markable.getAttributeValue(NUMBER, "any");
        String sem_type = markable.getAttributeValue(SEM_TYPE, "any");
        MentionType result = new MentionType();
        if ("nam".equals(mention_type)) {
            // TBD: add name features
            if (!sem_type.equalsIgnoreCase("any")) {
                result.features.add(MentionType.Features.isEnamex);
            }
            result.features.add(MentionType.Features.isProperName);
        } else if (mention_type.startsWith("pro.")) {
            add_pronoun_features(mention_type, result);
        } else if (mention_type.startsWith("nom.")) {
            add_nominal_features(mention_type, result);
        } else if (mention_type.equals("any")) {
            if (ConfigProperties.getInstance().getDbgPrint())
            System.err.format("mention_type=any: '%s'\n",
                    markable.toString());
        } else {
            throw new UnsupportedOperationException("Unknown mention type " + mention_type);
        }

        if (!sem_type.equalsIgnoreCase("any")) {
            result.semanticClass=getSemanticClass(sem_type,gend);
        }
        if (markable_type.equals("np.coord")) {
            result.features.add(MentionType.Features.isCoord);
        }
        // EnglishLanguagePlugin also uses Gender.PLURAL.
        // we don't do this and only use the isSingular/isPlural features
        if (gend.equals("fem")) {
            result.gender = Gender.FEMALE;
        } else if (gend.equals("masc")) {
            result.gender = Gender.MALE;
        } else if (gend.equals("neut")) {
            result.gender = Gender.NEUTRAL;
        }
        if (num.equals("sing")) {
            result.features.add(MentionType.Features.isSingular);
        } else if (num.equals("plur")) {
            result.features.add(MentionType.Features.isPlural);
        }
        if (markable.getAttributeValue(MARKABLE_TYPE).equals("np.coord")) {
            result.features.add(MentionType.Features.isCoord);
        }

        return result;
    }

    public String getHead(Markable markable) {
        String mention_type = markable.getAttributeValue(MARKABLE_TYPE,"null");
        if (mention_type.startsWith("v.")) {
            return markable.getAttributeValue(HEAD_WORD, "0");
        } else {
            String head_posId = markable.getAttributeValue(HEAD_POS);
            if (head_posId == null) {
                return "*NULL*";
            }
            String[] ranges = MarkableHelper.parseRanges(head_posId);
            MiniDiscourse doc = markable.getMarkableLevel().getDocument();
            MarkableLevel lemmas = doc.getMarkableLevelByName("lemma");
            int[] positions = doc.getPositions(ranges);
            StringBuffer buf = new StringBuffer();
            for (int pos : positions) {
                List<Markable> lemma_markables=
                        lemmas.getMarkablesAtDiscoursePosition(pos);
                if (lemma_markables.size()!=1) {
                    System.out.format("for ranges=%s: %s",head_posId,lemma_markables);
                }
                buf.append(' ');
                buf.append(lemma_markables.get(0).getAttributeValue("tag"));
            }
            return buf.substring(1);
        }
    }

/*
    public String enamexType(Markable markable) {
        return markable.getAttributeValue(SEM_TYPE,"any");
    }
*/
    public String enamexType(Markable markable) {
        SemanticClass semclass=getSemanticClass(markable.getAttributeValue(SEM_TYPE,"any"),"any");
        if (semclass==SemanticClass.PERSON) return "person";

        if (semclass==SemanticClass.TIME) return "date";
        if (semclass==SemanticClass.ORGANIZATION) return "organization";

        if (markable.getAttributeValue(SEM_TYPE,"any").equals("gsp")) return "gsp";
        if (semclass==SemanticClass.LOCATION) return "location";
        return markable.getAttributeValue(SEM_TYPE,"any");
    }

    protected Tree calcLowestProjection(Tree sentenceTree,
            int startWord, int endWord) {
        List<Tree> Leaves = sentenceTree.getLeaves();
        Tree startNode = Leaves.get(startWord);
        Tree endNode = Leaves.get(endWord);

        Tree parentNode = startNode;
        while (parentNode != null && !parentNode.dominates(endNode)) {
            parentNode = parentNode.parent(sentenceTree);
        }

        if (parentNode == null) {
            return startNode;
        }
        return parentNode;
    }

    public List<Tree>[] calcParseInfo(Tree sentTree,
            int startWord, int endWord,
            MentionType mentionType) {
        List<Tree>[] result = new List[3];
        List<Tree> projections = new ArrayList<Tree>();
        List<Tree> premod = new ArrayList<Tree>();
        List<Tree> postmod = new ArrayList<Tree>();
        result[0] = projections;
        result[1] = premod;
        result[2] = postmod;
        projections.add(calcLowestProjection(sentTree, startWord, endWord));
        return result;
    }

    public String getHeadLemma(Markable markable) {
        return getHead(markable);
    }
    public String getHeadOrName(Markable markable) {
        return getHead(markable);
    }

    public String markableString(Markable markable) {
        String mention_type = markable.getAttributeValue(MARKABLE_TYPE,"null");
        if (mention_type.startsWith("v.")) {
            return markable.getAttributeValue(HEAD_WORD, "0");
        } else {
            String chunk_posId = markable.getAttributeValue(MIN_IDS);
            int[] positions;
            MiniDiscourse doc = markable.getMarkableLevel().getDocument();
            StringBuffer buf = new StringBuffer();
            if (chunk_posId == null) {
                for (String tok : markable.getDiscourseElements()) {
                    buf.append(' ');
                    buf.append(tok);
                }
            } else {
                String[] ranges = MarkableHelper.parseRanges(chunk_posId);
                positions = doc.getPositions(ranges);
                for (int pos : positions) {
                    if (pos>markable.getRightmostDiscoursePosition()||
                            pos<markable.getLeftmostDiscoursePosition()) {
                         System.err.println("Yuck:"+markable.toString()+
                                "min_ids="+chunk_posId+"rightmost="+markable.getRightmostDiscoursePosition());
                    } else {
                        buf.append(' ');
                        buf.append(doc.getDiscourseElementAtDiscoursePosition(pos));
                    }
                }
            }
            return buf.substring(1);
        }
    }

    public String markablePOS(Markable markable) {
        String mention_type = markable.getAttributeValue(MARKABLE_TYPE,"any");
        if (mention_type.startsWith("v.")) {
            return "*CLI*";
        } else {
            String chunk_posId = markable.getAttributeValue(MIN_IDS);
            int[] positions;
            MiniDiscourse doc = markable.getMarkableLevel().getDocument();
            Markable[] pos_markables=doc.getMarkableLevelByName(DEFAULT_POS_LEVEL)
                    .getMarkablesAtSpan(markable.getLeftmostDiscoursePosition(),
                        markable.getRightmostDiscoursePosition());
            StringBuffer buf = new StringBuffer();
            if (chunk_posId == null) {
                for (Markable m_pos : pos_markables) {
                    buf.append(' ');
                    buf.append(m_pos.getAttributeValue("tag"));
                }
            } else {
                String[] ranges = MarkableHelper.parseRanges(chunk_posId);
                positions = doc.getPositions(ranges);
                int pos1=markable.getLeftmostDiscoursePosition();
                for (int pos : positions) {
                    if (pos>markable.getRightmostDiscoursePosition() ||
                            pos<markable.getLeftmostDiscoursePosition()) {
                        System.err.println("Yuck:"+markable.toString()+
                                "min_ids="+chunk_posId+"rightmost="+markable.getRightmostDiscoursePosition());
                    } else {
                        buf.append(' ');
                        buf.append(pos_markables[pos-pos1].getAttributeValue("tag"));
                    }
                }
            }
            return buf.substring(1);
        }
    }

    @Override
    public String getHeadPOS(Markable markable) {
// allow multiword heads (olga)

        MiniDiscourse doc = markable.getMarkableLevel().getDocument();
        MarkableLevel pos = doc.getMarkableLevelByName(MarkableLevels.DEFAULT_POS_LEVEL);

        String head_posId = markable.getAttributeValue(AbstractLanguagePlugin.HEAD_POS);
        if (head_posId == null) return "*NULL*";
        String[] ranges = MarkableHelper.parseRanges(head_posId);
        int[] positions;
        try {
        	positions = new int[] {Integer.parseInt(head_posId)};
        } catch (NumberFormatException ex) {
        	positions = doc.getPositions(ranges);
        }
        StringBuffer buf = new StringBuffer();
          for (int p : positions) {
              List<Markable> pos_markables=
                      pos.getMarkablesAtDiscoursePosition(p);
              if (pos_markables.size()!=1) {
                  System.out.format("for ranges=%s: %s",head_posId,pos_markables);
              }
              buf.append(' ');
              buf.append(pos_markables.get(0).getAttributeValue("tag"));
        }
        return buf.substring(1);
 
    }

    public SemanticClass getSemanticClass(String sem_type, String gender) {
        return SemanticClass.getFromString(sem_type);
    }

    public String lookupAlias(String original, TableName table) {
        Map<String,String> map=aliasTables.get(table);
        if (map==null) {
            return null;
        } else {
            return map.get(original);
        }
    }
    
    public boolean isInAnimateList(String string) {
    	for (String item : animate_list) {
    		if (item.equals(string)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isInInanimateList(String string) {
    	for (String item : inanimate_list) {
    		if (item.equals(string)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isInNeutralList(String string) {
    	for (String item : neutral_list) {
    		if (item.equals(string)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isInMaleList(String string) {
    	for (String item : male_list) {
    		if (item.equals(string)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isInFemaleList(String string) {
    	for (String item : female_list) {
    		if (item.equals(string)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isExpletiveRB(Mention m) {
        if (ConfigProperties.getInstance().getDbgPrint())
        System.out.println(getClass().getCanonicalName()+".isExpletiveRB is not supported yet.");
        return false;
    }

    // TODO: do something sensible here AND get rid of ModCollinsHeadFinder
    public Tree[] calcParseExtra(Tree sentTree, int startWord, int endWord, Tree prsHead, HeadFinder StHeadFinder) {
      return new Tree[0];
    }

}
