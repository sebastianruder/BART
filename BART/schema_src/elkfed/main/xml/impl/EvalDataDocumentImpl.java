/*
 * An XML document type.
 * Localname: eval-data
 * Namespace: 
 * Java type: elkfed.main.xml.EvalDataDocument
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * A document containing one eval-data(@) element.
 *
 * This is a complex type.
 */
public class EvalDataDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.EvalDataDocument
{
    private static final long serialVersionUID = 1L;
    
    public EvalDataDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVALDATA$0 = 
        new javax.xml.namespace.QName("", "eval-data");
    
    
    /**
     * Gets the "eval-data" element
     */
    public elkfed.main.xml.EvalData getEvalData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.EvalData target = null;
            target = (elkfed.main.xml.EvalData)get_store().find_element_user(EVALDATA$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eval-data" element
     */
    public void setEvalData(elkfed.main.xml.EvalData evalData)
    {
        generatedSetterHelperImpl(evalData, EVALDATA$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eval-data" element
     */
    public elkfed.main.xml.EvalData addNewEvalData()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.EvalData target = null;
            target = (elkfed.main.xml.EvalData)get_store().add_element_user(EVALDATA$0);
            return target;
        }
    }
}
