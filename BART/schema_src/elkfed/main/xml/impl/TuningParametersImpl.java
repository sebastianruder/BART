/*
 * XML Type:  TuningParameters
 * Namespace: 
 * Java type: elkfed.main.xml.TuningParameters
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML TuningParameters(@).
 *
 * This is a complex type.
 */
public class TuningParametersImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.TuningParameters
{
    private static final long serialVersionUID = 1L;
    
    public TuningParametersImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARAMETER$0 = 
        new javax.xml.namespace.QName("", "parameter");
    
    
    /**
     * Gets array of all "parameter" elements
     */
    public elkfed.main.xml.TuningParameter[] getParameterArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PARAMETER$0, targetList);
            elkfed.main.xml.TuningParameter[] result = new elkfed.main.xml.TuningParameter[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "parameter" element
     */
    public elkfed.main.xml.TuningParameter getParameterArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameter target = null;
            target = (elkfed.main.xml.TuningParameter)get_store().find_element_user(PARAMETER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "parameter" element
     */
    public int sizeOfParameterArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PARAMETER$0);
        }
    }
    
    /**
     * Sets array of all "parameter" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setParameterArray(elkfed.main.xml.TuningParameter[] parameterArray)
    {
        check_orphaned();
        arraySetterHelper(parameterArray, PARAMETER$0);
    }
    
    /**
     * Sets ith "parameter" element
     */
    public void setParameterArray(int i, elkfed.main.xml.TuningParameter parameter)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameter target = null;
            target = (elkfed.main.xml.TuningParameter)get_store().find_element_user(PARAMETER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(parameter);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "parameter" element
     */
    public elkfed.main.xml.TuningParameter insertNewParameter(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameter target = null;
            target = (elkfed.main.xml.TuningParameter)get_store().insert_element_user(PARAMETER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "parameter" element
     */
    public elkfed.main.xml.TuningParameter addNewParameter()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameter target = null;
            target = (elkfed.main.xml.TuningParameter)get_store().add_element_user(PARAMETER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "parameter" element
     */
    public void removeParameter(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PARAMETER$0, i);
        }
    }
}
