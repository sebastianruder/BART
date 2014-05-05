/*
 * SoonDecoder.java
 *
 * Created on July 12, 2007, 5:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.coref.algorithms.soon;

import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import elkfed.coref.PairFeatureExtractor;
import elkfed.coref.PairInstance;
import elkfed.coref.algorithms.soon.PostFilter;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.ml.FeatureDescription;
import elkfed.ml.TriValued;
import elkfed.ml.OfflineClassifier;
import java.util.ArrayList;
import java.util.List;
import elkfed.coref.features.pairs.*;

/**
 *
 * @author versley
 */
public class SoonDecoder extends LocalDecoder {
    private static final int CHUNK_SIZE = 10;
    List<PairFeatureExtractor> _fes;
    OfflineClassifier _model;
    PostFilter _filter;

    public SoonDecoder(List<PairFeatureExtractor> fes,
            OfflineClassifier model) {
        _fes=fes;
        _model=model;
        _filter= new PostFilter();
        ArrayList<FeatureDescription> fds = new ArrayList<FeatureDescription>();
        for (PairFeatureExtractor fe : _fes) {
            fe.describeFeatures(fds);
        }
        fds.add(PairInstance.FD_POSITIVE);
        _model.setHeader(fds);
    }

    public int resolveSingle(List<Mention> mentions, int ana) {
        List<PairInstance> candLinks=new ArrayList<PairInstance>();
        List<Boolean> result=new ArrayList<Boolean>();
        Mention m_i=mentions.get(ana);
        base_j_loop:
        for (int base_j=ana-1; base_j>=0; base_j -= CHUNK_SIZE) {
            candLinks.clear();
            result.clear();
            final int low_j;
            if (base_j<CHUNK_SIZE)
                low_j=0;
            else
                low_j=base_j-CHUNK_SIZE+1;
            for (int j=base_j; j>=low_j; j--) {
                Mention m_j=mentions.get(j);
                if(ConfigProperties.getInstance().getLanguagePlugin() instanceof GermanLanguagePlugin) {
                    if(!m_i.getRelPronoun()) {
                        if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                        continue;
                    }
                } else {
                    if (m_i.overlapsWith(m_j) || m_j.embeds(m_i))
                    continue;
                }
                PairInstance inst=new PairInstance(m_i, m_j);
                for (PairFeatureExtractor fe: _fes) {
                    fe.extractFeatures(inst);
                }
                // for debugging purposes, add the isCoreferent value to
                // the learning instances
                inst.setFeature(PairInstance.FD_POSITIVE,
                        inst.getAnaphor().isCoreferent(inst.getAntecedent()));
                if (_filter.FilterOut(inst)) continue;
                candLinks.add(inst);
            }
            _model.classify(candLinks,result);
            for (int j=0; j<candLinks.size(); j++) {
                if (result.get(j)) {
                    PairInstance lnk=candLinks.get(j);
                    return mentions.indexOf(lnk.getAntecedent());
                }
            }
	}

        return -1;
    }
}
