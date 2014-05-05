/*
 * AdjectiveMap.java
 *
 * Created on August 13, 2007, 12:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.nlp.util;

import elkfed.config.ConfigProperties;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LanguagePlugin.TableName;

/**
 *
 * @author yannick
 */
public class AdjectiveMap {
    private static AdjectiveMap _instance;
    
    public static AdjectiveMap getInstance()
    {
        if (_instance==null)
        {
            _instance=new AdjectiveMap();
        }
        return _instance;
    }
    
    public boolean hasKey(String adj)
    {
        LanguagePlugin plugin=ConfigProperties.getInstance().getLanguagePlugin();
        return (plugin.lookupAlias(adj, TableName.AdjMap)==null);
    }
    
    public String map(String adj)
    {
        LanguagePlugin plugin=ConfigProperties.getInstance().getLanguagePlugin();
        String result=plugin.lookupAlias(adj, TableName.AdjMap);
        if (result==null)
            return adj;
        else
            return result;
    }
}
