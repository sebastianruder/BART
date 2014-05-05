/*
 * XML Type:  EvalData
 * Namespace: 
 * Java type: elkfed.main.xml.EvalData
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML EvalData(@).
 *
 * This is a complex type.
 */
public class EvalDataImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.EvalData
{
    private static final long serialVersionUID = 1L;
    
    public EvalDataImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARAM$0 = 
        new javax.xml.namespace.QName("", "param");
    private static final javax.xml.namespace.QName GROUP$2 = 
        new javax.xml.namespace.QName("", "group");
    
    
    /**
     * Gets array of all "param" elements
     */
    public elkfed.main.xml.Parameter[] getParamArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PARAM$0, targetList);
            elkfed.main.xml.Parameter[] result = new elkfed.main.xml.Parameter[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "param" element
     */
    public elkfed.main.xml.Parameter getParamArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Parameter target = null;
            target = (elkfed.main.xml.Parameter)get_store().find_element_user(PARAM$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "param" element
     */
    public int sizeOfParamArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PARAM$0);
        }
    }
    
    /**
     * Sets array of all "param" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setParamArray(elkfed.main.xml.Parameter[] paramArray)
    {
        check_orphaned();
        arraySetterHelper(paramArray, PARAM$0);
    }
    
    /**
     * Sets ith "param" element
     */
    public void setParamArray(int i, elkfed.main.xml.Parameter param)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Parameter target = null;
            target = (elkfed.main.xml.Parameter)get_store().find_element_user(PARAM$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(param);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "param" element
     */
    public elkfed.main.xml.Parameter insertNewParam(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Parameter target = null;
            target = (elkfed.main.xml.Parameter)get_store().insert_element_user(PARAM$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "param" element
     */
    public elkfed.main.xml.Parameter addNewParam()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Parameter target = null;
            target = (elkfed.main.xml.Parameter)get_store().add_element_user(PARAM$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "param" element
     */
    public void removeParam(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PARAM$0, i);
        }
    }
    
    /**
     * Gets array of all "group" elements
     */
    public elkfed.main.xml.Group[] getGroupArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(GROUP$2, targetList);
            elkfed.main.xml.Group[] result = new elkfed.main.xml.Group[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "group" element
     */
    public elkfed.main.xml.Group getGroupArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Group target = null;
            target = (elkfed.main.xml.Group)get_store().find_element_user(GROUP$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "group" element
     */
    public int sizeOfGroupArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(GROUP$2);
        }
    }
    
    /**
     * Sets array of all "group" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setGroupArray(elkfed.main.xml.Group[] groupArray)
    {
        check_orphaned();
        arraySetterHelper(groupArray, GROUP$2);
    }
    
    /**
     * Sets ith "group" element
     */
    public void setGroupArray(int i, elkfed.main.xml.Group group)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Group target = null;
            target = (elkfed.main.xml.Group)get_store().find_element_user(GROUP$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(group);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "group" element
     */
    public elkfed.main.xml.Group insertNewGroup(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Group target = null;
            target = (elkfed.main.xml.Group)get_store().insert_element_user(GROUP$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "group" element
     */
    public elkfed.main.xml.Group addNewGroup()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Group target = null;
            target = (elkfed.main.xml.Group)get_store().add_element_user(GROUP$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "group" element
     */
    public void removeGroup(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(GROUP$2, i);
        }
    }
}
