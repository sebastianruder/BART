/*
 * Mention.java
 * 
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

/* ToDo for parseheads -- make it optional (for corpora with annotated heads) 
   the option is supported (do_not_use_parsehead), but not in config
*/
package elkfed.coref.mentions;

import static elkfed.lang.EnglishLinguisticConstants.PRONOUN;
import static elkfed.lang.EnglishLinguisticConstants.SINGULAR_PRONOUN_ADJ;
import static elkfed.mmax.pipeline.MarkableCreator.ISPRENOMINAL_ATTRIBUTE;
import static elkfed.mmax.pipeline.MarkableCreator.SENTENCE_ID_ATTRIBUTE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.international.tuebadz.*;
import edu.stanford.nlp.trees.Tree;
import elkfed.config.ConfigProperties;
import elkfed.coref.discourse_entities.DiscourseEntity;
import elkfed.coref.utterances.Utterance;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.GermanLinguisticConstants;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.MentionType;
import elkfed.lang.MentionType.Features;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.nlp.util.Gender;
import elkfed.nlp.util.Number;
import elkfed.nlp.util.NameStructure;
/* for new (parse) heads */


/**
 * Takes a markable and wraps it in a mention object.
 * Information about the mention (ex: gender information,
 * plurality, etc.) is determined as well.
 *
 * @author vae2101
 *
 */
public class Mention implements Comparable<Mention> {
	
	private static final LanguagePlugin langPlugin = ConfigProperties
			.getInstance().getLanguagePlugin();
	
    private static final Logger _logger=Logger.getLogger("elkfed.mentions");

    final private boolean do_not_use_parsehead=false; // set to true for old-style (aka "mmax") head computation always
    
    // Mention information
    final private MentionType _mentionType;
    final private String headString;
    final private String headLemma;
    // the parts of speech of the head
    final private String headPos;
    final private Markable _markable;
    final private String _markableString;
    final private String _enamexType;
    private String _setID = null;
    private int _mentionIdx; // in sentence
    final private MiniDiscourse _document;
    
    final private int _sentId;
    private int _sentStart;
    private int _sentEnd;
    private Tree _sentenceTree;
    private Tree _sentenceTreeDiscIds;
    private Tree _lowestProjection;  //the smallest np containing the mention, modulo embedding (looks like)
    public Tree _highestProjection; // the largest np containing the mention, modulo embedding

    private Tree _minparsespan; //minimal parse subtree that represents the mention 
    private Tree _minnpparsespan; //minimal parse np-subtree that represents the mention 
    private Tree _maxnpparsespan; // maximal parse np-subtree that represents the mention

    public List<Tree> _premodifiers;
    public List<Tree> _postmodifiers;
    private int _startWord;
    private int _endWord;
    private HashMap<String, String> _nameStructure;
    
    private Utterance _utterance;
    private int _posInUtterance;
    private boolean isFirstMention; // this seems to be first in a sentence, not dnew (olga)

    
    private DiscourseEntity _discourseEntity;
        
/* for parse heads */
     private Tree _ParseHead;
     private TueBaDZHeadFinder _headFinder;
     
     public TueBaDZHeadFinder getStHeadFinder() {
         if (_headFinder==null)
           _headFinder= new TueBaDZHeadFinder();
         return _headFinder;
     }


    public Tree getHighestNP() {
      return _highestProjection;
    }
    public Tree getLowestNP() {
      return _lowestProjection;
    }
    public Tree getMinParseTree() {
      return _minparsespan;
    }
    public Tree getMinNPParseTree() {
      return _minnpparsespan;
    }
    public Tree getMaxNPParseTree() {
      return _maxnpparsespan;
    }


    public Tree getParseHead() {
         if (do_not_use_parsehead) return null;
         return _ParseHead;
     }
    public String getHeadString() {
         if (_ParseHead==null) return headString;
         return _ParseHead.value();
    }

    public String getHeadLemma() {
         if (_ParseHead==null) return headLemma;
         return headLemma;
//         return _ParseHead.?? //ToDo: find out what to put here
    }

    public String getHeadPOS() {
         if (_ParseHead==null) return headPos;
         if (_ParseHead.parent(_sentenceTree)==null) return headPos;
         String st= _ParseHead.parent(_sentenceTree).label().toString();
         if (st==null) return "UNKNOWN";
         if (st.equals("")) return "UNKNOWN";
         if (st.equals(" ")) return "UNKNOWN";
         if (st.equals("''")) return "QUOTES"; //for WEKA: error in HeadPos feature otherwise
         return st;
   }

