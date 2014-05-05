/*
 * XML Type:  Experiment
 * Namespace: 
 * Java type: elkfed.main.xml.Experiment
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Experiment(@).
 *
 * This is a complex type.
 */
public class ExperimentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Experiment
{
    private static final long serialVersionUID = 1L;
    
    public ExperimentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SYSTEM$0 = 
        new javax.xml.namespace.QName("", "system");
    
    
    /**
     * Gets the "system" element
     */
    public elkfed.main.xml.System getSystem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.System target = null;
            target = (elkfed.main.xml.System)get_store().find_element_user(SYSTEM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "system" element
     */
    public void setSystem(elkfed.main.xml.System system)
    {
        generatedSetterHelperImpl(system, SYSTEM$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "system" element
     */
    public elkfed.main.xml.System addNewSystem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.System target = null;
            target = (elkfed.main.xml.System)get_store().add_element_user(SYSTEM$0);
            return target;
        }
    }
}
