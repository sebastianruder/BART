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
package elkfed.mmax.pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;
import elkfed.config.ConfigProperties;
import elkfed.mmax.DiscourseUtils;
import gnu.trove.list.array.TIntArrayList;

/**
 * Uses the Berkeley parser
 * (modified from StanfordParser)
 * 
 * @author yannick
 */
public class BerkeleyParser extends Parser {

    private CoarseToFineMaxRuleParser parser;
    private static Pattern quote_re = Pattern.compile("[\"']|``|''");

    /** Creates a new instance of StanfordParser */
    public BerkeleyParser() {
        super();
        ParserData pData = ParserData.Load(
                new File(ConfigProperties.getInstance().getRoot(),
                "models/parser/eng_sm5.gr").getAbsolutePath());
        Grammar grammar = pData.getGrammar();
        Lexicon lexicon = pData.getLexicon();
        Numberer.setNumberers(pData.getNumbs());
        parser = new CoarseToFineMaxRuleParser(grammar, lexicon,
                1.0, -1, false, false, false, true, false, false, false);
    //parser.binarization = pData.getBinarization();
    }

    /** Adds a parse tree to forest for each sentence in the document */
    protected void annotateDocument() {
        String[][] sentences = null;
        sentences = DiscourseUtils.getSentenceTokens(currentDocument);

        for (int sentence = 0; sentence < sentences.length; sentence++) {
            List<String> tempSentL = new ArrayList<String>();
            TIntArrayList quoteIndices = new TIntArrayList();
            List<String> quoteTokens = new ArrayList<String>();
            int i = 0;
            for (String tok : sentences[sentence]) {
                String tok2 = tok.replaceAll("\\(", "-LRB-").replaceAll("\\)", "-RRB-");
                if (quote_re.matcher(tok2).matches()) {
                    quoteIndices.add(i);
                    quoteTokens.add(tok2);
                } else {
                    tempSentL.add(tok2);
                    i++;
                }
            }
            System.out.println("parse:"+tempSentL);
            Tree<String> parsedTree=null;
            if (tempSentL.size()>0) {
                String[] tempSent = tempSentL.toArray(new String[tempSentL.size()]);
                parsedTree = parser.getBestConstrainedParse(Arrays.asList(tempSent), null);
            }
            if (parsedTree != null && parsedTree.getChildren().size() > 0) {
                parsedTree = TreeAnnotations.unAnnotateTree(parsedTree, false);
                //TBD: put quotes back in
                parsedTree = putBackTokens(parsedTree,
                        quoteTokens, quoteIndices);
                System.out.println("parsed:" + parsedTree);
                forest.add(parsedTree.toString());
            } else {
                // do something else...
                StringBuffer buf = new StringBuffer("(FRAG");
                for (String tok : sentences[sentence]) {
                    String tok2 = tok.replaceAll("\\(", "-LRB-")
                            .replaceAll("\\)", "-RRB-");
                    buf.append(" (X ").append(tok2).append(")");
                }
                buf.append(")");
                System.out.println("not parsed:" + buf.toString());
                forest.add(buf.toString());
            }
        }
    }

    private static class PutbackState {

        int posTree = 0;
        int posList = 0;
    }

    private Tree<String> putBackTokens(Tree<String> parsedTree,
            List<String> quoteTokens, TIntArrayList quoteIndices) {
        PutbackState state = new PutbackState();
        Tree<String> resultTree = putBackTokens(parsedTree, state, quoteTokens, quoteIndices);
        while (state.posList < quoteIndices.size() &&
                state.posTree == quoteIndices.get(state.posList)) {
            resultTree.getChildren().add(
                    makeQuoteToken(quoteTokens.get(state.posList)));
            state.posList++;
        }
        return resultTree;
    }

    private Tree<String> makeQuoteToken(String quote) {
        List<Tree<String>> resultChildren = new ArrayList<Tree<String>>();
        resultChildren.add(new Tree<String>(quote));
        Tree<String> resultTree = new Tree<String>("\"",resultChildren);
        return resultTree;
    }

    private Tree<String> putBackTokens(Tree<String> parsedTree,
            PutbackState state,
            List<String> quoteTokens, TIntArrayList quoteIndices) {
        if (parsedTree.isPreTerminal()) {
            state.posTree += 1;
            return parsedTree;
        } else {
            Tree<String> resultTree = new Tree<String>(parsedTree.getLabel());
            List<Tree<String>> resultChildren = new ArrayList<Tree<String>>();
            for (Tree<String> chld : parsedTree.getChildren()) {
                while (state.posList < quoteIndices.size() &&
                        state.posTree == quoteIndices.get(state.posList)) {
                    resultChildren.add(
                            makeQuoteToken(quoteTokens.get(state.posList)));
                    state.posList++;
                }
                resultChildren.add(putBackTokens(chld, state,
                        quoteTokens, quoteIndices));
            }
            resultTree.setChildren(resultChildren);
            return resultTree;
        }
    }
}