    public void SetNumber() {
        if (_ParseHead == null ) {
// do nothing -- rely on the number as determined by the language plugin instead
          return;

        }else{
// check pos tags
          String headpos=getHeadPOS();
          boolean issing=true;

       // if pronoun, check sing/plural pronoun
          if (getHeadString().toLowerCase().matches(PRONOUN)) {
            issing=getHeadString().toLowerCase().matches(SINGULAR_PRONOUN_ADJ);
         } else {
            if (headpos.equals("CC")) issing=false;
            if (headpos.equals("NNS")) issing=false;
            if (headpos.equals("NNPS")) issing=false;

            _mentionType.features.remove(MentionType.Features.isSingular);
            _mentionType.features.remove(MentionType.Features.isPlural);
          }
          if (issing)
            _mentionType.features.add(MentionType.Features.isSingular);
          else
            _mentionType.features.add(MentionType.Features.isPlural);
        }
    }




    /**
     * Constructs a mention object and populates all necessary local variables,
     * taking a markable and an MMAX document as arguments.
     *
     */
    public Mention(Markable markable, MiniDiscourse doc) {
        LanguagePlugin lang_plugin=
                ConfigProperties.getInstance().getLanguagePlugin();
        _markable = markable;
        _document = doc;
        _markableString = lang_plugin.markableString(markable);
        _enamexType = lang_plugin.enamexType(markable);
        
        headString = lang_plugin.getHead(markable);
        headLemma = lang_plugin.getHeadLemma(markable);
        String hp = lang_plugin.getHeadPOS(markable);
        if (hp == null || hp.equals("")) {
          headPos="UNKNOWN";
        }else{
          if (hp.equals("''")) headPos="QUOTES";
           else
          headPos=hp;
        }
        // get semclass, gender, number
        _mentionType=lang_plugin.calcMentionType(markable);
        
        _sentId = Integer.parseInt(getMarkable().getAttributeValue(
                SENTENCE_ID_ATTRIBUTE));
        
        // Utterance - no need to set??
        //_utterance=null;
        //_posInUtterance = -1;
        isFirstMention = false;
    
        
        // Name internal stucture

        if (getProperName()) {
            _nameStructure = calcNameStructure();
        
        }
      
   }

    public MentionType mentionType() {
        return _mentionType;
    }

    public boolean getProperName() {
        return _mentionType.features.contains(MentionType.Features.isProperName);
    }

    public void setMentionIdx(int i) {
        _mentionIdx = i;
    }

    public int getMentionIdx() {
        return _mentionIdx;
    }
    /** returns true if mention is a pronoun */
    public boolean getPronoun(){
        return _mentionType.features.contains(MentionType.Features.isPronoun);
    }
    public boolean getReflPronoun(){
        return _mentionType.features.contains(MentionType.Features.isReflexive);
    }
    public boolean getPossPronoun(){
        return _mentionType.features.contains(MentionType.Features.isPossPronoun);
    }
    public boolean getRelPronoun(){
        return _mentionType.features.contains(MentionType.Features.isRelative);
    }
    public boolean getPersPronoun(){
        return _mentionType.features.contains(MentionType.Features.isPersPronoun);
    }
    public boolean getDefinite(){
        return _mentionType.features.contains(MentionType.Features.isDefinite);
    }
    public boolean getDnewDeterminer(){
        return _mentionType.features.contains(MentionType.Features.isDnewDeterminer);
    }

    public boolean getIndefinite() {
        return _mentionType.features.contains(MentionType.Features.isIndefinite);
    }

    public boolean getDemonstrative(){
        return _mentionType.features.contains(MentionType.Features.isDemonstrative);
    }
    public boolean getDemPronoun(){
        return _mentionType.features.contains(MentionType.Features.isDemPronoun);
    }
    public boolean getDemNominal(){
        return _mentionType.features.contains(MentionType.Features.isDemNominal);
    }
    
    
    /**
     *  Return if mention is singular
     */
    public boolean getNumber(){
        return _mentionType.features.contains(MentionType.Features.isSingular);
    }
    
