/*
 * Copyright 2008 Yannick Versley / Univ. Tuebingen
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
package elkfed.mmax.importer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import elkfed.ml.util.Alphabet;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.ArrayList;
import java.util.List;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.BobChrisTreeNormalizer;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.Tree;

import static elkfed.mmax.MarkableLevels.DEFAULT_POS_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;

/** a converter from OntoNote's horrible not-quite-XML format
 *  into MMAX files.
 *  Caution: (i) you need to replace all &s by &amp;s in the original
 *  OntoNotes data AND (ii) there is ONE occurrence of a 0 that is not
 *  a trace in the whole corpus.
 * @author versley
 */
public class ImportOntonotes extends Importer {

    static Pattern unwanted_token =
            Pattern.compile("^(?:0|\\*(?:ICH|PRO|T|RNR|U|EXP|NOT|PPA|\\?)\\*(?:-[0-9]+)?|\\*(?:-[0-9]+)?)$");

    ImportOntonotes(File coref_dir, String docId) {
        super(coref_dir, docId);
    }

    /** adds pos and chunk information */
    private void addParseInfo(int start, Tree tree) {
        /** Retrieve chunk tags from the parse tree and add chunk markables */
        boolean inNP = false;
        int startNP = -1;
        int wordLoc = 0;
        int depth = 0;
        for (String tok : tree.toString().replaceAll("\\)", ") ").split("\\s+")) {
            if (tok.matches("\\(NP")) {
                inNP = true;
                startNP = wordLoc;
                depth = 0;
            }
            if ((inNP) && (tok.matches(".*\\)"))) {
                depth--;
            }
            if ((inNP) && (tok.matches("\\(.*"))) {
                depth++;
            }
            if (tok.matches(".+\\)")) {
                wordLoc++;
            }
            if ((depth == 0) && (inNP)) {
                inNP = false;
                Tag t = new Tag();
                t.tag = DEFAULT_CHUNK_LEVEL;
                t.attrs.put("tag", "np");
                t.start = start + startNP;
                t.end = start + wordLoc - 1;
                tags.add(t);
            }
        }

        /** Retrieve POS tags from the parse tree */
        List<Label> taggedSent = new ArrayList<Label>(tree.preTerminalYield());
        for (int i = 0; i < taggedSent.size(); i++) {
            Tag t = new Tag();
            t.tag = DEFAULT_POS_LEVEL;
            t.start = t.end = start + i;
            String tag = taggedSent.get(i).value();
            t.attrs.put("tag", tag.toLowerCase());
            tags.add(t);
        }
    }

