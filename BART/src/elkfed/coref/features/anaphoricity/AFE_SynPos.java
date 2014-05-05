/*
 * AFE_SynPos.java
 *
 * Created on August 6, 2007, 6:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.features.anaphoricity;

import elkfed.coref.AnaphoricityInstance;
import elkfed.ml.FeatureDescription;
import elkfed.ml.FeatureExtractor;
import elkfed.ml.FeatureType;
import java.util.List;

/**
 *
 * @author yannick
 */
public class AFE_SynPos implements FeatureExtractor<AnaphoricityInstance>
{
    public static final FeatureDescription<String> FD_SYN_POS=
            new FeatureDescription<String>(FeatureType.FT_STRING,"AFE_SynPos");

    public void describeFeatures(List<FeatureDescription> fds) {
        fds.add(FD_SYN_POS);
    }
    
    private static int nthIndex(String path,String sep,int n)
    {
        int idx=0;
        for (int i=0;i<n;i++)
        {
            idx=path.indexOf(sep,idx)+1;
            if (idx==0) return -1;
        }
        return idx;
    }

     public void extractFeatures(AnaphoricityInstance inst) {
        String rootPath=inst.getMention().getRootPath();
        int off=nthIndex(rootPath,".",3);
        if (off == -1)
        {
            inst.setFeature(FD_SYN_POS, rootPath);
        }
        else
        {
            inst.setFeature(FD_SYN_POS, rootPath.substring(0,off));
        }
    }
    
}