    /**
     *  return singular, plural or unknown
     */
//    public Number getNumber(){
//    	
//    	// if mention's words contain undefined article, mention is singular
//    	
//    	if (	getDiscourseElementsByLevel("lemma").contains("ein") || 
//    			getDiscourseElementsByLevel("lemma").contains("eine")){
//    		return Number.SINGULAR;
//    	}
//    	
//    	// if a certain unambigious pronoun occurs in mention words --> mention's number is pronoun's number
//    	
//    	for (String s: getDiscourseElementsByLevel("lemma")){
//    		if (s.matches(GermanLinguisticConstants.FIRSTPERSON_SG_PRONOUNS)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (s.matches(GermanLinguisticConstants.SECONDPERSON_SG_PRONOUNS)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (s.matches(GermanLinguisticConstants.THIRDPERSON_SG_PRONOUNS) &&
//    				!s.matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (s.matches(GermanLinguisticConstants.FIRSTPERSON_PL_PRONOUNS)){
//    			return Number.PLURAL;
//    		}
//    		
//    		if (s.matches(GermanLinguisticConstants.SECONDPERSON_PL_PRONOUNS) &&
//    				!s.matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)){
//    			return Number.PLURAL;
//    		}
//    		
//    		if (s.matches(GermanLinguisticConstants.THIRDPERSON_PL_PRONOUNS) &&
//    				!s.matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)){
//    			return Number.PLURAL;
//    		}
//    		
//    	}
//    	
//    	// check for words that only occur in singular or plural
//    	
//    	for (String s: getDiscourseElementsByLevel("lemma")){
//    		if (langPlugin.isInSingularList(s)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (langPlugin.isInPluralList(s)){
//    			return Number.PLURAL;
//    		}
//    		
//    	}
//    	
//    	// if mention is a pronoun, check pronoun list for number
//    	
//    	if (getPronoun()){
//    		if (getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.FIRSTPERSON_SG_PRONOUNS)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.SECONDPERSON_SG_PRONOUNS)){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (
//    				getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.THIRDPERSON_SG_PRONOUNS) &&
//    				!getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)
//    				){
//    			return Number.SINGULAR;
//    		}
//    		
//    		if (getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.FIRSTPERSON_PL_PRONOUNS)){
//    			return Number.PLURAL;
//    		}
//    		
//    		if (
//    				getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.SECONDPERSON_PL_PRONOUNS) &&
//    				!getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)
//    				){
//    			return Number.PLURAL;
//    		}
//    		
//    		if (
//    				getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.THIRDPERSON_PL_PRONOUNS) &&
//    				!getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)
//    				){
//    			return Number.PLURAL;
//    		}
//    		
//    		if (
//    				getMarkable().toString().replace("[", "").replace("]", "").matches(GermanLinguisticConstants.AMBIGOUS_SG_PL_PRONOUNS)
//    				){
//    			return Number.UNKNOWN;
//    		}
//    	}
//    		
//    
//    		// stanford idea: all ne are singular except for organisations   		
//    		
//    		if (getProperName() && !(getSemanticClass().equals(SemanticClass.ORGANIZATION) || getSemanticClass().equals(SemanticClass.UNKNOWN))){
//        		return Number.SINGULAR;
//        	}
//    		
//    		// to try: compare original form and lemma form, check for typical plural signs, e.g.  +-e, +-es, +-s, +-en, +-n
//    		
//
//        return Number.UNKNOWN;
//    }
    
    
    
    /**
     *  Return mention person
     */
    public boolean getIsFirstSecondPerson() {
        return _mentionType.features.contains(MentionType.Features.isFirstSecondPerson);
    }
    
    /**
     *  Return mention gender
     */
    public Gender getGender(){ return _mentionType.gender;}
    

    /**
     *  Return mention head string of NP
     */
/*    public String getHeadString(){return headString;} */ //Olga: use prs first (cf. above)

    /**
     * Return mention head POS of NP
     * @author samuel
     * @return mention head POS of NP
     */
/*    public String getHeadPOS(){return headPos;} */ //Olga: commented -- use prs first (cf. new getHeadPos())

    
    /**
     * return the head string of the NP as it is needed
     * for pattern searches or WP queries
     */
    public String getHeadOrName() {
        LanguagePlugin lang_plugin=
                ConfigProperties.getInstance().getLanguagePlugin();
        return lang_plugin.getHeadOrName(getMarkable());
    }

