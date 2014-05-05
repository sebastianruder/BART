/*
 * An XML document type.
 * Localname: pipeline
 * Namespace: 
 * Java type: elkfed.main.xml.PipelineDocument
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * A document containing one pipeline(@) element.
 *
 * This is a complex type.
 */
public class PipelineDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.PipelineDocument
{
    private static final long serialVersionUID = 1L;
    
    public PipelineDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PIPELINE$0 = 
        new javax.xml.namespace.QName("", "pipeline");
    
    
    /**
     * Gets the "pipeline" element
     */
    public elkfed.main.xml.Pipeline getPipeline()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Pipeline target = null;
            target = (elkfed.main.xml.Pipeline)get_store().find_element_user(PIPELINE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "pipeline" element
     */
    public void setPipeline(elkfed.main.xml.Pipeline pipeline)
    {
        generatedSetterHelperImpl(pipeline, PIPELINE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "pipeline" element
     */
    public elkfed.main.xml.Pipeline addNewPipeline()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Pipeline target = null;
            target = (elkfed.main.xml.Pipeline)get_store().add_element_user(PIPELINE$0);
            return target;
        }
    }
}
