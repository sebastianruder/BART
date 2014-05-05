/*
 * An XML document type.
 * Localname: coref-experiment
 * Namespace: 
 * Java type: elkfed.main.xml.CorefExperimentDocument
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * A document containing one coref-experiment(@) element.
 *
 * This is a complex type.
 */
public class CorefExperimentDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.CorefExperimentDocument
{
    private static final long serialVersionUID = 1L;
    
    public CorefExperimentDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COREFEXPERIMENT$0 = 
        new javax.xml.namespace.QName("", "coref-experiment");
    
    
    /**
     * Gets the "coref-experiment" element
     */
    public elkfed.main.xml.Experiment getCorefExperiment()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Experiment target = null;
            target = (elkfed.main.xml.Experiment)get_store().find_element_user(COREFEXPERIMENT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "coref-experiment" element
     */
    public void setCorefExperiment(elkfed.main.xml.Experiment corefExperiment)
    {
        generatedSetterHelperImpl(corefExperiment, COREFEXPERIMENT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "coref-experiment" element
     */
    public elkfed.main.xml.Experiment addNewCorefExperiment()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Experiment target = null;
            target = (elkfed.main.xml.Experiment)get_store().add_element_user(COREFEXPERIMENT$0);
            return target;
        }
    }
}
