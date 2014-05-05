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

package elkfed.coref.features.pairs;

import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.coref.features.pairs.FE_AppositiveParse;
import edu.stanford.nlp.trees.Tree;
import elkfed.mmax.minidisc.MarkableHelper;
import elkfed.mmax.minidisc.MiniDiscourse;
import elkfed.mmax.minidisc.Markable;

/**
 * One MaxNP is embedded in another
 * NB: appositives excluded
 * @author olga
 */
public class FE_Span implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_IS_SPAN=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"Span");
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_SPAN);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_IS_SPAN, getSpanEmbed(inst));

    }
    public static Boolean getSpanEmbed(PairInstance inst) {

// should be in the same sentence
        if (inst.getAnaphor().getSentId()!=inst.getAntecedent().getSentId())
           return false;

// should not be in apposition
        if (FE_AppositiveParse.getAppositivePrs(inst)) return false;


// should not be adjacent (should take from appo_icab, but doesn't work for some reason)


    Markable m1=inst.getAntecedent().getMarkable();
    Markable m2=inst.getAnaphor().getMarkable();


    int sana=m2.getLeftmostDiscoursePosition();
    int sante=m1.getLeftmostDiscoursePosition();
    int eana=m2.getRightmostDiscoursePosition();
    int eante=m1.getRightmostDiscoursePosition();

    MiniDiscourse doc = m1.getMarkableLevel().getDocument();


        if (m1.getAttributeValue("min_ids") != null) {
           String[] spans = MarkableHelper.parseRanges(m1.getAttributeValue("min_ids"));
           sante = doc.DiscoursePositionFromDiscourseElementID(spans[0]);
           eante = doc.DiscoursePositionFromDiscourseElementID(spans[spans.length - 1]);
        }
        if (m2.getAttributeValue("min_ids") != null) {
           String[] spans = MarkableHelper.parseRanges(m2.getAttributeValue("min_ids"));
           sana = doc.DiscoursePositionFromDiscourseElementID(spans[0]);
           eana = doc.DiscoursePositionFromDiscourseElementID(spans[spans.length - 1]);
        }
          

        if (eana==sante-1) return false;
        if (eante==sana-1) return false;


// check for trivial embedding, if maxnps are missing
    if (inst.getAntecedent().getMaxNPParseTree()== null &&
        sana>=sante && eana<=eante) return true;
    if (inst.getAnaphor().getMaxNPParseTree()== null &&
        sana<=sante && eana>=eante) return true;

// check for maximal np embedding

    sana-=inst.getAnaphor().getSentenceStart();
    sante-=inst.getAntecedent().getSentenceStart();
    eana-=inst.getAnaphor().getSentenceStart();
    eante-=inst.getAntecedent().getSentenceStart();



// if anaphor does have MaxNPParseTree -- check that it does not span over the antecedent
// if it does -- check whether there is an s-node in between

        if (inst.getAnaphor().getMaxNPParseTree()!= null) {
        Tree sentTree=inst.getAntecedent().getSentenceTree();
        List<Tree> Leaves = sentTree.getLeaves();
        Tree startNode = Leaves.get(sante);
         if (eante<Leaves.size()) {
// check <leaves.size for markables spanning over sentence boundaries
           Tree endNode = Leaves.get(eante);
           if (inst.getAnaphor().getMaxNPParseTree().dominates(endNode) &&
             inst.getAnaphor().getMaxNPParseTree().dominates(startNode)) {
             Boolean sfound=false;
             Tree t=startNode;
             while(t!=null && t!=inst.getAnaphor().getMaxNPParseTree() && !sfound ) {
               if (t.value().toLowerCase().startsWith("s")) sfound=true;
               t=t.parent(sentTree);
             }
             if (!sfound) return true;
           }
         }

        }

// same for antecedent
        if (inst.getAntecedent().getMaxNPParseTree()!= null) {
        Tree sentTree=inst.getAntecedent().getSentenceTree();
        List<Tree> Leaves = sentTree.getLeaves();
        Tree startNode = Leaves.get(sana);
        if (eana<Leaves.size()) {
        Tree endNode = Leaves.get(eana);

        if (inst.getAntecedent().getMaxNPParseTree().dominates(endNode) &&
            inst.getAntecedent().getMaxNPParseTree().dominates(startNode)) {
             Boolean sfound=false;
             Tree t=startNode;
             while(t!=null && t!=inst.getAntecedent().getMaxNPParseTree() && !sfound) {
               if (t.value().toLowerCase().startsWith("s")) sfound=true;
               t=t.parent(sentTree);
             }
             if (!sfound) return true;

        }
       }
      }

      return false;

    }

    
}
