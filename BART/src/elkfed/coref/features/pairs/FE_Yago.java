/*
 * FE_HeadMatch.java
 *
 * Created on July 12, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package elkfed.coref.features.pairs;

import static elkfed.mmax.MarkableLevels.DEFAULT_YAGO_LEVEL;

import java.util.ArrayList;
import java.util.List;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;

/**
 * features for yago-relations 
 * @author olga
 */
public class FE_Yago implements PairFeatureExtractor {
    

//private ### _yagohash=null;

    public static final FeatureDescription<Boolean> FD_YAGO_MEANS=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "YagoMeans");
    public static final FeatureDescription<Boolean> FD_YAGO_TYPEOF=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "YagoTypeof");
    public static final FeatureDescription<Boolean> FD_YAGO_CAPITAL=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL, "YagoCapital");
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_YAGO_MEANS);
        fds.add(FD_YAGO_CAPITAL);
        fds.add(FD_YAGO_TYPEOF);
    }

    public void extractFeatures(PairInstance inst) {
        inst.setFeature(FD_YAGO_MEANS,yago(inst,"means"));
        inst.setFeature(FD_YAGO_CAPITAL,yago(inst,"capital"));
        inst.setFeature(FD_YAGO_TYPEOF,yago(inst,"types"));
    }
