package elkfed.coref.features.pairs.de;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.Synset;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.mentions.Mention;
import elkfed.knowledge.SemanticClass;
import elkfed.lang.util.de.appositions.AppositionHelper;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;

/**
 *
 * @author samuel
 */
public class FE_GN_RelatednessAllSensesWuPalmer implements PairFeatureExtractor {

	GermaNet gnet = null;
	
	GermaNet getGWN() {
		if (gnet == null) {
			//TODO: load GermaNet path from config.properties
			try {
				gnet = new GermaNet("");
			} catch (XMLStreamException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return gnet;
	}
	
    public enum FE_Rating {
        
        UNKNOWN(0),
        NOT_RELATED(1),
        SIGNIFICANTLY_RELATED(2),
        STRONGLY_RELATED(3);

        int order;
        FE_Rating(int order) {
            this.order = order;
        }
        int order() {
            return order;
        }
    }

    public static final FeatureDescription<FE_Rating> FD_GERMANET_SIMILARITY =
            new FeatureDescription<FE_Rating>(FeatureType.FT_NOMINAL_ENUM, FE_Rating.class, "GermaNet_Similarity");

    /** Creates a new instance of FE_SemClass */
    public FE_GN_RelatednessAllSensesWuPalmer() {
    }

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_GERMANET_SIMILARITY);
    }

    public void extractFeatures(PairInstance inst) {

        Mention anaphor = inst.getAnaphor();
        Mention antecedent = inst.getAntecedent();

        // Both proper nouns: compare semantic class if available
        if (anaphor.getProperName() && antecedent.getProperName()) {

            if(!anaphor.getSemanticClass().equals(SemanticClass.UNKNOWN) && !antecedent.getSemanticClass().equals(SemanticClass.UNKNOWN)) {
                if(anaphor.getSemanticClass().equals(antecedent.getSemanticClass())) {
                    inst.setFeature(FD_GERMANET_SIMILARITY, FE_Rating.SIGNIFICANTLY_RELATED);
                } else if(!anaphor.getSemanticClass().equals(antecedent.getSemanticClass())) {
                    inst.setFeature(FD_GERMANET_SIMILARITY, FE_Rating.NOT_RELATED);
                }
            } else {
                inst.setFeature(FD_GERMANET_SIMILARITY, FE_Rating.UNKNOWN);
            }

        /* Just one proper noun:    look for a nominal apposition to the proper noun
         *                          or else take the german name of the semantic class
         *                          as word
         */
        } else if (anaphor.getProperName() || antecedent.getProperName()) {

            Mention enamexMention;
            Mention nominalMention;

            if (anaphor.getProperName()) {
                enamexMention = anaphor;
                nominalMention = antecedent;
            } else {
                enamexMention = antecedent;
                nominalMention = anaphor;
            }

            String enamexNN = AppositionHelper.getApposition(enamexMention, "NN");

            String word1;

            if (enamexMention.getSemanticClass() == SemanticClass.UNKNOWN && enamexNN == null) {
                inst.setFeature(FD_GERMANET_SIMILARITY, FE_Rating.UNKNOWN);
                return;
            } else if (enamexNN != null) {
                word1 = capitalizeFirst(enamexNN);
            } else {
                word1 = getGermanNameForSemanticClass(enamexMention);
            }

            String word2 = capitalizeFirst(nominalMention.getHeadString());

            FE_Rating rating = getRelatednessRating(word1, word2);
            if (rating == FE_Rating.STRONGLY_RELATED) {
                rating = FE_Rating.SIGNIFICANTLY_RELATED;
            }
            inst.setFeature(FD_GERMANET_SIMILARITY, rating);

        } else {

            String anaphorString = capitalizeFirst(anaphor.getHeadString());
            String antecedentString = capitalizeFirst(antecedent.getHeadString());

            FE_Rating rating = getRelatednessRating(anaphorString, antecedentString);

            if (rating == FE_Rating.UNKNOWN) {
                inst.setFeature(FD_GERMANET_SIMILARITY, FE_Rating.UNKNOWN);
            } else {
                inst.setFeature(FD_GERMANET_SIMILARITY, rating);
            }
        }
    }
    
    

