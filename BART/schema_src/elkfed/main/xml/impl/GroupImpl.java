/*
 * XML Type:  Group
 * Namespace: 
 * Java type: elkfed.main.xml.Group
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Group(@).
 *
 * This is a complex type.
 */
public class GroupImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Group
{
    private static final long serialVersionUID = 1L;
    
    public GroupImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PARAM$0 = 
        new javax.xml.namespace.QName("", "param");
    private static final javax.xml.namespace.QName GROUP$2 = 
        new javax.xml.namespace.QName("", "group");
    private static final javax.xml.namespace.QName RELCOUNT$4 = 
        new javax.xml.namespace.QName("", "relCount");
    private static final javax.xml.namespace.QName SINGLEVAL$6 = 
        new javax.xml.namespace.QName("", "singleVal");
    private static final javax.xml.namespace.QName NAME$8 = 
        new javax.xml.namespace.QName("", "name");
    
    
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
    
    /**
     * Gets array of all "relCount" elements
     */
    public elkfed.main.xml.RelCount[] getRelCountArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RELCOUNT$4, targetList);
            elkfed.main.xml.RelCount[] result = new elkfed.main.xml.RelCount[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "relCount" element
     */
    public elkfed.main.xml.RelCount getRelCountArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.RelCount target = null;
            target = (elkfed.main.xml.RelCount)get_store().find_element_user(RELCOUNT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "relCount" element
     */
    public int sizeOfRelCountArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RELCOUNT$4);
        }
    }
    
    /**
     * Sets array of all "relCount" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRelCountArray(elkfed.main.xml.RelCount[] relCountArray)
    {
        check_orphaned();
        arraySetterHelper(relCountArray, RELCOUNT$4);
    }
    
    /**
     * Sets ith "relCount" element
     */
    public void setRelCountArray(int i, elkfed.main.xml.RelCount relCount)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.RelCount target = null;
            target = (elkfed.main.xml.RelCount)get_store().find_element_user(RELCOUNT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(relCount);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relCount" element
     */
    public elkfed.main.xml.RelCount insertNewRelCount(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.RelCount target = null;
            target = (elkfed.main.xml.RelCount)get_store().insert_element_user(RELCOUNT$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relCount" element
     */
    public elkfed.main.xml.RelCount addNewRelCount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.RelCount target = null;
            target = (elkfed.main.xml.RelCount)get_store().add_element_user(RELCOUNT$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "relCount" element
     */
    public void removeRelCount(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RELCOUNT$4, i);
        }
    }
    
    /**
     * Gets array of all "singleVal" elements
     */
    public elkfed.main.xml.SingleVal[] getSingleValArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SINGLEVAL$6, targetList);
            elkfed.main.xml.SingleVal[] result = new elkfed.main.xml.SingleVal[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "singleVal" element
     */
    public elkfed.main.xml.SingleVal getSingleValArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.SingleVal target = null;
            target = (elkfed.main.xml.SingleVal)get_store().find_element_user(SINGLEVAL$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "singleVal" element
     */
    public int sizeOfSingleValArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SINGLEVAL$6);
        }
    }
    
    /**
     * Sets array of all "singleVal" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setSingleValArray(elkfed.main.xml.SingleVal[] singleValArray)
    {
        check_orphaned();
        arraySetterHelper(singleValArray, SINGLEVAL$6);
    }
    
    /**
     * Sets ith "singleVal" element
     */
    public void setSingleValArray(int i, elkfed.main.xml.SingleVal singleVal)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.SingleVal target = null;
            target = (elkfed.main.xml.SingleVal)get_store().find_element_user(SINGLEVAL$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(singleVal);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "singleVal" element
     */
    public elkfed.main.xml.SingleVal insertNewSingleVal(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.SingleVal target = null;
            target = (elkfed.main.xml.SingleVal)get_store().insert_element_user(SINGLEVAL$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "singleVal" element
     */
    public elkfed.main.xml.SingleVal addNewSingleVal()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.SingleVal target = null;
            target = (elkfed.main.xml.SingleVal)get_store().add_element_user(SINGLEVAL$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "singleVal" element
     */
    public void removeSingleVal(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SINGLEVAL$6, i);
        }
    }
    
    /**
     * Gets the "name" attribute
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$8);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "name" attribute
     */
    public org.apache.xmlbeans.XmlString xgetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$8);
            return target;
        }
    }
    
    /**
     * True if has "name" attribute
     */
    public boolean isSetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(NAME$8) != null;
        }
    }
    
    /**
     * Sets the "name" attribute
     */
    public void setName(java.lang.String name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$8);
            }
            target.setStringValue(name);
        }
    }
    
    /**
     * Sets (as xml) the "name" attribute
     */
    public void xsetName(org.apache.xmlbeans.XmlString name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(NAME$8);
            }
            target.set(name);
        }
    }
    
    /**
     * Unsets the "name" attribute
     */
    public void unsetName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(NAME$8);
        }
    }
}
