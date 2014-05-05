/*
 * FE_SemClass.java
 *
 * Created on August 20, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.pairs;

import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.features.pairs.wn.FE_SemanticClass;
import elkfed.knowledge.SemanticClass;
import elkfed.coref.*;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureType;
import java.util.List;
import elkfed.config.ConfigProperties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.List;
import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.HashMap;
import java.util.Map;
import elkfed.coref.mentions.Mention;
import elkfed.coref.mentions.MentionFactory;

/**
 * ante is the closes mention to ana, that has suitable agreement properties,
does not violate c-command, does not make a pronoun-nonpro pair, and they do not have the same maxnp (exception: appositive)
 * NB: assumes that mentions are in linear order!
 * will not produce mess otherwise, but will not be useful either
 * (will just output "some compatible"
 * NB: with Soon et al sampling, always use FE_Compatible instead (too slow otherwise)
 * @author olga
 */
public class FE_ClosestCompatible implements PairFeatureExtractor {
    public static final FeatureDescription<Boolean> FD_IS_CLOSESTCOMPATIBLE=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"ClosestCompatible");
    public static final FeatureDescription<Boolean> FD_IS_CLOSESTCOMPATIBLE_ANAPRO=
            new FeatureDescription<Boolean>(FeatureType.FT_BOOL,"ClosestCompatible_AnaPro");
    
    private List<Mention> _mentions;
    private MentionFactory _mfact;
    private MiniDiscourse _doc;
    private Map<Integer, Integer> _closest;

    public FE_ClosestCompatible() {
       _mfact = ConfigProperties.getInstance().getMentionFactory();
       _doc=null;
       _closest=new HashMap<Integer, Integer>();
    }
    
    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_IS_CLOSESTCOMPATIBLE);
        fds.add(FD_IS_CLOSESTCOMPATIBLE_ANAPRO);
    }
    
    public void extractFeatures(PairInstance inst) {
        initMentions(inst.getAntecedent());
        if (!_closest.containsKey(inst.getAnaphor().getMentionIdx())) {
          inst.setFeature(FD_IS_CLOSESTCOMPATIBLE,false);
          inst.setFeature(FD_IS_CLOSESTCOMPATIBLE_ANAPRO,false);
          return;
        }
        if (_closest.get(inst.getAnaphor().getMentionIdx())==inst.getAntecedent().getMentionIdx()) {
          inst.setFeature(FD_IS_CLOSESTCOMPATIBLE,true);
          if (inst.getAnaphor().getPronoun()) 
            inst.setFeature(FD_IS_CLOSESTCOMPATIBLE_ANAPRO,true);
          else
            inst.setFeature(FD_IS_CLOSESTCOMPATIBLE_ANAPRO,false);
          return;
        }
        inst.setFeature(FD_IS_CLOSESTCOMPATIBLE,false);
        inst.setFeature(FD_IS_CLOSESTCOMPATIBLE_ANAPRO,false);
    }

private Boolean Compatible(int ante, int ana) {

if (ante<0) return false;
if (ana<0) return false;
if (ante>=_mentions.size()) return false;
if (ana>=_mentions.size()) return false;
Mention m_ana=_mentions.get(ana);
Mention m_ante=_mentions.get(ante);

PairInstance inst=new PairInstance(m_ana,m_ante);
return FE_Compatible.Compatible(inst);

}

    private void initMentions(Mention m) {
// for the first call -- get the closest compatible mention for each m 
//ToDo:  don't be lazy, compute indices!!

      MiniDiscourse doc = m.getMarkable().getMarkableLevel().getDocument();
      if (_doc==null || _doc!=doc)
      _doc=doc;
      _closest=new HashMap<Integer, Integer>();
      _mentions=null;

      try {
        _mentions = _mfact.extractMentions(_doc);
        Integer ana;
        for (ana=1;ana<_mentions.size();ana++) {
          Boolean found=false;
          for (Integer ante=ana-1;ante>0 && !found; ante--) {
            if (Compatible(ante,ana)) {
              found=true;
              _closest.put(_mentions.get(ana).getMentionIdx(),_mentions.get(ante).getMentionIdx());
            }
          }
        }
     }catch (IOException ex) {
          Logger.getLogger(FE_ClosestCompatible.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