    //TODO: is this necessary?
    //      Shouldn't getHeadOrName do the same?
    // Similar to lemma, but kepping capitals
    // to use for the strudel similarity
    public String getHeadStr4Strudel(Mention m) {
        String str;
        if (!m.getNumber()) {
            str = m.getHeadOrName();
        } else {
            str = m.getHeadString();
        }
        return str;
    }

    public Set<Features> getFeatures() {
        return _mentionType.features;
    }
    
    /**
     * Return the mention's name internal structure.
     * Only really makes sense if mention is a proper name
     */
    public HashMap<String,String> getNameStructure() { return _nameStructure; }
    
    
    /**
     *  Return mentions markable
     */
    public Markable getMarkable(){return _markable;}
    
    /**
     *  Return mentions markable string
     */
    public String getMarkableString() {return _markableString;}
    
    /**
     *  Return mentions document
     */
    public MiniDiscourse getDocument() { return _document; }
    
    /**
     *  Return mentions semantic class
     */
    public SemanticClass getSemanticClass() {
        return _mentionType.semanticClass;
    }

    /**
    * Takes the sentence tree of this mention and replaces the unlexicalized
    * tokens in the leaves with their discourse ids. Usefull to derive
    * spans based on syntax.
    *
    * @author samuel
    * @param sentTree original sentence tree
    * @return Tree with discourse ids in leaves
    */
    private Tree getSentenceTreeWithDiscurseIdsInLeaves(Tree sentTree) {
        Tree result = sentTree.deepCopy();
        List<Tree> leaves = result.getLeaves();
        String[] discElements = getSentenceDiscourseElements();
        String[] discIDs = getSentenceDiscourseElementIDs();
        if(discElements.length != leaves.size() || discIDs.length != leaves.size()) {
            throw new RuntimeException("SentenceTree leaves and discourse elements do not match for mention " + this.getMarkable().getID());
        }

        /* sometimes tueba-d/z punctuations in the parse tree are
         * out of context appended to the root of the tree hence
         * the corrector part
         *
         * yv: ... and we need to unescape those words for the
         * semeval2mmax output. duh.
         */
        int corrector = 0;
        for (int i = 0; i < discElements.length; i++) {
            String leafWord = leaves.get(i - corrector).value().toLowerCase();
            leafWord=leafWord.replaceAll("-lrb-", "(");
            leafWord=leafWord.replaceAll("-rrb-", ")");
            leafWord=leafWord.replaceAll("\\\\", "");
            String discWord = discElements[i].replaceAll("\\\\", "");
            if (discWord.equalsIgnoreCase(leafWord)) {
                leaves.get(i - corrector).setValue(discIDs[i]);
            } else {
                System.err.format("non-matching: %s vs %s\n",
                        leafWord, discElements[i]);
                corrector++;
            }
        }
        return result;
    }

    /** set parse information. This should only be used by
     *  CorefMentionFactory */
    public void setParseInfo(Tree sentTree, int start, int end) {
        _sentenceTree=sentTree;
        _startWord=start;
        _endWord=end;
        LanguagePlugin lang_plugin=
                ConfigProperties.getInstance().getLanguagePlugin();
        List<Tree>[] parseInfo=lang_plugin.calcParseInfo(
                sentTree, start, end,
                _mentionType);
        List<Tree> projections=parseInfo[0];
        _premodifiers=parseInfo[1];
        _postmodifiers=parseInfo[2];
        _lowestProjection=projections.get(0);
        _highestProjection=projections.get(projections.size()-1);
        _ParseHead=_lowestProjection.headTerminal(getStHeadFinder());

/* check that the head is inside the markable's min, set null otherwise */

        List<Tree> Leaves =sentTree.getLeaves();
        int ind=start;
        boolean found=false;
        int lsz=Leaves.size();

        while(ind<=end && ind<lsz && found==false) {
          Tree CurLeaf=Leaves.get(ind);
          if (CurLeaf == _ParseHead) found=true;
          ind++;
        }
        if (found==false) _ParseHead=null;

        Tree[] parseExtra=lang_plugin.calcParseExtra(sentTree, start, end, _ParseHead,getStHeadFinder());
        
        _minparsespan=parseExtra[0];
        _minnpparsespan=parseExtra[1];
        _maxnpparsespan=parseExtra[2];


/* adjust number */
        SetNumber();



        if (_logger.isLoggable(Level.FINE))
        {
            _logger.fine(String.format("Parse info for '%s'",toString()));
            _logger.fine("headOrName: "+getHeadOrName());
            _logger.fine("lowest: "+_lowestProjection);
            _logger.fine("highest: "+_highestProjection);
            _logger.fine("premodify: "+_premodifiers);
            _logger.fine("postmodify: "+_postmodifiers);
        }
    }
    
