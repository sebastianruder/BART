/*
 * HeadMatcher.java
 *
 * In the current mmax conversion of the TueBa-D/Z the markable spans include 
 * appositions as well. So if we want to do a head match we have to match 
 * against the markables head and against the head of the appositioned phrase
 * as well. The code for matching all possible heads resides in the following
 * class and can be used for all features that do some sort of head matching.
 *
 */
package elkfed.lang.util.de.appositions;

import elkfed.coref.*;
import elkfed.coref.mentions.Mention;
import java.util.ArrayList;
import java.util.List;


public class HeadMatcher {

    public abstract class BooleanMatcher {
        public abstract boolean match(String string1, String string2);
    }

    public abstract class NumericalMatcher {
        public abstract double match(String string1, String string2);
    }

    public boolean getHeadMatch(PairInstance inst, BooleanMatcher headMatcher) {
        final Mention antecedent = inst.getAntecedent();
        final Mention anaphor = inst.getAnaphor();
        return getHeadMatch(antecedent, anaphor, headMatcher);
    }

    public boolean getHeadMatch(Mention mention1, Mention mention2, BooleanMatcher headMatcher) {

        List<String> mention1Alternatives = new ArrayList<String>();
        List<String> mention2Alternatives = new ArrayList<String>();

        String mention1Alternative;
        String mention2Alternative;

        mention2Alternative = AppositionHelper.getApposition(mention2);
        if (mention2Alternative != null) {
            mention2Alternatives.add(mention2Alternative);
        }

        mention2Alternatives.add(mention2.getHeadString());

        mention1Alternative = AppositionHelper.getApposition(mention1);
        if (mention1Alternative != null) {
            mention1Alternatives.add(mention1Alternative);
        }

        mention1Alternatives.add(mention1.getHeadString());

        for (String mention1String : mention1Alternatives) {
            for (String mention2String : mention2Alternatives) {
                if (headMatcher.match(mention1String, mention2String)) {
                    return true;
                }
            }
        }

        return false;
    }
}
