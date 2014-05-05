/*
 * Clustering.java
 *
 * Created on July 16, 2007, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.coref.util;

import static elkfed.mmax.MarkableLevels.COREF_SET_ATTRIBUTE;
import static elkfed.mmax.MarkableLevels.DEFAULT_LEX_LEVEL;
import static elkfed.mmax.MarkableLevels.DEFAULT_RESPONSE_LEVEL;
import static elkfed.mmax.MarkableLevels.DIRECT_ANT_ATTRIBUTE;
import static elkfed.mmax.pipeline.MarkableCreator.ISPRENOMINAL_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.cscott.jutil.DisjointSet;
import elkfed.config.ConfigProperties;
import elkfed.coref.mentions.Mention;
import elkfed.mmax.minidisc.Markable;
import elkfed.mmax.minidisc.MarkableLevel;
import elkfed.mmax.minidisc.MiniDiscourse;
import gnu.trove.map.hash.TObjectIntHashMap;

/** Class to add the coreference chains to a MMAX document 
 *
 * @author brett.shwom
 */
public class Clustering
{    
    private static final Logger _logger=Logger.getAnonymousLogger();
    
    private static final String ETYPE = "etype";
    private static final String MTYPE = "mtype";
    private static final String COS = "cos";
    private static final String ONSET = "onset";
    private static final String COE = "coe";
    private static final String OFFSET = "offset";
    
    private static final Comparator COMP = MiniDiscourse.DISCOURSEORDERCMP;
    
    public static void addClustersToMMAX(DisjointSet<Mention> partition,
            Map<Mention, Mention> antecedents,
            MiniDiscourse document )
    {        
        // the map where every mention is mapped to the canonical
        // representative of the set
        Map<Mention, Mention> mentionMap = partition.asMap();
        
//System.err.println("in Clustering total of markables=" + mentionMap.size());
        // lots of map juggling here: we map each Mention to the
        // the id of the coreference set it belongs to
        Map<Mention, String> mention2setid = new HashMap<Mention,String>();
        int n_ids=0;
        // first, make entries for the partition representatives only
        for (Mention m: mentionMap.values())
        {
            if (!mention2setid.containsKey(m))
            {
                mention2setid.put(m,"set_"+n_ids++);
            }
        }
//System.err.println("in Clustering partition size (step2) =" + mention2setid.size());
        
        // add all other mentions to the map
        for (Mention m: mentionMap.keySet())
        {
            Mention key_m=mentionMap.get(m);
            mention2setid.put(m,mention2setid.get(key_m));
        }

//System.err.println("in Clustering total of markables (step3) =" + mention2setid.size());
        if (ConfigProperties.getInstance().getBalanceKeyAndResponse()) {
            mention2setid = filterSingleSets(mention2setid);
        }
        // add the markables to the document, optionally with filtering
        if (ConfigProperties.getInstance().getFilterPrenominals())
        { addMarkableLevel(filterPrenominals(mention2setid), antecedents, document); }
        else
        { addMarkableLevel(mention2setid, antecedents, document); }
    }
    
    private static void addMarkableLevel(
            final Map<Mention, String> documentCorefSets, 
            final Map<Mention, Mention> antecedents,
            final MiniDiscourse document )
    {
        // get the response level
        final MarkableLevel responseLevel =
                document.getMarkableLevelByName(DEFAULT_RESPONSE_LEVEL);
        // gets the lex level (we need it to store onset/offset)
        final MarkableLevel lexLevel =
                document.getMarkableLevelByName(DEFAULT_LEX_LEVEL);        
        final TObjectIntHashMap<Mention> markable_ids=new TObjectIntHashMap<Mention>();
        final HashMap<Markable,Mention> markable_ante_ids=new HashMap<Markable,Mention>();
        Map<String,String> responseLevelAttributes = new HashMap<String,String>();
        responseLevelAttributes.put("mmax_level", DEFAULT_RESPONSE_LEVEL);

        // for each markable in document belonging to a coreference set
        for (Mention corefElement : documentCorefSets.keySet())
        {
            // the attributes' map
            final HashMap<String, String> attributes = new HashMap<String, String>(responseLevelAttributes);
            attributes.put(COREF_SET_ATTRIBUTE, documentCorefSets.get(corefElement));
            String min_ids=corefElement.getMarkable().getAttributeValue("min_ids");
            if (min_ids!=null) {
                attributes.put("min_ids", min_ids);
            }
            // create a response markable
            Markable response = responseLevel.addMarkable(
                    corefElement.getMarkable().getLeftmostDiscoursePosition(),
                    corefElement.getMarkable().getRightmostDiscoursePosition(),
                    attributes);
            markable_ids.put(corefElement, response.getIntID());
            if (antecedents.containsKey(corefElement)) {
                markable_ante_ids.put(response,antecedents.get(corefElement));
            }
            // add the attributes:
            // 0. COS (onset) - COE (offset)
            // 1. ETYPE: GPE, LOC, PERSON, etc.
            // 2. MTYPE: noun, pronoun, name
            //addAttributes(corefElement, attributes, lexLevel);
            
            // add the markable to a coreference set
            response.setAttributeValue(COREF_SET_ATTRIBUTE, 
                    documentCorefSets.get(corefElement));
        }
        for (Markable response: markable_ante_ids.keySet()) {
            int ante_id=markable_ids.get(markable_ante_ids.get(response));
            response.setAttributeValue(DIRECT_ANT_ATTRIBUTE,
                    "markable_"+ante_id);
        }
        responseLevel.saveMarkables();
    }
    