    /* Utterance info */
    
    public Utterance getUtterance() { return _utterance; }
    public void setUtterance(Utterance utt) {      
        _utterance=utt;
        _utterance.addCF( this );
    }
    
    public int getUttPos() { return _posInUtterance;}
    public void setUttPos(int pos) {
        _posInUtterance=pos;
        if (_posInUtterance==0) { 
            isFirstMention=true; 
        }
    }
    
    public boolean getIsFirstMention() { return isFirstMention; }

    
    // discourse entities
    public DiscourseEntity getDiscourseEntity() { return _discourseEntity; }
    public void createDiscourseEntity() {
        _discourseEntity = new DiscourseEntity(this);
    }
  
    /**
     * Sorting
     */
    public int compareTo(Mention m) {
        if (_startWord < m.getStartWord()) {
            return -1;
        } 
        else if ( m.getStartWord() < _startWord) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /** Uses some heuristics to determine internal structure in names.
     *  i.e. Forename, Middle, Surname, etc.
     *
     */
    private HashMap<String,String> calcNameStructure() {
        return NameStructure.getNameStructure(_markableString);
    }
    

    
    /** returns the parse for the containing sentence */
    public Tree getSentenceTree() { return _sentenceTree; }
    /**
     * @author samuel
     * @return returns the parse for the containing sentence with disc ids in leaves
     */
    public Tree getSentenceTreeWithDiscIds() {
        if(_sentenceTreeDiscIds==null) {
            _sentenceTreeDiscIds = getSentenceTreeWithDiscurseIdsInLeaves(_sentenceTree);
        } 
        return _sentenceTreeDiscIds;
    }
    /** returns the baseNP node for that markable */
    public Tree getLowestProjection() { return _lowestProjection; }
    /** returns the highest projection for that markable */
    public Tree getHighestProjection() { return _highestProjection; }
    
    /** returns the sentence-relative word index of the
     *  start of the markable */
    public int getStartWord() { return _startWord; }
    public void setStartWord(int i) { _startWord = i; }
    
    /** returns the sentence-relative word index of the
     *  end of the markable */
    public int getEndWord() { return _endWord; }
    public void setEndWord(int i) { _endWord = i; }
    
    
    /**Determine whether mention is coreferent with a given mention
     */
    public boolean isCoreferent(Mention m) {
        if (this._setID == null)
            return false;
        else if (_setID.equals(m._setID))
            return true;
        else
            return false;
    }
    
    /** see if two mentions have overlapping spans -
     *  actually, a better approximation to syntactic embedding
     *  would probably make sense here, as in
     *  [1 the guardian] of [2[3 his] treasure]
     *  we would like to allow 1--3, but not 1--2(?)
     */
    public boolean overlapsWith(Mention m) {
        Markable m1=getMarkable();
        Markable m2=m.getMarkable();
        if (m1.getRightmostDiscoursePosition() <=
                m2.getLeftmostDiscoursePosition())
            return false;
        else if (m2.getRightmostDiscoursePosition() <=
                m1.getLeftmostDiscoursePosition())
            return false;
        else
            return true;
        
    }
    
    /** see if two mentions have overlapping spans -
     *  like in [1 [2 his] treasure]
     */
    public boolean embeds(Mention m) {
        Markable m1=getMarkable();
        Markable m2=m.getMarkable();
        return
                m1.getLeftmostDiscoursePosition() <= m2.getLeftmostDiscoursePosition()
                &&
                m1.getRightmostDiscoursePosition() >= m2.getRightmostDiscoursePosition();
    }
    
    public void setSetID(String setid) {
        _setID = setid;
    }
    
    public String getSetID() {
       return _setID;
    }
    
    public static String getMarkableString(final Markable markable) {
        return new StringBuffer(markable.toString()).
                deleteCharAt(markable.toString().length()-1).deleteCharAt(0).toString();
    }
    
    public int getSentId() {
        return _sentId;
    }
    
    public void setSentenceStart(int start) {
        _sentStart = start;
    }

    public int getSentenceStart() {
        return _sentStart;
    }

    public void setSentenceEnd(int end) {
        _sentEnd = end;
    }

    public int getSentenceEnd() {
        return _sentEnd;
    }

    @Override
    public String toString() {
        return _markableString;
    }
    
    public String[] getLeftContext(int nWords) {
        int n;
        int posN=getMarkable().getLeftmostDiscoursePosition();
        if (posN<nWords)
            n=posN;
        else
            n=nWords;
        String[] result=new String[n];
        for (int token = posN-n; token < posN; token++) {
            result[token-posN+n]=_document
                    .getDiscourseElementAtDiscoursePosition(token).toString();
        }
        return result;
    }
    
    public String[] getRightContext(int nWords) {
        int posN=getMarkable().getRightmostDiscoursePosition()+1;
        if (_document.getTokens().length<posN+nWords) {
            nWords=_document.getTokens().length-posN;
        }
        String[] result=new String[nWords];
        for (int token = posN; token < posN+nWords; token++) {
            result[token-posN]=_document
                    .getDiscourseElementAtDiscoursePosition(token).toString();
        }
        return result;
    }
    
    /** Checks whether this mention is a named entity */
    public boolean isEnamex()
    {
        return _mentionType.features.contains(Features.isEnamex);
    }

    /** Checks whether this mention is a coordinated NP */
    /* probably only works for Italian though (with "np.coord" annotated in the data)*/
    
    public boolean isCoord()
    {
        return _mentionType.features.contains(MentionType.Features.isCoord);
    }
    
    /** Gets the type of a named entity */
    public String getEnamexType()
    { return _enamexType; }

    public List<Tree> getPostmodifiers() {
        return _postmodifiers;
    }

    
    public List<Tree> getPremodifiers() {
        return _premodifiers;
    }
    
    public String getRootPath()
    {
        Tree top=getSentenceTree();
        Tree here=getHighestProjection();
        StringBuffer sb=new StringBuffer();
        String lastValue=null;
        while (here!=top)
        {
            here=here.parent(top);
            String val=here.value();
            if (!val.equals(lastValue))
                sb.append(here.value()).append(".");
            lastValue=val;
        }
        return sb.toString();
    }
    
    public boolean isPrenominal()
    {
        return Boolean.parseBoolean(
                     getMarkable().getAttributeValue(ISPRENOMINAL_ATTRIBUTE));
    }

    public String computeAppType(Mention np) {

/* this probably shouldn't work properly
* it's used by discourse_entities
* but hP and lP are inconsistent with it
* ToDo: unify with other appo stuff
* (olga)
*/

        String headAppo = null;
        Tree lowestProjection = np.getLowestProjection();
        Tree highestProjection = np.getHighestProjection();
        if (highestProjection == null) return headAppo;
        if (highestProjection.children() == null) return headAppo;
        if (highestProjection.children().length <=2) return headAppo;
        if (!highestProjection.children()[1].label().toString().equals(",")) 
           return headAppo;

        Tree h0=highestProjection.children()[0].headPreTerminal(getStHeadFinder());
        Tree h2=highestProjection.children()[2].headPreTerminal(getStHeadFinder());
        if (h0==null)  return headAppo;
        if (h2==null)  return headAppo;

        if (highestProjection.children()[0] == lowestProjection &&
            highestProjection.children()[2].label().toString().equals("NP") &&
            !h0.label().toString().equals("NNP")&&
            h2.label().toString().equals("NN")) 

                return highestProjection.children()[2].headTerminal(getStHeadFinder()).toString();


        if (highestProjection.children()[0].label().toString().equals("NP") &&
                    highestProjection.children()[2] ==lowestProjection &&
                    h2.label().toString().equals("NNP")&&
                    h0.label().toString().equals("NN")) 
          return highestProjection.children()[0].headTerminal(getStHeadFinder()).toString();


       return headAppo;
    }

    /**
     *  @author samuel
     *  @param  leftMostDiscursePos left most discourse position
     *  @param  rightMostDiscursePos  most discourse position
     *  @return  The subtree inside a span from the current mentions sentenceTreeWithDiscIds
     */
    public Tree getMarkableTree(int leftMostDiscursePos, int rightMostDiscursePos) {
        Tree sentenceTree = this.getSentenceTreeWithDiscIds();
        List<Tree> leaves = sentenceTree.getLeaves();

        int start = leftMostDiscursePos - this.getSentenceStart();
        int end = rightMostDiscursePos - this.getSentenceStart();
        Tree startNode = leaves.get(start);
        Tree endNode = leaves.get(end);

        Tree parentNode = startNode;
        while (parentNode != null && !parentNode.dominates(endNode)) {
            parentNode = parentNode.parent(sentenceTree);
        }

        return parentNode;
    }

    /**
     * @author samuel
     * @param  postag  Parts of speech tag
     * @return  Array of disc ids representing the highest projecting phrase inside the markables subtree with the given postag
     */
    public ArrayList<String> getHighestProjectingPhraseWithPOS(String postag) {
        return getHighestProjectingPhraseWithPOS(getMarkableSubTree(), postag);
    }

     /**
     * @author samuel
     * @param markableSubtree A subtree of the sentenceTreeWithDiscIds
     * @param  postag Parts of speech tag
     * @return  Array of disc ids representing the highest projecting phrase inside the markables subtree with the given postag
     */
    public ArrayList<String> getHighestProjectingPhraseWithPOS(Tree markableSubtree, String postag) {
        LinkedList<Tree> stack = new LinkedList<Tree>();
        stack.add(markableSubtree);
        ArrayList<String> result = new ArrayList<String>();
        Tree head = null;
        fifo:
        while (stack.size() > 0) {
            head = stack.removeFirst();
            for (Tree child : head.children()) {
                if (child.value().toString().equalsIgnoreCase(postag)) {
                    break fifo;
                } else if (child.value().toString().equalsIgnoreCase("nx")) {
                    stack.add(child);
                }
            }
        }
        for (Tree child : head.children()) {
            if (child.value().toString().equalsIgnoreCase(postag)) {
                result.add(child.children()[0].value().toString());
            }
        }
        if (result.size() > 0) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * @author samuel
     * @param level  markable level name
     * @return
     */
    public Markable[] getSentenceMarkables(String level) {
        Markable m = this.getMarkable();
        MiniDiscourse doc = m.getMarkableLevel().getDocument();
        MarkableLevel markableLevel = doc.getMarkableLevelByName(level);
        return getSentenceMarkables(markableLevel);
    }

    /**
     * @author samuel
     * @return discourse elements in this mentions sentence
     */
    public String[] getSentenceDiscourseElements() {
        return _document.getDiscourseElements(getSentenceStart(), getSentenceEnd());
    }

    /**
     * @author samuel
     * @return discourse elements in this mentions sentence
     */
    public String[] getSentenceDiscourseEleme1ntIDs() {
        return _document.getDiscourseElementIDs(getSentenceStart(), getSentenceEnd());
    }
    
    public List<String > getDiscourseElementsByLevel(String markableLevel) {
    	List<String> words = new ArrayList<String>();
    	MarkableLevel level = _document.getMarkableLevelByName(markableLevel);
		int from = _markable.getLeftmostDiscoursePosition();
		int to = _markable.getRightmostDiscoursePosition();        

        Markable[] markables = level.getMarkablesAtSpan(from, to);
        for (Markable markable : markables) {
            words.add(markable.getAttributeValue("tag"));
            
        }
    	return words;
    }

    /**
    * @author samuel
    * @param level markable level name
    * @return this mentions sentence markables
    */
    public Markable[] getSentenceMarkables(MarkableLevel level) {
        return level.getMarkablesAtSpan(this.getSentenceStart(), this.getSentenceEnd());
    }

    /**
     * @author samuel
     * @return  The markables subtree with disc ids in leaves
     */
    public Tree getMarkableSubTree() {
        return getMarkableTree(this.getMarkable().getLeftmostDiscoursePosition(), this.getMarkable().getRightmostDiscoursePosition());
    }

    //TODO further cleanup of Kepa stuff - move to LanguagePlugin or delete
    public String computePredicationType(Mention np) {
        String predType = null;
        Tree mentionTree = np.getHighestProjection();
        Tree sentenceTree = np.getSentenceTree();
        Tree parentNode = null;
        if (mentionTree==null && ConfigProperties.getInstance().getDbgPrint()) {
            System.out.println("No mentionTree for "+np.toString());
        }
        if (mentionTree != null ) parentNode=mentionTree.ancestor(1, sentenceTree);
        if (!(parentNode == null) &&
                parentNode.children().length > 1 &&
                parentNode.children()[1].label().toString().equals("VP") &&
                parentNode.children()[1].children().length > 1) {
            String hword10 = parentNode.children()[1].children()[0].headTerminal(new ModCollinsHeadFinder()).toString();
            if (hword10.equals("is") ||
                    hword10.equals("are") ||
                    hword10.equals("was") ||
                    hword10.equals("were")) {
                Tree pchild11 = parentNode.children()[1].children()[1];
                if (pchild11 != null) {// &&
                    if (pchild11.label().toString().equals("NP")) {
                        String pchild11_headpos = pchild11.headPreTerminal(new ModCollinsHeadFinder()).label().toString();
                        if (!pchild11_headpos.equals("JJS") &&
                                !pchild11_headpos.equals("NNP")) {
                            predType = pchild11.headTerminal(new ModCollinsHeadFinder()).toString();
                        }
                    }
                }
            }
        }
        return predType;
    }

    public String computePredicationAttr(Mention np) {
        String predAttr = null;
        Tree mentionTree = np.getHighestProjection();
        Tree sentenceTree = np.getSentenceTree();
        Tree parentNode=null;
        if (!(mentionTree == null)){
        parentNode = mentionTree.ancestor(1, sentenceTree);
        }
        if (!(parentNode == null) &&
                parentNode.children().length > 1 &&
                parentNode.children()[1].label().toString().equals("VP") &&
                parentNode.children()[1].children().length > 1) {
            if (parentNode.children()[1].children()[0].headTerminal(new ModCollinsHeadFinder()).toString().equals("is") ||
                    parentNode.children()[1].children()[0].headTerminal(new ModCollinsHeadFinder()).toString().equals("are") ||
                    parentNode.children()[1].children()[0].headTerminal(new ModCollinsHeadFinder()).toString().equals("was") ||
                    parentNode.children()[1].children()[0].headTerminal(new ModCollinsHeadFinder()).toString().equals("were")) {
                if (!(parentNode.children()[1].children()[1] == null)) {// &&
                    if (parentNode.children()[1].children()[1].label().toString().equals("ADJP")) {
                        predAttr = parentNode.children()[1].children()[1].headTerminal(new ModCollinsHeadFinder()).toString();
 //                       System.out.println("ATTR!!! " + predAttr);
                    } else if (parentNode.children()[1].children()[1].label().toString().equals("NP") &&
                            parentNode.children()[1].children()[1].headPreTerminal(new ModCollinsHeadFinder()).label().toString().equals("JJS")) {
                        predAttr = parentNode.children()[1].children()[1].headTerminal(new ModCollinsHeadFinder()).toString();
//                        System.out.println("ATTR!!! " + predAttr);
                    }
                }
            }
        }
        return predAttr;
    }

    /**
     *  @author samuel
     *  @param  discIds discourse ids
     *  @param  markableLevel markable level name
     *  @return a joined string of the markable levels tag attribute
     */
    public String getJoinedStringFromDiscIds(ArrayList<String> discIds, String markableLevel) {
        if (discIds != null) {
            int from = MarkableHelper.parseId(discIds.get(0), "word");
            int to = MarkableHelper.parseId(discIds.get(discIds.size() - 1), "word");
            return getJoinedStringFromDiscIds(from, to, markableLevel);
        } else {
            return null;
        }
    }

   /**
     *  @author samuel
     *  @param  from  left most discourse position
     *  @param  to  right most discourse position
     *  @param  markableLevel  markable level name
     *  @return a joined string of the markable levels tag attribute
     */
    public String getJoinedStringFromDiscIds(int from, int to, String markableLevel) {
        MarkableLevel level = _document.getMarkableLevelByName(markableLevel);

        StringBuffer result = new StringBuffer();

        Markable[] markables = level.getMarkablesAtSpan(from - 1, to - 1);
        for (Markable markable : markables) {
            result.append(markable.getAttributeValue("tag"));
            result.append(" ");
        }

        if (result.length() > 0) {
            return result.toString().trim();
        } else {
            return null;
        }
    }
    public void setDiscourseEntity(DiscourseEntity de) {
    	_discourseEntity = de;
    }
    public void linkToAntecedent(Mention ante) {
        ante.getDiscourseEntity().merge(this);
        _discourseEntity = ante.getDiscourseEntity();
    }
}
