package elkfed.coref.algorithms.soon;

import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.TriValued;
import java.util.ArrayList;
import java.util.List;
import elkfed.coref.features.pairs.*;
import edu.stanford.nlp.trees.Tree;

/**
 * hard filters for soon-style decoding
 * NB: note that relevant features should be used at the testing stage (but may be better avoided at the training stage though)
 * @author olga
 */


public class PostFilter {
  List<PairFeatureExtractor> _fesfi;
  public PostFilter(){
  _fesfi=new ArrayList<PairFeatureExtractor> ();
/*
  _fesfi.add(new FE_StringMatch());
  _fesfi.add(new FE_MentionType());
  _fesfi.add(new FE_Yago());
*/
  ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fesfi) {
            fe.describeFeatures(fds);
        }
  }




  public boolean FilterOut(PairInstance lnk) {
/*
  for (PairFeatureExtractor fe: _fesfi) {
    fe.extractFeatures(lnk);
  }
*/
//------  for debugging purposes only ---------- //

//  if (lnk.getAnaphor().getPronoun()) return true;

//---------- for conll ---------//

   if(ConfigProperties.getInstance().getLanguagePlugin() instanceof EnglishLanguagePlugin) {
 if (ConfigProperties.getInstance().getLanguagePlugin().isExpletiveRB(lnk.getAnaphor())) return true;

 if (ConfigProperties.getInstance().getLanguagePlugin().isExpletiveRB(lnk.getAntecedent())) return true;
}

  return false;

/*

//---------- for ace ----------//

// remove bare plurals that are not exactly the same
    if (lnk.getFeature(FE_StringMatchExtra.FD_IS_BAREPLSTRINGMISMATCH)) return true;

// remove locations that are modified -- e.g. Old Madras vs. Madras -- todo
// for now -- just remove locs that are not stringmatch or capital

  if (lnk.getFeature(FE_MentionType.FD_ARE_PROPERNAMES)) {
    if (lnk.getFeature(FE_StringMatch.FD_IS_STRINGMATCH)) return false;
    if (lnk.getFeature(FE_Yago.FD_YAGO_CAPITAL)) return false;
    final String enamexClass = lnk.getAnaphor().getEnamexType();
    if (enamexClass.startsWith("loc")||
       enamexClass.startsWith("gpe") ||
       enamexClass.startsWith("gsp")) return true;

    }
    return false;
*/
  }

}