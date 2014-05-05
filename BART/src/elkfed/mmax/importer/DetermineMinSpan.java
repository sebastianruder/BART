/*
 *  Copyright 2008 Yannick Versley / Univ. Tuebingen
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

import edu.stanford.nlp.trees.ModCollinsHeadFinder;
import edu.stanford.nlp.trees.Tree;
import elkfed.mmax.DiscourseUtils;
import elkfed.mmax.MMAX2FilenameFilter;
import elkfed.mmax.minidisc.IMarkable;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.pipeline.PipelineComponent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static elkfed.mmax.MarkableLevels.DEFAULT_PARSE_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_COREF_LEVEL;

/**
 *
 * @author yannick
 */
public class DetermineMinSpan {
    
    public static int find_same(List<Tree> ts, Tree t) {
        for (int i=0; i<ts.size(); i++) {
            if (ts.get(i)==t) {
                return i;
            }
        }
        throw new UnsupportedOperationException("Not found: "+t+" in "+ts);
    }

    /** adds min_ids and min_span attributes so that
     *  BART's chunk-based coref resolution works
     */
    public static void addMinSpan(int start, Tree tree, IMarkable tag,
            List<String> tokens)
    {
        List<Tree> leaves = tree.getLeaves();
        Tree startNode;
        Tree endNode;
        try {
            startNode = leaves.get(tag.getLeftmostDiscoursePosition() - start);
            endNode = leaves.get(tag.getRightmostDiscoursePosition() - start);
            if (".".equals(endNode.parent(tree).value())) {
                //System.err.println("Sentence-final dot in "+
                //        tokens.subList(tag.start, tag.end + 1)+ "removed.");
                endNode = leaves.get(tag.getRightmostDiscoursePosition()-start-1);
            }
        } catch (IndexOutOfBoundsException ex) {
            System.out.format("indices not found: %d,%d in %s [wanted: %s] [ctx: %s]",
                    tag.getLeftmostDiscoursePosition() - start, 
                    tag.getRightmostDiscoursePosition() - start,
                    leaves, tokens.subList(tag.getLeftmostDiscoursePosition(),
                    tag.getRightmostDiscoursePosition() + 1),
                    tokens.subList(start, tag.getLeftmostDiscoursePosition()));
            throw ex;
        }

        Tree parentNode = startNode;
        while (parentNode != null && !parentNode.dominates(endNode)) {
            parentNode = parentNode.parent(tree);
        }

        if (parentNode == null) {
            System.err.println("Could not match tree (1)");
            return;
        }

        if (startNode.leftCharEdge(tree) != parentNode.leftCharEdge(tree) ||
                endNode.rightCharEdge(tree) != parentNode.rightCharEdge(tree)) {
            System.err.println("Could not match tree (2)");
            return;
        }

        Tree oldParent = parentNode;
        ModCollinsHeadFinder hf = new ModCollinsHeadFinder();
        // use the head finder to narrow down the span.
        // stop if (a) the head is no longer an NP or
        // (b) the NP is a conjunction
        go_up:
        while (true) {
            for (Tree t : parentNode.getChildrenAsList()) {
                if (t.value().equals("CC")) {
                    break go_up;
                }
            }
            Tree headDtr = hf.determineHead(parentNode);
            if (headDtr==null || !headDtr.value().equals("NP")) {
                break;
            }
            parentNode = headDtr;
        }
        if (parentNode != oldParent) {
            List<Tree> newLeaves = parentNode.getLeaves();
            int newStart = start + find_same(leaves,newLeaves.get(0));
            int newEnd = newStart + newLeaves.size() - 1;
            if (newStart<=tag.getLeftmostDiscoursePosition()) {
                if (tag.getLeftmostDiscoursePosition()-newStart > 1) {
                    System.err.println("NP node is too big:"+parentNode.toString()+
                            " wanted:"+tokens.subList(tag.getLeftmostDiscoursePosition(),
                            tag.getRightmostDiscoursePosition() + 1)+
                            " in: "+tree);
                    return;
                }
                for (int i=newStart-start ; i<tag.getLeftmostDiscoursePosition()-start; i++) {
                    System.err.println("additional prefix in syntax:"+leaves.get(i));
                }
                // switch NP boundary and tag boundary
                // (even [Connie Cheung]) => min_words="Connie Cheung"
                int tmp=tag.getLeftmostDiscoursePosition();
                tag.adjustSpan(newStart, tag.getRightmostDiscoursePosition());
                newStart=tmp;
            }
            assert newEnd<=tag.getRightmostDiscoursePosition();
            // this relies on MiniDiscourse's default word numbering
            // which is ugly but should generally work...
            if (newStart == newEnd) {
                tag.setAttributeValue("min_ids", "word_" + (newStart + 1));
            } else {
                tag.setAttributeValue("min_ids",
                        String.format("word_%d..word_%d", newStart + 1, newEnd + 1));
            }
            StringBuffer buf = new StringBuffer();
            for (Tree t : newLeaves) {
                buf.append(t.toString().toLowerCase());
                buf.append(' ');
            }
            buf.setLength(buf.length() - 1);
            tag.setAttributeValue("min_words", buf.toString());
        }
    }
    
    /** adds min_ids and min_words attribute to all markables on the
     * coref level that do not currently have it
     * uses the parse trees if available.
     * @param doc the Minidiscourse document
     */
    public static void addMinSpanAttrs(MiniDiscourse doc) throws IOException {
        List<Markable> parses=DiscourseUtils.getMarkables(doc,DEFAULT_PARSE_LEVEL);
        List<Markable> coref_tags=DiscourseUtils.getMarkables(doc,DEFAULT_COREF_LEVEL);
        List<String> tokens=Arrays.asList(doc.getTokens());
        int parses_idx=0;
        for (Markable mk: coref_tags) {
            while (parses_idx<parses.size() &&
                    parses.get(parses_idx).getRightmostDiscoursePosition() <
                        mk.getRightmostDiscoursePosition()) {
                parses_idx++;
            }
            Markable parseMarkable=parses.get(parses_idx);
            Tree parse=Tree.valueOf(parseMarkable
                    .getAttributeValue(PipelineComponent.TAG_ATTRIBUTE));
            addMinSpan(parseMarkable.getLeftmostDiscoursePosition(),
                    parse, mk, tokens);
        }
    }
    
    public static void main(String[] args) {
        File dataDir=new File(args[0]);
        for (File mmaxFile : dataDir.listFiles(MMAX2FilenameFilter.FILTER_INSTANCE)) {
            try {
                String fname = mmaxFile.getName();
                if (fname.endsWith(".mmax")) {
                    fname = fname.substring(0, fname.length() - 5);
                }
                MiniDiscourse doc = MiniDiscourse.load(mmaxFile.getParentFile(), fname);
                addMinSpanAttrs(doc);
                doc.saveAllLevels();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("IOException occurred:"+ex);
            }
        }
    }
}
