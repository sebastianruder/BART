/*
 * SyntaxTreeFeature.java
 *
 * Created on July 24, 2007, 12:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.knowledge;

/**
 *
 * @author Xiaofeng Yang
 *  Institute for Infocomm Research
 *  xiaofengy@i2r.a-star.edu.sg
 */


import java.util.List;

import edu.stanford.nlp.trees.Tree;
import elkfed.coref.mentions.Mention;

public class SyntaxTreeFeature {
    //int mIndex = 0;
    /***
     *Find the trees that cover both antecedent and anaphor nodes
     *///
    protected  String FindConnectingStruct(Tree AnteTree, Tree AnteTreeRoot, Tree AnaTree, Tree AnaTreeRoot){
        String ReturnStr = "";
        
        
        if (AnteTree == null || AnaTree == null || AnteTreeRoot == null || AnaTreeRoot == null)
            return ReturnStr;
        
        String TreeStr1;
        String TreeStr2;
        StringBuffer tempStr = new StringBuffer();
        
        if (AnteTree == AnaTree){
            tempStr.append("(CANDI-ANA-").append(AnteTree.value());
            tempStr.append(" CANDI-ANA-").append(AnteTree.value());
            tempStr.append(")");
            ReturnStr = tempStr.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        
        if (AnteTree.dominates(AnaTree)){
            tempStr.append(FindTree_1(AnteTree,AnaTree,2, ""));
            int index = tempStr.indexOf(" ");
            StringBuffer tempStr1 = new StringBuffer();
            tempStr1.append("(CANDI-").append(AnteTree.value()).append(" ").append(tempStr.substring(index));
            ReturnStr = tempStr1.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        if (AnaTree.dominates(AnteTree)){
            tempStr.append(FindTree_1(AnaTree,AnteTree,1, ""));
            int index = tempStr.indexOf(" ");
            StringBuffer tempStr1 = new StringBuffer();
            tempStr1.append("(ANA-").append(AnaTree.value()).append(" ").append(tempStr.substring(index));
            ReturnStr = tempStr1.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        
        if (AnteTreeRoot != AnaTreeRoot){
            TreeStr1 = FindTree_1(AnteTreeRoot,AnteTree,1, "");
            TreeStr2 = FindTree_1(AnaTreeRoot,AnaTree,2, "");
            tempStr.append("(s2 ");
            tempStr.append(TreeStr1);
            int SentenceDist = 1;
            for (int I = 0; I < SentenceDist; I++)
                tempStr.append("(s1 s1) ");
            tempStr.append(TreeStr2);
            tempStr.append(" )");
        }else{


            //find the most immediate node that dominate both anaphor and antecedent
            
            Tree ParentTree1 = null;
            Tree ParentTree2 = null;
            
            Tree CommonNode = AnteTree.parent(AnteTreeRoot);
            while (CommonNode != null &&
                   !CommonNode.dominates(AnaTree))
                CommonNode = CommonNode.parent(AnteTreeRoot);
            
            int bFoundAnte = 0;
            for (int I = 0; I < CommonNode.children().length; I++){
                if (bFoundAnte == 0){
                    if ( CommonNode.children()[I] == AnteTree ||
                         CommonNode.children()[I].dominates(AnteTree)){
                        ParentTree1 = CommonNode.children()[I];
                        bFoundAnte = 1;
                    }
                }else{
                    if (    CommonNode.children()[I] == AnaTree ||
                            CommonNode.children()[I].dominates(AnaTree)){
                        ParentTree2 = CommonNode.children()[I];
                        break;
                    }
                }
            }
                        
            TreeStr1 = FindTree_1(ParentTree1,AnteTree,1, "");
            TreeStr2 = FindTree_1(ParentTree2,AnaTree,2, "");

            tempStr.append("(");
            tempStr.append(CommonNode.value());
            tempStr.append(" ");

            for (int I = 0; I < CommonNode.children().length; I++){
                Tree aChildNode =  CommonNode.children()[I];
                if (aChildNode == ParentTree1) {
                    tempStr.append(TreeStr1);
                    continue;
                }

                if (aChildNode == ParentTree2) {
                    tempStr.append(TreeStr2);
                    continue;
                }

                tempStr.append("(");
                tempStr.append(aChildNode.value());
                tempStr.append(" ");
                tempStr.append(aChildNode.value());
                tempStr.append(")");
            }
            tempStr.append(")");
        }

        ReturnStr = tempStr.toString();
        
        ReturnStr = PostProcessTreeString(ReturnStr);
        return ReturnStr;


    }
    
   /***
    *Return the string representing the structure including all the nodes between two nodes as well as their immediate children.
    *It is an internal function called by other function
   */
    protected String FindTree_1(Tree ParentTree, Tree CurTree, int Mark, String UserMarkString){
        if (CurTree == null || ParentTree == null)
            return "";
        
        StringBuffer Buf = new StringBuffer();
        StringBuffer Buf1 = new StringBuffer();
        
	Tree pTree = CurTree;
	Tree UpTree = CurTree.parent(ParentTree);

        if (UserMarkString != "")
            Buf1.append(UserMarkString);
        else{
            if (Mark == 0)
                Buf1.append("(").append(CurTree.value()).append(" ").append(CurTree.value()).append(")");
            if (Mark == 1)
                Buf1.append("(CANDI-").append(CurTree.value()).append(" CANDI-").append(CurTree.value()).append(")");
            if (Mark == 2){
                int bReflexivePron = 0;
                if (CurTree.value().equalsIgnoreCase("PRP")){
                    String aWord = CurTree.children()[0].value();
                    if (aWord.equalsIgnoreCase("himself") || aWord.equalsIgnoreCase("herself") ||
                        aWord.equalsIgnoreCase("themselves") || aWord.equalsIgnoreCase("myself") ||
                        aWord.equalsIgnoreCase("yourselves") || aWord.equalsIgnoreCase("yourself") ||
                        aWord.equalsIgnoreCase("ourselves")  )
                        bReflexivePron = 1;
                }
                if (bReflexivePron == 1)
                    Buf1.append("(ANA-").append("PRP-RR").append(" ANA-").append("PRP-RR").append(")");
                else
                    Buf1.append("(ANA-").append(CurTree.value()).append(" ANA-").append(CurTree.value()).append(")");
            }
        }

        if (ParentTree == CurTree){
            return Buf1.toString();
        }


	do{

                Buf.delete(0,Buf.length());
                Buf.append("(").append(UpTree.value()).append(" ");
                for (int I = 0; I < UpTree.children().length; I++){
                    Tree aChildNode = UpTree.children()[I];
                    if (aChildNode == pTree){
                        Buf.append(Buf1);
                    }else{
                        String aType = aChildNode.value();
                        if ( (aType.equalsIgnoreCase("DT") || aType.equalsIgnoreCase("CC") || aType.equalsIgnoreCase("to") || aType.equalsIgnoreCase("in") || aType.charAt(0) == 'v' || aType.charAt(0) == 'V') &&
                              aChildNode.children().length == 1 && aChildNode.children()[0].children().length == 0)
                            Buf.append("(").append(aChildNode.value()).append(" ").append(aChildNode.children()[0].value()).append(")");
                        else
                            Buf.append("(").append(aChildNode.value()).append(" ").append(aChildNode.value()).append(")");
                    }
                }

                Buf.append(")");
                Buf1.delete(0,Buf1.length());
                Buf1.append(Buf);
		
		if (UpTree == ParentTree)
                    break;
		pTree = UpTree;
		UpTree = UpTree.parent(ParentTree);
	} while(UpTree != null);
	
        return Buf1.toString();

    }

    protected  String FindAppositiveStruct(Tree AnteTree, Tree AnteTreeRoot, Tree AnaTree, Tree AnaTreeRoot){
        String ReturnStr = "";
        Tree CommonNode = null;
        
        if (AnteTreeRoot != AnaTreeRoot){
            return ReturnStr;
        }
        
        
        if (AnteTree.dominates(AnaTree)){
            CommonNode = AnteTree;
        }else        
        if (AnaTree.dominates(AnteTree)){
            CommonNode = AnaTree;
        }else{
           //find the most immediate node that dominate both anaphor and antecedent
            CommonNode = AnteTree.parent(AnteTreeRoot);
            while (CommonNode != null &&
                   !CommonNode.dominates(AnaTree))
                CommonNode = CommonNode.parent(AnteTreeRoot);
        }
        
        if (CommonNode == null)
            return ReturnStr;
        Tree UpperNode = CommonNode;
        while (UpperNode != null && UpperNode.value().charAt(0) != 's' && UpperNode.value().charAt(0) != 'S')
            UpperNode = UpperNode.parent(AnteTreeRoot);
        
        String StrConnecting = FindConnectingStruct(AnteTree, AnteTreeRoot,  AnaTree, AnaTreeRoot);
        ReturnStr = FindTree_1(UpperNode, CommonNode, 0, StrConnecting);
        ReturnStr = PostProcessTreeString(ReturnStr);
        return ReturnStr;
        
    }
    
    
/*
 protected  String FindConnectingStruct_ShowFull(Tree AnteTree, Tree AnteTreeRoot, Tree AnaTree, Tree AnaTreeRoot){
        String ReturnStr = "";
        
        StringBuffer TreeStr;
        String TreeStr1;
        String TreeStr2;
        StringBuffer tempStr = new StringBuffer();
        
        if (AnteTree == AnaTree){
            tempStr.append("(CANDI-ANA-").append(AnteTree.value());
            tempStr.append(" CANDI-ANA-").append(AnteTree.value());
            tempStr.append(")");
            ReturnStr = tempStr.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        
        if (AnteTree.dominates(AnaTree)){
            tempStr.append(FindTree_1(AnteTree,AnaTree,2));
            int index = tempStr.indexOf(" ");
            StringBuffer tempStr1 = new StringBuffer();
            tempStr1.append("(CANDI-").append(AnteTree.value()).append(" ").append(tempStr.substring(index));
            ReturnStr = tempStr1.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        if (AnaTree.dominates(AnteTree)){
            tempStr.append(FindTree_1(AnaTree,AnteTree,1));
            int index = tempStr.indexOf(" ");
            StringBuffer tempStr1 = new StringBuffer();
            tempStr1.append("(ANA-").append(AnaTree.value()).append(" ").append(tempStr.substring(index));
            ReturnStr = tempStr1.toString();
            ReturnStr = PostProcessTreeString(ReturnStr);
            return ReturnStr;
        }
        
        
        if (AnteTreeRoot != AnaTreeRoot){
            TreeStr1 = FindTree_1(AnteTreeRoot,AnteTree,1);
            TreeStr2 = FindTree_1(AnaTreeRoot,AnaTree,2);
            tempStr.append("(s2 ");
            tempStr.append(TreeStr1);
            int SentenceDist = 1;
            for (int I = 0; I < SentenceDist; I++)
                tempStr.append("(s1 s1) ");
            tempStr.append(TreeStr2);
            tempStr.append(" )");
        }else{


            //find the most immediate node that dominate both anaphor and antecedent
            
            Tree ParentTree1 = null;
            Tree ParentTree2 = null;
            
            Tree CommonNode = AnteTree.parent(AnteTreeRoot);
            while (CommonNode != null &&
                   !CommonNode.dominates(AnaTree))
                CommonNode = CommonNode.parent(AnteTreeRoot);
            
            int bFoundAnte = 0;
            for (int I = 0; I < CommonNode.children().length; I++){
                if (bFoundAnte == 0){
                    if ( CommonNode.children()[I] == AnteTree ||
                         CommonNode.children()[I].dominates(AnteTree)){
                        ParentTree1 = CommonNode.children()[I];
                        bFoundAnte = 1;
                    }
                }else{
                    if (    CommonNode.children()[I] == AnaTree ||
                            CommonNode.children()[I].dominates(AnaTree)){
                        ParentTree2 = CommonNode.children()[I];
                        break;
                    }
                }
            }
                        
            TreeStr1 = FindTree_2(ParentTree1,AnteTree,1, false);
            TreeStr2 = FindTree_2(ParentTree2,AnaTree,2, true);

            int bBetween = 0;

            tempStr.append("(");
            tempStr.append(CommonNode.value());
            tempStr.append(" ");

            for (int I = 0; I < CommonNode.children().length; I++){
                Tree aChildNode =  CommonNode.children()[I];
                if (aChildNode == ParentTree1) {
                    tempStr.append(TreeStr1);
                    bBetween = 1;
                    continue;
                }

                if (aChildNode == ParentTree2) {
                    tempStr.append(TreeStr2);
                    bBetween = 0;
                    continue;
                }

                if (bBetween == 1){
                    String ChildTreeStr = Tree2String(CommonNode, aChildNode , true);
                    tempStr.append(ChildTreeStr);
                    continue;
                }
                
                tempStr.append("(");
                tempStr.append(aChildNode.value());
                tempStr.append(" ");
                tempStr.append(aChildNode.value());
                tempStr.append(")");
            }
            tempStr.append(")");
        }

        ReturnStr = tempStr.toString();
        
        ReturnStr = PostProcessTreeString(ReturnStr);
        return ReturnStr;


    }

 protected String FindTree_2(Tree ParentTree, Tree CurTree, int Mark, boolean bAna){
        StringBuffer Buf = new StringBuffer();
        StringBuffer Buf1 = new StringBuffer();
        
	Tree pTree = CurTree;
	Tree UpTree = CurTree.parent(ParentTree);

	if (Mark == 0)
            Buf1.append("(").append(CurTree.value()).append(" ").append(CurTree.value()).append(")");
	if (Mark == 1)
            Buf1.append("(CANDI-").append(CurTree.value()).append(" CANDI-").append(CurTree.value()).append(")");
	if (Mark == 2)
            Buf1.append("(ANA-").append(CurTree.value()).append(" ANA-").append(CurTree.value()).append(")");

        if (ParentTree == CurTree){
            return Buf1.toString();
        }


	do{

                Buf.delete(0,Buf.length());
                Buf.append("(").append(UpTree.value()).append(" ");
                boolean bShow = bAna;
                
                for (int I = 0; I < UpTree.children().length; I++){
                    Tree aChildNode = UpTree.children()[I];
                    
                    if (aChildNode == pTree){
                        Buf.append(Buf1);
                        bShow = !bShow;
                    }else{
                        if (bShow)
                            Buf.append(Tree2String(UpTree, aChildNode , true));
                        else
                            Buf.append("(").append(aChildNode.value()).append(" ").append(aChildNode.value()).append(")");
                    }
                }

                Buf.append(")");
                Buf1.delete(0,Buf1.length());
                Buf1.append(Buf);
		
		if (UpTree == ParentTree)
                    break;
		pTree = UpTree;
		UpTree = UpTree.parent(ParentTree);
	} while(UpTree != null);
	
        return Buf1.toString();

    }
  */  
    
    
  /***
     *postprocess the string extracted. All the NP type (NN, NNS, NNP) will be normalized to type "NP"
    *///

protected String PostProcessTreeString(String SearchStr){
	
        return SearchStr;
        /*
        String str;
        String ReturnStr = SearchStr;
        
        Pattern pattern;
	Matcher matcher;
	
        
	str = "\\(NN |\\(NNP |\\(NNS |\\(NNPS ";
	pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
	matcher = pattern.matcher(ReturnStr);
        ReturnStr = matcher.replaceAll("(np ");
        
	str = " NN\\)| NNP\\)| NNS\\)| NNPS\\)";
	pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
	matcher = pattern.matcher(ReturnStr);
        ReturnStr = matcher.replaceAll(" np)");
        
        return ReturnStr;
         */
  }

    /***
     *Find the trees that cover an input mention node 
     *///
protected  String FindMentionStruct(Tree aTreeNode, Tree RootNode, boolean bShowLeafWord){
        
        if (aTreeNode == null)
            return "";
        
	Tree ParentTree = aTreeNode;
        Tree CurTree = aTreeNode;
        
        while (CurTree != null){
            if (CurTree.value().compareToIgnoreCase("NP") != 0 &&
                CurTree.value().compareToIgnoreCase("PRP") != 0 &&
                CurTree.value().compareToIgnoreCase("PRP$") != 0 &&
                CurTree.value().compareToIgnoreCase("NN") != 0 &&
                CurTree.value().compareToIgnoreCase("NNS") != 0 &&
                CurTree.value().compareToIgnoreCase("NNP") != 0 &&
                CurTree.value().compareToIgnoreCase("NNPS") != 0 
               )
                break;
            ParentTree = CurTree;
            CurTree = CurTree.parent(RootNode);
        }
        
        
 	String TreeStr = Tree2String(aTreeNode, aTreeNode, bShowLeafWord);
        String ReturnStr = FindTree_1(ParentTree, aTreeNode, 1, TreeStr);
        return ReturnStr;
}



protected  String FindMentionStruct_1(Tree aTreeNode, Tree RootNode, boolean bShowLeafWord){

        if (aTreeNode == null)
            return "";

	Tree ParentTree = aTreeNode;
        Tree CurTree = aTreeNode;
        
        while (CurTree != null){
            if (CurTree.value().compareToIgnoreCase("NP") != 0 &&
                CurTree.value().compareToIgnoreCase("PRP") != 0 &&
                CurTree.value().compareToIgnoreCase("PRP$") != 0 &&
                CurTree.value().compareToIgnoreCase("NN") != 0 &&
                CurTree.value().compareToIgnoreCase("NNS") != 0 &&
                CurTree.value().compareToIgnoreCase("NNP") != 0 &&
                CurTree.value().compareToIgnoreCase("NNPS") != 0 
               )
                break;
            ParentTree = CurTree;
            CurTree = CurTree.parent(RootNode);
        }
 	return Tree2String(aTreeNode, ParentTree, bShowLeafWord);
}


protected String Tree2String(Tree MentionNode, Tree RootNode, boolean bShowLeafWord){
    
        String ReturnStr = "";
        StringBuffer Buf1 = new StringBuffer();

        int ChildNum = RootNode.children().length;
        if (ChildNum == 0){
            if (RootNode == MentionNode)
                Buf1.append("MENTION-");
            
            if (RootNode.value().equalsIgnoreCase("("))
                Buf1.append("LRB");
            else if (RootNode.value().equalsIgnoreCase(")"))
                Buf1.append("RRB");
            else
                Buf1.append(RootNode.value());
            
            return Buf1.toString();
        }
            
        if (RootNode == MentionNode)
            Buf1.append("(MENTION-").append(RootNode.value()).append(" ");
        else
            Buf1.append("(").append(RootNode.value()).append(" ");
        
        if ( bShowLeafWord == false && RootNode.value().equalsIgnoreCase("DT") == false && ChildNum == 1 && RootNode.getChild(0).children().length == 0 ){
            if (RootNode.value().equalsIgnoreCase("("))
                Buf1.append("LRB");
            else if (RootNode.value().equalsIgnoreCase(")"))
                Buf1.append("RRB");
            else
                Buf1.append(RootNode.value());
        }
        else
            for (int I = 0; I < ChildNum; I++){
                Buf1.append(Tree2String(MentionNode, RootNode.getChild(I), bShowLeafWord));
            }
        Buf1.append(")");
        
        ReturnStr = Buf1.toString();
        return ReturnStr;
}



 /** Return a fake tree whose children are bag of words surrounding a mention */

protected String FindSurrondingBagWords(Mention aMention, int WindowLength, int ShowFlag)
{   
        StringBuffer textBuffer = new StringBuffer();

        int MentionStartWord = aMention.getStartWord();
        int MentionEndWord = aMention.getEndWord();

        Tree SentenceTree = aMention.getSentenceTree();
        List<Tree> Leaves = SentenceTree.getLeaves();
        
        int StartPos = MentionStartWord - WindowLength;
        //if (StartPos < 0) StartPos = 0;
        
        int EndPos = MentionEndWord + WindowLength;
        //if (EndPos >= Leaves.size())
          //  EndPos = Leaves.size() - 1;
        
        textBuffer.append("(|SEQ| ");
        for (int token = StartPos; token <= EndPos; token++)
        {
            if (token < 0 || token >= Leaves.size()){
                textBuffer.append("(").
                    append("nil").
                    append(" ").
                    append("nil").
                    append(")");
                
            }else{
                Tree WordNode = Leaves.get(token);
                Tree TypeNode = WordNode.parent(SentenceTree);
                String WordValue = WordNode.value();
                if (WordValue.equalsIgnoreCase("("))
                    WordValue = "LRB";
                if (WordValue.equalsIgnoreCase(")"))
                    WordValue = "RRB";
                

                
                if (ShowFlag == 0){
                    textBuffer.append("(").
                        append(TypeNode.value()).
                        append(" ").
                        append(WordValue).
                        append(")");
                }
                
               if (ShowFlag == 1){
                    textBuffer.append("(").
                        append(TypeNode.value()).
                        append(" ").
                        append(TypeNode.value()).
                        append(")");
                }

               if (ShowFlag == 2){
                    textBuffer.append("(").
                        append(WordValue).
                        //append(" ").
                        //append(WordNode.value()).
                        append(")");
                }

            }
        }
        textBuffer.append(")");
        
        return textBuffer.toString();
        
    }

protected String FindSurrondingBagWords_Mark(Mention aMention, int WindowLength, int ShowFlag)
{   
        StringBuffer textBuffer = new StringBuffer();

        int MentionStartWord = aMention.getStartWord();
        int MentionEndWord = aMention.getEndWord();

        Tree SentenceTree = aMention.getSentenceTree();
        List<Tree> Leaves = SentenceTree.getLeaves();
        
        int StartPos = MentionStartWord - WindowLength;
        if (StartPos < 0) StartPos = 0;
        
        int EndPos = MentionEndWord + WindowLength;
        if (EndPos >= Leaves.size())
            EndPos = Leaves.size() - 1;
        
        textBuffer.append("(|SEQ| ");
        
        if (StartPos == 0)
            textBuffer.append("(BS)");
        
        
        for (int token = StartPos; token <= EndPos; token++)
        {
            if (token == MentionStartWord)    
                textBuffer.append("(BM)");
            Tree WordNode = Leaves.get(token);
            Tree TypeNode = WordNode.parent(SentenceTree);
            String WordValue = WordNode.value();

            if (WordValue.equalsIgnoreCase("("))
                WordValue = "LRB";
            if (WordValue.equalsIgnoreCase(")"))
                WordValue = "RRB";



            if (ShowFlag == 0){
                textBuffer.append("(").
                    append(TypeNode.value()).
                    append(" ").
                    append(WordValue).
                    append(")");
            }

           if (ShowFlag == 1){
                textBuffer.append("(").
                    append(TypeNode.value()).
                    //append(" ").
                    //append(TypeNode.value()).
                    append(")");
            }

           if (ShowFlag == 2){
                textBuffer.append("(").
                    append(WordValue).
                    //append(" ").
                    //append(WordNode.value()).
                    append(")");
            }
            
           if (token == MentionEndWord)    
                textBuffer.append("(EM)");

        }

       if (EndPos == Leaves.size()-1)
            textBuffer.append("(ES)");
        
        textBuffer.append(")");
        
        return textBuffer.toString();
        
    }

protected String MakeStringTree(Mention aMention)
{   
        String aStr = aMention.getMarkableString();
        StringBuffer textBuffer = new StringBuffer();
        
        textBuffer.append("(|SEQ| ");
        for (int i = 0; i < aStr.length(); i++){
            char aChr = aStr.charAt(i);
            textBuffer.append("(");
            if (aChr == '(')
                textBuffer.append("LRB");
            else
            if (aChr == ')')
                textBuffer.append("RRB");
            else
                textBuffer.append(aChr);
            textBuffer.append(")");
        }
        textBuffer.append(")");
        
        return textBuffer.toString();
        
    }


protected  Tree FindUpperNPNode(Tree aTreeNode, Tree RootNode){
        
       if (aTreeNode.value().charAt(0) != 'N' &&
           aTreeNode.value().charAt(0) != 'n' &&
           !aTreeNode.value().equalsIgnoreCase("prp") && //"it"
           !(aTreeNode.value().equalsIgnoreCase("dt") && aTreeNode.children().length == 1 &&
               (aTreeNode.children()[0].value().equalsIgnoreCase("this") ||
                aTreeNode.children()[0].value().equalsIgnoreCase("that") ||
                aTreeNode.children()[0].value().equalsIgnoreCase("these") ||
                aTreeNode.children()[0].value().equalsIgnoreCase("those"))
                )
           )
           return aTreeNode;

	Tree ParentTree = aTreeNode;
        Tree CurTree = aTreeNode.parent(RootNode);
        
        while (CurTree != null){
            if (CurTree.value().charAt(0) != 'N' &&
                CurTree.value().charAt(0) != 'n')
                break;
            
            //check whether Current Node is a embeded NP, or in a conjunction
            int bRight = 0;
            int bEmbeded = 0;
            int bConjuncted = 0;
            int bHasOtherNP = 0;
            for (int i = 0; i < CurTree.children().length; i++){
                Tree ChildNode = CurTree.children()[i];
                if (ParentTree == ChildNode){
                    bRight = 1;
                    continue;
                }
                if (bRight == 1 && 
                     (ChildNode.value().charAt(0) == 'N' || ChildNode.value().charAt(0) == 'n')
                     ){
                    bEmbeded = 1;
                    break;
                }
                if (ChildNode.value().charAt(0) == 'N' || ChildNode.value().charAt(0) == 'n'){
                    bHasOtherNP = 1;
                    continue;
                }
                if (bHasOtherNP == 1){
                    if (ChildNode.value().equalsIgnoreCase(",") || ChildNode.value().equalsIgnoreCase("cc")){
                        bConjuncted = 1;
                        break;
                    }
                }
            }
            
            if (bEmbeded == 1 || bConjuncted == 1)
                break;
            
            ParentTree = CurTree;
            CurTree = CurTree.parent(RootNode);
        }
        
        return ParentTree;
}

protected String FlatExtractedFeatures(Tree inAnteTree, Tree AnteTreeRoot, Tree inAnaTree, Tree AnaTreeRoot){
        Tree AnteTree = FindUpperNPNode(inAnteTree, AnteTreeRoot);
        Tree AnaTree = FindUpperNPNode(inAnaTree, AnaTreeRoot);

        int[] NumList = fnCalNodeNum(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot);
        int IsCandiSubj = fnIsCandiSubj(AnteTree, AnteTreeRoot);
        int IsCandiObj =  fnIsCandiObj(AnteTree, AnteTreeRoot);
        int IsCCommanding = fnIsCCommanding(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot);
        
        StringBuffer temp = new StringBuffer();
        temp.append(NumList[0]).append(",").append(NumList[1]).append(",").append(NumList[2]).append(",");
        temp.append(IsCandiSubj).append(",").append(IsCandiObj).append(",").append(IsCCommanding);
        return temp.toString();
}

protected int[] fnCalNodeNum(Tree AnteTree, Tree AnteTreeRoot, Tree AnaTree, Tree AnaTreeRoot){
        int[] RetVal = {0, 0, 0};
        Tree CommonNode = AnteTree;
        
        while (CommonNode != null &&
               !CommonNode.dominates(AnaTree))
            CommonNode = CommonNode.parent(AnteTreeRoot);

        if (CommonNode == null)
            return RetVal;
        
        Tree CurTree;
        CurTree = AnteTree;
        int NumSNode = 0;
        int NumNPNode = 0;
        int NumVPNode = 0;
        
        while (CurTree != CommonNode){
            if (CurTree.value().charAt(0) == 'S' || CurTree.value().charAt(0) == 's')
                NumSNode++;
            if (CurTree.value().compareToIgnoreCase("NP") == 0)
                NumNPNode++;
            if (CurTree.value().compareToIgnoreCase("VP") == 0)
                NumVPNode++;
            CurTree = CurTree.parent(CommonNode);
        }
       
       CurTree = AnaTree; 
       while (CurTree != CommonNode){
            if (CurTree.value().charAt(0) == 'S' || CurTree.value().charAt(0) == 's')
                NumSNode++;
            if (CurTree.value().compareToIgnoreCase("NP") == 0)
                NumNPNode++;
            if (CurTree.value().compareToIgnoreCase("VP") == 0)
                NumVPNode++;
            CurTree = CurTree.parent(CommonNode);
        }
       
       int[] RetVal1 = {NumSNode, NumNPNode, NumVPNode};
       return RetVal1;
    
}

protected int fnIsCandiSubj(Tree AnteTree, Tree AnteTreeRoot){
    Tree CurNode = AnteTree;
    Tree ParentNode = CurNode.parent(AnteTreeRoot);
    int bFound = 0;
    
    if (ParentNode != null && 
         (ParentNode.value().charAt(0) == 'S' || ParentNode.value().charAt(0) == 's')){
         
        for (int i = 0; i < ParentNode.children().length; i++){
            Tree ChildNode = ParentNode.children()[i];
            
            if (ChildNode == CurNode){
                bFound = 1;
                continue;
            }
            
            if (bFound == 1){
                if (ChildNode.value().equalsIgnoreCase("VP")){
                    if (ParentNode.value().equalsIgnoreCase("SBAR"))
                        return 2;
                    if (ParentNode.value().equalsIgnoreCase("S") || ParentNode.value().equalsIgnoreCase("S"))
                        return 1;
                }
                return 0;
            }
        }
    }
    return 0;
         
}

protected int fnIsCandiObj(Tree AnteTree, Tree AnteTreeRoot){
    Tree CurNode = AnteTree;
    Tree ParentNode = CurNode.parent(AnteTreeRoot);
    
    if (ParentNode != null) {
         if (ParentNode.value().equalsIgnoreCase("VP"))
             return 1;
         if (ParentNode.value().equalsIgnoreCase("PP") )
             return 2;
    }
    
    return 0;

}

protected int fnIsCCommanding(Tree AnteTree, Tree AnteTreeRoot, Tree AnaTree, Tree AnaTreeRoot){
    Tree CurNode = AnteTree;
    Tree ParentNode = CurNode.parent(AnteTreeRoot);
    int bFound = 0;

    if (AnteTreeRoot != AnaTreeRoot)
        return 0;
    
    if (ParentNode != null && ParentNode.dominates(AnaTree))
        return 1;
    
    return 0;
}
         

       /***
     *Return the extracted tree structed feature for a given antecedent & anaphor pair.
     */
    public String[] GetSyntaxFeatures(Mention Candi, Mention Ana){
        
        Tree AnteTree;
        Tree AnaTree;

        //if (!Ana.getPronoun()){
        //    String[] retval = {"","",""};
        //    return retval;
        //}
        
        AnaTree =  Ana.getLowestProjection();
        AnteTree = Candi.getLowestProjection();
        
        Tree AnteTreeRoot = Candi.getSentenceTree();
        Tree AnaTreeRoot = Ana.getSentenceTree();


        //if (mIndex == 763)
        //    System.out.println(mIndex);
        //System.out.println(mIndex++);
        /*
        System.out.println(AnteTreeRoot.toString());
        System.out.println();
        System.out.println(AnaTreeRoot.toString());
        System.out.println();
        System.out.println(FindConnectingStruct(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot));
        System.out.println();
        System.out.println(FindMentionStruct(AnteTree, AnteTreeRoot, false));
        System.out.println();
        System.out.println(FindMentionStruct(AnaTree, AnaTreeRoot, false));
        System.out.println();
        System.out.println(FindTree_1(AnteTreeRoot, AnteTree, 1));
        System.out.println();
        System.out.println(FindTree_1(AnaTreeRoot, AnaTree, 2));
        System.out.println();
        System.out.println("****************************************");
*/
        String ConnectingStruct = "";
		String MentionStruct_Ante = "";
		String MentionStruct_Ana = "";

        if (Ana.getPronoun() || Ana.getSentId() == Candi.getSentId()){
		//if (Ana.getPronoun()){
		  //ConnectingStruct = FindConnectingStruct_ShowFull(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot);
                 //String temp =                  FindSurrondingBagWords_Mark(Candi, 2, 1);
		 ConnectingStruct = FindConnectingStruct(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot);
		//  MentionStruct_Ante = FindMentionStruct(AnteTree, AnteTreeRoot, false);
		//  MentionStruct_Ana = FindMentionStruct(AnaTree, AnaTreeRoot, false);
                   
		}


        
        String[] retval=
               { ConnectingStruct,
                 FindMentionStruct(AnteTree, AnteTreeRoot, true),
                 FindMentionStruct(AnaTree, AnaTreeRoot, true),
                 FindSurrondingBagWords(Candi, 2, 2),
                 FindSurrondingBagWords(Ana, 2, 2),
                 FindSurrondingBagWords_Mark(Candi, 2, 2),
                 FindSurrondingBagWords_Mark(Ana, 2, 2),
                 FindSurrondingBagWords_Mark(Candi, 2, 1),
                 FindSurrondingBagWords_Mark(Ana, 2, 1),
                 "", "",
                 //FindTree_1(AnteTreeRoot, AnteTree, 1),
                 //FindTree_1(AnaTreeRoot, AnaTree, 2),
                 //FindSurrondingBagWords(Candi, 2, 0),
                 //FindSurrondingBagWords(Ana, 2, 0),
                 //FindSurrondingBagWords(Candi, 2, 1),
                 //FindSurrondingBagWords(Ana, 2, 1),
                 //FindSurrondingBagWords(Candi, 2, 2),
                 //FindSurrondingBagWords(Ana, 2, 2),
                 //MakeStringTree(Candi),
                 //MakeStringTree(Ana),
                  //"","",
                 //"","","","","","","","",
                         
                 //FlatExtractedFeatures(AnteTree, AnteTreeRoot, AnaTree, AnaTreeRoot),
                 

        };
        return retval;
    }
    
}
