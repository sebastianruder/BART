/*
 * FeatureDescription.java
 *
 * Created on July 9, 2007, 5:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

/** Describes one feature that is used in a ML (or just general) context.
 * The FeatureType (<i>type</i>) is meant to specify the allowable values and their
 * interpretation, while the <i>name</i> should identify the feature.
 */
public class FeatureDescription<T>
{
    public final String name;
    public final FeatureType type;
    public Class<T> cls;
    /** Creates a new instance of FeatureDescription */
    public FeatureDescription(FeatureType type, Class<T> cls, String name) {
        type.check(cls);
        this.type=type;
        this.name=name;
        this.cls=cls;
    }
    
    @SuppressWarnings("unchecked")
	public FeatureDescription(FeatureType type, String name) {
        this(type,type.cls,name);
    }
    
    public String toString() {return "FD[" + name + "|" + type + "|" + cls.getName()+"]";}
    
    public boolean equals(Object o)
    {
        if (o==null) 
        { return false; }
        else
        {
            try{
                FeatureDescription<T> fd=(FeatureDescription<T>)o;
                return (fd.name.equals(this.name) &&
                        fd.type.equals(this.type) &&
                        fd.cls.equals(this.cls));            
            }
            catch (ClassCastException e)
            {
                return false;
            }
        }
    }
    
    public int hashCode()
    {
        return name.hashCode()+type.hashCode()+cls.hashCode();
    }
}
