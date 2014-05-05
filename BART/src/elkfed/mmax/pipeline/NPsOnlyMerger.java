package elkfed.mmax.pipeline;

import static elkfed.mmax.MarkableLevels.DEFAULT_CHUNK_LEVEL;

import java.util.HashMap;
import elkfed.mmax.minidisc.Markable;

public class NPsOnlyMerger extends Merger {
    
    /** Add coreference candidate markables to a document */
    @Override
    protected void addMarkables()
    {
        // we take only the NPs
        for (Markable markable : nps)
        { addMarkable(markable, DEFAULT_CHUNK_LEVEL); }
    }

    /** Add base attributes of a markable to the attribute hashmap */
    @Override
    protected HashMap<String,String> addBaseAttributes(
            final Markable markable, 
	    final HashMap<String,String> attributes,
	    final String type)
    {
        attributes.put(ISPRENOMINAL_ATTRIBUTE, isPrenominal());
        attributes.put(TYPE_ATTRIBUTE, DEFAULT_CHUNK_LEVEL);
        attributes.put(LABEL_ATTRIBUTE, 
		       markable.getAttributeValue(TAG_ATTRIBUTE));
        return attributes;
    }
        
    @Override
    protected void runComponent()
    {
        // 0. clean up namely remove leading and trailing genitives and punctuation
        super.cleanUp();
        
        this.nps = getNPs();
//	this.enamexes = getEnamex();
    }
}