    /**
     * Compute the relatedness of two words with the pathfinder implementation
     * of the WuPalmer similarity measure using all senses (due to a lack of 
     * predominant sense information in GermaNet) and take the result with the
     * highest relatedness.
     *
     * @param   word1
     * @param   word2
     * @return  A relatedness rating beeing either NOT_RELATED, SIGNIFICANTLY_RELATED,
     *          STRONGLY_RELATED or UNKNOWN if one of the words could not be found in
     *          GermaNet.
     */
    private FE_Rating getRelatednessRating(String word1, String word2) {
    	GermaNet gds = getGWN();
    	
        List<Synset> word1Synsets = gds.getSynsets(word1);
        List<Synset> word2Synsets = gds.getSynsets(word2);

        if (word1Synsets.size() > 0 && word2Synsets.size() > 0 ) {
        	
        	//TODO: the public GermaNet api doesn't seem to have
        	//      the relatedness measures?!

//            ArrayList<AbstractGermaNetRelatednessMeasure> measures =  new ArrayList<AbstractGermaNetRelatednessMeasure>();
//
//            measures.add(new WuPalmerMeasure(gds));
//
//            ArrayList<FE_Rating> ratings = new ArrayList<FE_Rating> ();
//
//            for(AbstractGermaNetRelatednessMeasure measure: measures) {
//                for (Synset word1Synset: word1Synsets) {
//                for (Synset word2Synset: word2Synsets) {
//                switch (measure.getRating(word1Synset, word2Synset)) {
//                    case NOT_RELATED:
//                        ratings.add(FE_Rating.NOT_RELATED);
//                        break;
//
//                    case SIGNIFICANTLY_RELATED:
//                        ratings.add(FE_Rating.SIGNIFICANTLY_RELATED);
//                        break;
//
//                    case STRONGLY_RELATED:
//                        ratings.add(FE_Rating.STRONGLY_RELATED);
//                        break;
//
//                    default:
//                        ratings.add(FE_Rating.UNKNOWN);
//                }
//                }
//                }
//            }

            //FE_Rating[] result = (FE_Rating[]) ratings.toArray(new FE_Rating[ratings.size()]);
        	FE_Rating[] result = null;

            Arrays.sort(
                        result,
                        new Comparator<FE_Rating>() {
                            public int compare(FE_Rating rating1, FE_Rating rating2) {
                                int delta=rating1.order()-rating2.order();
                                if (delta<0.0) {
                                    return +1;
                                } else if (delta>0.0) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        }
            );

            return result[0];

        } else {
//            if (word1Synset == null) {
//                System.err.println("GERMANET couldnt find: " + word1);
//            }
//            if (word2Synset == null) {
//                System.err.println("GERMANET couldnt find: " + word2);
//            }
            return FE_Rating.UNKNOWN;
        }
    }

    private String capitalizeFirst(String s) {
        return (s.length() > 0) ? Character.toUpperCase(s.charAt(0)) + s.substring(1) : s;
    }

    /**
     * @param mention
     * @return a string representation of the german noun for this mentions semantic class
     */
    public static String getGermanNameForSemanticClass(Mention mention) {
        SemanticClass semclass = mention.getSemanticClass();

        switch (semclass) {

            case PERSON:
                return "Person";
            case MALE:
                return "Mann";
            case FEMALE:
                return "Frau";
            case ORGANIZATION:
                return "Organisation";
            case LOCATION:
                return "Ort";
            case DATE:
                return "Datum";
            case EVENT:
                return "Ereignis";

            default:
                return "Unbekannt";
        }
    }

    public static void main(String[] args) {
        FE_GN_RelatednessAllSensesWuPalmer fesim = new FE_GN_RelatednessAllSensesWuPalmer();
        String word1 = null;
        loop:
        for(String word2: args) {
            if(word1==null) {
                word1 = word2;
                continue loop;
            }
            System.out.println(word1 + " + " +  word2 + " = " + fesim.getRelatednessRating(word1, word2));
            word1 = null;
        }
    }
}
