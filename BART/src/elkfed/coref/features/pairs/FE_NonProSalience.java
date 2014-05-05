/*
 * NonProSalience.java
 */

package elkfed.coref.features.pairs;

import elkfed.config.ConfigProperties;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;
import elkfed.knowledge.SemanticClass;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.mmax.minidisc.Markable;
import java.io.IOException;
import java.util.List;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author samuel
 *
 * Computes the non pronoun salience as a fraction. The numerator
 * is the number of previous head matching markables. The denumerator
 * is the number of the previous semantically compatible markables.
 * Semantically compatiable is every markable that has a equal semantic
 * class or the semantic class UNKNOWN
 */
public class FE_NonProSalience implements PairFeatureExtractor {

    MentionFactory mfact;
    List<Mention> mentions;
    MiniDiscourse doc;

    Map<Markable, Integer> mentionSaliences;
    Map<Markable, Integer> mentionIndex;
    Map<SemanticClass, Integer> mentionSemanticIndex;

    public FE_NonProSalience() {
        this.doc = null;
        this.mfact = ConfigProperties.getInstance().getMentionFactory();
        this.mentionSaliences = new HashMap<Markable, Integer>();
        this.mentionIndex = new HashMap<Markable, Integer>();
        this.mentionSemanticIndex = new HashMap<SemanticClass, Integer>();
    }
    
    public static final FeatureDescription<Double> FD_NON_PRO_SALIENCE=
            new FeatureDescription<Double>(FeatureType.FT_SCALAR, "Non_Pronoun_Salience");

    @Override
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_NON_PRO_SALIENCE);
    }

    @Override
    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_NON_PRO_SALIENCE,getLemmaSalience(inst.getAntecedent()));
    }

    /*
     *  if a new document is accessed for the first time, compute for every markable
     *  the number of previous headmatching markables (mentionSaliences)
     *  the number of previous semantically compatible markables (mentionIndex)
     */

    private void initMentions(Mention ante) {

        MiniDiscourse mentionDoc = ante.getMarkable().getMarkableLevel().getDocument();

        if(this.doc == null || mentionDoc!=this.doc) {

            this.mentionSaliences = new HashMap<Markable, Integer>();
            this.mentionIndex = new HashMap<Markable, Integer>();
            this.mentionSemanticIndex = new HashMap<SemanticClass, Integer>();
            this.doc = mentionDoc;
            this.mentions = null;
            try {
                this.mentions = this.mfact.extractMentions(this.doc);

                int     outerIndex = 0;

                outerloop:
                for (Mention antecedent: this.mentions) {

                    int     salience = 0;

                    if(!antecedent.getPronoun()) {

                        outerIndex += 1;
                        
                        increaseSemanticClassIndex(antecedent);

                        mentionIndex.put(antecedent.getMarkable(), getSemanticClassIndex(antecedent, outerIndex));

                        if(mentionSaliences.containsKey(antecedent.getMarkable())) {
                            continue outerloop;
                        } else {
                            mentionSaliences.put(antecedent.getMarkable(), 0);
                        }

                        String antecedentLemma = antecedent.getHeadString();

                        int innerIndex = 0;

                        innerloop:
                        for (Mention mention: this.mentions) {

                            if(     !mention.getPronoun() &&
                                    (
                                    mention.getSemanticClass().equals(SemanticClass.UNKNOWN)
                                    ||
                                    mention.getSemanticClass().equals(antecedent.getSemanticClass())
                                    )
                              ) {

                                innerIndex += 1;

                                if (   //mention.getHeadString().startsWith(antecedentLemma) ||
                                       //  antecedentLemma.startsWith(mention.getHeadString()) ||
                                       mention.getHeadString().endsWith(antecedentLemma) ||
                                       antecedentLemma.endsWith(mention.getHeadString())) {

                                    salience += 1;
                                    
                                }
                                
                                if (innerIndex <= outerIndex) {
                                    continue innerloop;
                                }

                                if (mention.getHeadString().equals(antecedentLemma)) {
                                    mentionSaliences.put(mention.getMarkable(), salience);
                                } 
                            }
                        }
                    }
                }


            } catch (IOException ex) {
                Logger.getLogger(FE_NonProSalience.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }

    private void increaseSemanticClassIndex(Mention mention) {
        if (mentionSemanticIndex.containsKey(mention.getSemanticClass())) {
            int index = mentionSemanticIndex.get(mention.getSemanticClass());
            mentionSemanticIndex.put(mention.getSemanticClass(), index + 1);
        } else {
            mentionSemanticIndex.put(mention.getSemanticClass(), 0);
        }
    }

    private int getSemanticClassIndex(Mention mention, int defaultresult) {
        if(mention.getSemanticClass().equals(SemanticClass.UNKNOWN)) {
            return defaultresult;
        } else {
            if (!mentionSemanticIndex.containsKey(mention.getSemanticClass())) {
                mentionSemanticIndex.put(mention.getSemanticClass(), 0);
            }
            if (!mentionSemanticIndex.containsKey(SemanticClass.UNKNOWN)) {
                mentionSemanticIndex.put(SemanticClass.UNKNOWN, 0);
            }
            return mentionSemanticIndex.get(mention.getSemanticClass()) + mentionSemanticIndex.get(SemanticClass.UNKNOWN);
        }
    }

    private double getLemmaSalience(Mention antecedent) {

        if(antecedent.getPronoun()) {

            return 0.0;

        } else {

            initMentions(antecedent);

            if(!this.mentionSaliences.containsKey(antecedent.getMarkable())) {
                new RuntimeException("NO SALIENCE FOUND FOR " + antecedent.getMarkableString());
            } else if(!this.mentionIndex.containsKey(antecedent.getMarkable())) {
                new RuntimeException("NO DOCSIZE FOUND FOR " + antecedent.getMarkableString());
            }
            
            int docSize = this.mentionIndex.get(antecedent.getMarkable());
            int salience = this.mentionSaliences.get(antecedent.getMarkable());

            if( docSize > 0 ) {
                return (double) salience/ (double) docSize;
            } else {
                return 0.0;
            }
        }
    }
}
