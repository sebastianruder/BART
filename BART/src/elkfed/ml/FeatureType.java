/*
 * FeatureType.java
 *
 * Created on July 9, 2007, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package elkfed.ml;

import edu.stanford.nlp.trees.Tree;
import java.lang.reflect.Modifier;

/** a feature type represents the kind of values (together with some semantics)
 *  that a feature for use in a classifier may take
 *  The idea is that:
 *  (i) through the type parameter, it is possible to use feature types
 *      type-safely even when using dynamic typing (e.g. in a map).
 *  (ii) different machine learning libraries interpret features as consistently
 *      as it is reasonably possible.
 */
public enum FeatureType {
    FT_BOOL(Boolean.class, "Boolean"),
    FT_SCALAR(Number.class, "Scalar/Double"),
    FT_NOMINAL_ENUM(Enum.class, "Nominal/Enum"),
    FT_STRING(String.class, "String"),
    FT_TREE_STRING(String.class, "Tree/String-based"),
    FT_TREE_TREE(Tree.class, "Tree/Tree-based");
    
    public final Class cls;
    public final String name;
    /** Creates a new instance of FeatureType */
    FeatureType(Class cls, String name) {
        this.cls=cls;
        this.name=name;
    }

    /** checks if the class given to a FeatureDescriptor is actually compatible
     *  with the Featuretype.
     *  If it can't be assigned to a variable of that type (e.g. Number and String),
     *  that's a problem.
     *  If the FeatureDescription class passed Enum here, someone has forgotten to
     *  pass the right class to the constructor (i.e., instead of calling
     *  new FeatureDescription(FT_NOMINAL_ENUM, "MyEnum")
     *  they should be calling
     *  new FeatureDescription(FT_NOMINAL_ENUM, MyEnum.class, "MyEnum")
     */
    void check(Class subcls)
    {
        if (! cls.isAssignableFrom(subcls))
        {
            throw new RuntimeException("Classes do not match!");
        }
        if (cls==Enum.class && Modifier.isAbstract(subcls.getModifiers()))
        {
            throw new RuntimeException("Pass the concrete class instead of Enum!");
        }
    }

}