    public MiniDiscourse importFile(String fname) {
        try {
            boolean had_space = true;
            boolean need_bugfix =
             System.getProperty("elkfed.BuggyOntonotes","no")
             .matches("y|yes|true");
            List<Tag> names_stack = new ArrayList<Tag>();
            Alphabet<String> sets = new Alphabet<String>();
            sets.lookupIndex("*DUMMY*");
            int sent_id = 0;
            Tag sentence_tag = null;
            OntonotesReader reader =
                    new OntonotesReader(new File(fname + ".coref"));
            OntonotesReader readerNE =
                    new OntonotesReader(new File(fname + ".name"));
            TreeReader tr = new PennTreeReader(new FileReader(fname + ".parse"),
                    new LabeledScoredTreeFactory(),
                    new BobChrisTreeNormalizer());
            Tree tree = null;
            int eventType = reader.getNextEvent();
            boolean in_text = false;
            do {
                if (eventType == OntonotesReader.START_TAG &&
                        "COREF".equals(reader.getName())) {
                    Tag t;
                    if (need_bugfix) {
                        t = buggy_push_tag("coref",tag_stack);
                    } else {
                        t = push_tag("coref");
                    }
                    if ("IDENT".equals(reader.getAttribute("TYPE"))) {
                        t.attrs.put("coref_set", "set_" +
                                sets.lookupIndex(reader.getAttribute("ID")));
                    }
                    had_space = true;
                } else if (eventType == OntonotesReader.END_TAG &&
                        "COREF".equals(reader.getName())) {
                    Tag t = pop_tag("coref");
                    DetermineMinSpan.addMinSpan(sentence_tag.start, tree, t, tokens);
                    had_space = true;
                } else if (in_text && eventType == OntonotesReader.TOKEN) {
                    if (!reader.isTrace()) {
                        // process up to the next token in the names part
                        int names_event = readerNE.getNextEvent();
                        while (names_event != OntonotesReader.TOKEN) {
                            if (names_event == OntonotesReader.START_TAG &&
                                    "ENAMEX".equals(readerNE.getName())) {
                                Tag t = push_tag("enamex", names_stack);
                                t.attrs.put("tag", readerNE.getAttribute("TYPE"));
                            } else if (names_event == OntonotesReader.END_TAG &&
                                    "ENAMEX".equals(readerNE.getName())) {
                                Tag t = pop_tag("enamex", names_stack);
                            } else {
                                throw new IllegalStateException("Unexpected event:" + names_event);
                            }
                            names_event = readerNE.getNextEvent();
                        }
                        assert(reader.getToken().equals(readerNE.getToken()));
                        String tok=reader.getToken();
                        if (tok.equals("-LRB-")) tok="(";
                        if (tok.equals("-RRB-")) tok=")";
                        if (tok.equals("-LSB-")) tok="[";
                        if (tok.equals("-RSB-")) tok="]";
                        if (tok.equals("-LCB-")) tok="{";
                        if (tok.equals("-RCB-")) tok="}";
                        add_token(tok);
                    }
                } else if (in_text &&
                        eventType == OntonotesReader.NEWLINE) {
                    //System.out.println("sentence break");
                    if (sentence_tag != null) {
                        sentence_tag.end = tokens.size() - 1;
                        if (sentence_tag.end >= sentence_tag.start) {
                            tags.add(sentence_tag);
                            if (tree != null) {
                                Tag parse_tag = new Tag();
                                parse_tag.tag = "parse";
                                parse_tag.start = sentence_tag.start;
                                parse_tag.end = sentence_tag.end;
                                parse_tag.attrs.put("tag", tree.toString());
                                tags.add(parse_tag);
                                assert sentence_tag.end - sentence_tag.start + 1 == tree.yield().size() :
                                        String.format("%s / %s",
                                        tokens.subList(sentence_tag.start,
                                        sentence_tag.end + 1),
                                        tree.yield());
                                addParseInfo(sentence_tag.start, tree);
                            }
                        }
                    }
                    // process up to end of sentence in names annotation
                    int names_event = readerNE.getNextEvent();
                    while (names_event != OntonotesReader.NEWLINE) {
                        if (names_event == OntonotesReader.START_TAG &&
                                "ENAMEX".equals(readerNE.getName())) {
                            Tag t = push_tag("enamex", names_stack);
                            t.attrs.put("tag", readerNE.getAttribute("TYPE"));
                        } else if (names_event == OntonotesReader.END_TAG &&
                                "ENAMEX".equals(readerNE.getName())) {
                            Tag t = pop_tag("enamex", names_stack);
                        } else if (names_event == OntonotesReader.END_TAG &&
                                "DOC".equals(readerNE.getName())) {
                            // ignore
                        } else {
                            throw new IllegalStateException("Unexpected event:" +
                                    readerNE.describeEvent(names_event));
                        }
                        names_event = readerNE.getNextEvent();
                    }
                    // prepare new parse and sentence
                    sentence_tag = new Tag();
                    sentence_tag.start = tokens.size();
                    sentence_tag.tag = "sentence";
                    sentence_tag.attrs.put("orderid", "" + sent_id++);
                    tree = tr.readTree();
                } else if (eventType == OntonotesReader.END_TAG &&
                        "DOCNO".equals(reader.getName())) {
                    in_text = true;
                    // go to the end of the DOCNO part in name doc
                    int names_event = readerNE.getNextEvent();
                    while (names_event != OntonotesReader.END_TAG ||
                            !"DOCNO".equals(reader.getName())) {
                        names_event = readerNE.getNextEvent();
                    }
                } else if (eventType == OntonotesReader.START_TAG &&
                        "TURN".equals(reader.getName())) {
                    int names_event = readerNE.getNextEvent();
                    if (names_event != OntonotesReader.START_TAG ||
                            !"TURN".equals(readerNE.getName())) {
                        throw new UnsupportedOperationException("TURN in coref but not in names");
                    }
                    // parse level seems to be inconsistent... so don't check here :-|
                    System.err.println("TURN parse:"+tree.toString());
                    tree = tr.readTree();
                    eventType = reader.getNextEvent();
                    names_event=readerNE.getNextEvent();
                    if (eventType != OntonotesReader.NEWLINE ||
                            names_event != OntonotesReader.NEWLINE) {
                        throw new UnsupportedOperationException("No Newline after TURN");
                    }
                }
                eventType = reader.getNextEvent();
            } while (eventType != OntonotesReader.END_DOCUMENT);
            return create();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read file", ex);
        }

    }

    public static void main(String[] args) {
        String coref_dirS=System.getProperty("elkfed.ImportDir","/home/yannick.versley/OntoNotes-MMAX");
        File coref_dir = new File(coref_dirS);
        try {
            for (String arg : args) {
                if (arg.endsWith(".coref")) {
                    System.err.println(arg + " -> " +
                            arg.substring(arg.length() - 14, arg.length() - 6));
                    ImportOntonotes imp = new ImportOntonotes(coref_dir,
                            arg.substring(arg.length() - 14, arg.length() - 6));
                    imp.importFile(arg.substring(0, arg.length() - 6));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
