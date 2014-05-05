/*
 * XML Type:  Extractors
 * Namespace: 
 * Java type: elkfed.main.xml.Extractors
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Extractors(@).
 *
 * This is a complex type.
 */
public class ExtractorsImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Extractors
{
    private static final long serialVersionUID = 1L;
    
    public ExtractorsImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EXTRACTOR$0 = 
        new javax.xml.namespace.QName("", "extractor");
    
    
    /**
     * Gets array of all "extractor" elements
     */
    public elkfed.main.xml.Extractor[] getExtractorArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EXTRACTOR$0, targetList);
            elkfed.main.xml.Extractor[] result = new elkfed.main.xml.Extractor[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "extractor" element
     */
    public elkfed.main.xml.Extractor getExtractorArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractor target = null;
            target = (elkfed.main.xml.Extractor)get_store().find_element_user(EXTRACTOR$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "extractor" element
     */
    public int sizeOfExtractorArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EXTRACTOR$0);
        }
    }
    
    /**
     * Sets array of all "extractor" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setExtractorArray(elkfed.main.xml.Extractor[] extractorArray)
    {
        check_orphaned();
        arraySetterHelper(extractorArray, EXTRACTOR$0);
    }
    
    /**
     * Sets ith "extractor" element
     */
    public void setExtractorArray(int i, elkfed.main.xml.Extractor extractor)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractor target = null;
            target = (elkfed.main.xml.Extractor)get_store().find_element_user(EXTRACTOR$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(extractor);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "extractor" element
     */
    public elkfed.main.xml.Extractor insertNewExtractor(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractor target = null;
            target = (elkfed.main.xml.Extractor)get_store().insert_element_user(EXTRACTOR$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "extractor" element
     */
    public elkfed.main.xml.Extractor addNewExtractor()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractor target = null;
            target = (elkfed.main.xml.Extractor)get_store().add_element_user(EXTRACTOR$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "extractor" element
     */
    public void removeExtractor(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EXTRACTOR$0, i);
        }
    }
}