private boolean isnostatenp_hack (String commonnoun) {
/* this is a tmp hack, should be moved to yago preprocessing scripts */
  if (commonnoun.equalsIgnoreCase("country")) return true;
  if (commonnoun.equalsIgnoreCase("district")) return true;
  if (commonnoun.equalsIgnoreCase("location")) return true;
  if (commonnoun.equalsIgnoreCase("region")) return true;
  return false;
}

    private boolean isstate_hack (String state) {
/* this is a tmp hack, should be moved to yago preprocessing scripts */
if (state.equalsIgnoreCase("alabama")) return true;
if (state.equalsIgnoreCase("Arkansas")) return true;
if (state.equalsIgnoreCase("California")) return true;
if (state.equalsIgnoreCase("Colorado")) return true;
if (state.equalsIgnoreCase("Hawaii")) return true;
if (state.equalsIgnoreCase("Idaho")) return true;
if (state.equalsIgnoreCase("Illinois")) return true;
if (state.equalsIgnoreCase("Indiana")) return true;
if (state.equalsIgnoreCase("Iowa")) return true;
if (state.equalsIgnoreCase("Kansas")) return true;
if (state.equalsIgnoreCase("Kentucky")) return true;
if (state.equalsIgnoreCase("Louisiana")) return true;
if (state.equalsIgnoreCase("Maine")) return true;
if (state.equalsIgnoreCase("Maryland")) return true;
if (state.equalsIgnoreCase("Massachusetts")) return true;
if (state.equalsIgnoreCase("Michigan")) return true;
if (state.equalsIgnoreCase("Minnesota")) return true;
if (state.equalsIgnoreCase("Missouri")) return true;
if (state.equalsIgnoreCase("Montana")) return true;
if (state.equalsIgnoreCase("Nevada")) return true;
if (state.equalsIgnoreCase("New_Hampshire")) return true;
if (state.equalsIgnoreCase("New_Jersey")) return true;
if (state.equalsIgnoreCase("New_Mexico")) return true;
if (state.equalsIgnoreCase("New_York")) return true;
if (state.equalsIgnoreCase("North_Carolina")) return true;
if (state.equalsIgnoreCase("North_Dakota")) return true;
if (state.equalsIgnoreCase("Ohio")) return true;
if (state.equalsIgnoreCase("Oklahoma")) return true;
if (state.equalsIgnoreCase("Oregon")) return true;
if (state.equalsIgnoreCase("Pennsylvania")) return true;
if (state.equalsIgnoreCase("South_Carolina")) return true;
if (state.equalsIgnoreCase("Tennessee")) return true;
if (state.equalsIgnoreCase("Texas")) return true;
if (state.equalsIgnoreCase("Vermont")) return true;
if (state.equalsIgnoreCase("Virginia")) return true;
if (state.equalsIgnoreCase("Washington")) return true;
if (state.equalsIgnoreCase("West_Virginia")) return true;
if (state.equalsIgnoreCase("Wisconsin")) return true;
if (state.equalsIgnoreCase("Wyoming")) return true;
return false;
}      

    public static boolean emptynoun(String s) {
      if (s.equalsIgnoreCase("person")) return true;
      if (s.equalsIgnoreCase("people")) return true;
      if (s.equalsIgnoreCase("region")) return true;
      if (s.equalsIgnoreCase("location")) return true;
      if (s.equalsIgnoreCase("group")) return true;
      if (s.equalsIgnoreCase("institution")) return true;
      if (s.equalsIgnoreCase("facility")) return true;
      if (s.equalsIgnoreCase("area")) return true;
      if (s.equalsIgnoreCase("some")) return true;
      if (s.equalsIgnoreCase("somebody")) return true;
      if (s.equalsIgnoreCase("someone")) return true;
      if (s.equalsIgnoreCase("anybody")) return true;
      if (s.equalsIgnoreCase("nobody")) return true;
      if (s.equalsIgnoreCase("anyone")) return true;
      if (s.equalsIgnoreCase("others")) return true;
      if (s.equalsIgnoreCase("other")) return true;
      if (s.equalsIgnoreCase("another")) return true;
      if (s.equalsIgnoreCase("most")) return true;
      if (s.equalsIgnoreCase("many")) return true;
      if (s.equalsIgnoreCase("few")) return true;


      return false;
    }
    
    private boolean yago(PairInstance inst, String rel)
    {   

       Markable mante=inst.getAntecedent().getMarkable();
       Markable mana=inst.getAnaphor().getMarkable();

/*
      if (emptynoun(inst.getAnaphor().getHeadString().toLowerCase())) return false;
      if (emptynoun(inst.getAntecedent().getHeadString().toLowerCase())) return false;

       if (inst.getAnaphor().getDnewDeterminer()) return false;

if (rel.equals("types")) {

 if (inst.getAnaphor().isEnamex() &&  !inst.getAntecedent().isEnamex()) return false;

      if (isstate_hack(inst.getAntecedent().getMarkableString().toLowerCase())) {
      if (inst.getAnaphor().getHeadString().equalsIgnoreCase("state")) return true;
      if (isnostatenp_hack(inst.getAnaphor().getHeadString())) return false;
  }
}
*/
//       if (_yagohash==null) LoadYagoHash;

/* start with stupid brute-force, implement hash later */

       MarkableLevel yagoLevel = mana.getMarkableLevel().getDocument().getMarkableLevelByName(DEFAULT_YAGO_LEVEL);

       ArrayList<Markable> yagoLevelMarkables = new ArrayList<Markable> (yagoLevel.getMarkables(null));

       for (Markable yagoMarkable : yagoLevelMarkables){
  
          if (yagoMarkable.getAttributeValue("reltype") == null) continue;
 if (!yagoMarkable.getAttributeValue("reltype").equals(rel)) continue;

boolean oneway=false;

if (!oneway) {
          if (yagoMarkable.getAttributeValue("arg0") != null && 
yagoMarkable.getAttributeValue("arg0").equals(mana.getAttributeValue("id")) &&
yagoMarkable.getAttributeValue("arg1") != null && 
yagoMarkable.getAttributeValue("arg1").equals(mante.getAttributeValue("id"))) return true;
}

          if (yagoMarkable.getAttributeValue("arg0") != null && 
yagoMarkable.getAttributeValue("arg0").equals(mante.getAttributeValue("id")) &&
yagoMarkable.getAttributeValue("arg1") != null && 
yagoMarkable.getAttributeValue("arg1").equals(mana.getAttributeValue("id"))) return true;

}



       return false;   
    }
}
