/*
 * XML Type:  Pipeline
 * Namespace: 
 * Java type: elkfed.main.xml.Pipeline
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Pipeline(@).
 *
 * This is a complex type.
 */
public class PipelineImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Pipeline
{
    private static final long serialVersionUID = 1L;
    
    public PipelineImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COMPONENT$0 = 
        new javax.xml.namespace.QName("", "component");
    
    
    /**
     * Gets array of all "component" elements
     */
    public elkfed.main.xml.PipelineComponent[] getComponentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COMPONENT$0, targetList);
            elkfed.main.xml.PipelineComponent[] result = new elkfed.main.xml.PipelineComponent[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "component" element
     */
    public elkfed.main.xml.PipelineComponent getComponentArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.PipelineComponent target = null;
            target = (elkfed.main.xml.PipelineComponent)get_store().find_element_user(COMPONENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "component" element
     */
    public int sizeOfComponentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COMPONENT$0);
        }
    }
    
    /**
     * Sets array of all "component" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setComponentArray(elkfed.main.xml.PipelineComponent[] componentArray)
    {
        check_orphaned();
        arraySetterHelper(componentArray, COMPONENT$0);
    }
    
    /**
     * Sets ith "component" element
     */
    public void setComponentArray(int i, elkfed.main.xml.PipelineComponent component)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.PipelineComponent target = null;
            target = (elkfed.main.xml.PipelineComponent)get_store().find_element_user(COMPONENT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(component);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "component" element
     */
    public elkfed.main.xml.PipelineComponent insertNewComponent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.PipelineComponent target = null;
            target = (elkfed.main.xml.PipelineComponent)get_store().insert_element_user(COMPONENT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "component" element
     */
    public elkfed.main.xml.PipelineComponent addNewComponent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.PipelineComponent target = null;
            target = (elkfed.main.xml.PipelineComponent)get_store().add_element_user(COMPONENT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "component" element
     */
    public void removeComponent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COMPONENT$0, i);
        }
    }
}
