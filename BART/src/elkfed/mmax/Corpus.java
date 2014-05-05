/*
 * Copyright 2007 EML Research
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.mmax;

import elkfed.mmax.minidisc.MiniDiscourse;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/** A corpus is a collection of {@link elkfed.mmax.minidisc.MiniDiscourse},
 *  objects, in the form of an ArrayList.
 *
 * @author ajern
 */
public class Corpus extends ArrayList<MiniDiscourse>
{
    private static final long serialVersionUID = -6127768357075602171L;
	
    /* The corpus String identifier */
    private String id;
    
    /** Constructs an initially empty Corpus */
    public Corpus() { super(); }

    /** Constructs a Corpus containing the elements of the
     *  specified collection, in the order they are returned
     *  by the collection's iterator.
     */
    public Corpus(Collection<MiniDiscourse> discourses)
    {
        addAll(discourses);
    }
    
    /** Gets the Corpus ID */
    public String getId()
    { return id; }
    
    /** Sets the Corpus ID */
    public void setId(String id)
    { this.id = id; }
    
    /**  Returns a Map view of the corpus, where every documentID
     *   is mapped to its MMAX2Discourse
     */
    public Map<String,MiniDiscourse> asMap()
    {
        final Map<String,MiniDiscourse> map = new HashMap<String,MiniDiscourse>();
        for (MiniDiscourse document : this)
        { map.put(document.getNameSpace(), document); }
        return map;
    }
}