package elkfed.lang.util.de.appositions;

import java.util.ArrayList;

import elkfed.coref.mentions.Mention;
import elkfed.mmax.MarkableLevels;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;

/**
 *
 * @author samuel
 */
public class AppositionHelper {

    public static String getApposition(Mention mention) {

        String nnPhrase = getApposition(mention, "NN");
        String nePhrase = getApposition(mention, "NE");

        if (nnPhrase == null || nnPhrase.equals(mention.getHeadString())) {
            return nePhrase;
        } else {
            return nnPhrase;
        }
    }

    public static String getApposition(Mention mention, String appositionedPosTag) {
    	MiniDiscourse doc=mention.getDocument();
        MarkableLevel deprel = doc.getMarkableLevelByName(MarkableLevels.DEFAULT_DEPREL_LEVEL);

        ArrayList<String> phraseWithPos = mention.getHighestProjectingPhraseWithPOS(appositionedPosTag);

        String result = null;

        if (phraseWithPos != null && (result == null || result.equals(mention.getHeadString()))) {
            int from = doc.DiscoursePositionFromDiscourseElementID(phraseWithPos.get(0));
            int to = doc.DiscoursePositionFromDiscourseElementID(phraseWithPos.get(phraseWithPos.size() - 1));
            if (deprel.isLevelAttributeValueInRange("tag", "APP", from, to)) {
                result = mention.getJoinedStringFromDiscIds(from, to, "lemma");
            }
        }

        return result;
    }
}