    private static Map<Mention, String> filterPrenominals(Map<Mention, String> mention2setid)
    {

        // first get the reverse mapping
        final Map<String, Set<Mention>> setid2mention = getSetid2MentionMap(mention2setid);
        
        // the list holding the sets to remove
        final List<String> set2remove = getPrenominalOnlyChains(setid2mention);
     
        // remove!
        int mentionsRemoved = 0;
        final Map<Mention, String> filteredMap = new HashMap<Mention, String>(mention2setid);
        for (Mention mention : mention2setid.keySet())
        {
            if (set2remove.contains(mention2setid.get(mention)))
            { filteredMap.remove(mention); mentionsRemoved++; }
        }
        System.out.println(
                "Removed " + set2remove.size() + " sets with " + mentionsRemoved + " mentions ");
        return filteredMap;
    }

    /**
     * This is only to filter out singletons that have been added by the
     * balanceKeyAndResponse step. This is optional and only to not litter
     * the mmax file with singletons that are only usefull for computation.
     *
     * @author samuel
     * @param mention2setid
     * @return A map where the singletons have been filtered out.
     */
    private static Map<Mention, String> filterSingleSets(Map<Mention, String> mention2setid) {

        final Map<Mention, String> filteredMap = new HashMap<Mention, String>(mention2setid);

        ArrayList<String> corefSets = new ArrayList<String>(mention2setid.values());
        ArrayList<String> removableCorefSets = new ArrayList<String>();
        for (String corefSet : corefSets) {
            if(corefSets.indexOf(corefSet)==corefSets.lastIndexOf(corefSet)) {
                removableCorefSets.add(corefSet);
            }
        }

        for (Mention mention : mention2setid.keySet()) {
            if (removableCorefSets.contains(mention2setid.get(mention))) {
                filteredMap.remove(mention);
            }
        }
        return filteredMap;
    }


    /** Creates the (reverse) map mapping the ids of the coreference sets to their mentions */
    private static Map<String, Set<Mention>> getSetid2MentionMap(Map<Mention, String> mention2setid)
    {
        final Map<String, Set<Mention>> setid2mention = new HashMap<String, Set<Mention>>();
        
        // make root for each set
        for (String setid : mention2setid.values())
        { setid2mention.put(setid, new HashSet<Mention>()); }        
        
        // do the rest
        for (Mention corefElement : mention2setid.keySet()) 
        { setid2mention.get(mention2setid.get(corefElement)).add(corefElement); }
        
        return setid2mention;
    }
    
    /** Gets the list of coreference sets containing prenominals only */
    private static List<String> getPrenominalOnlyChains(Map<String, Set<Mention>> setid2mention)
    {
        // the list holding the sets with prenominals only
        final List<String> sets2remove = new ArrayList<String>();
        
        // for each coreference chain
        ForSet:
        for (String setid : setid2mention.keySet())
        {
            // for each element in the chain
            for (Mention corefElement : setid2mention.get(setid))
            {
                if (
                       !Boolean.parseBoolean(
                            corefElement.getMarkable().getAttributeValue(ISPRENOMINAL_ATTRIBUTE)
                         )
                )
                {
                    // if a prenominal is found, keep the chain
                    continue ForSet;
                }
            }
            // if we made it here, remove the set
            sets2remove.add(setid);
        }
        return sets2remove;
    }
    

}
