/*
 * XML Type:  System
 * Namespace: 
 * Java type: elkfed.main.xml.System
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML System(@).
 *
 * This is a complex type.
 */
public class SystemImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.System
{
    private static final long serialVersionUID = 1L;
    
    public SystemImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CLASSIFIERS$0 = 
        new javax.xml.namespace.QName("", "classifiers");
    private static final javax.xml.namespace.QName EXTRACTORS$2 = 
        new javax.xml.namespace.QName("", "extractors");
    private static final javax.xml.namespace.QName TUNINGPARAMETERS$4 = 
        new javax.xml.namespace.QName("", "tuning-parameters");
    private static final javax.xml.namespace.QName TYPE$6 = 
        new javax.xml.namespace.QName("", "type");
    
    
    /**
     * Gets the "classifiers" element
     */
    public elkfed.main.xml.Classifiers getClassifiers()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifiers target = null;
            target = (elkfed.main.xml.Classifiers)get_store().find_element_user(CLASSIFIERS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "classifiers" element
     */
    public void setClassifiers(elkfed.main.xml.Classifiers classifiers)
    {
        generatedSetterHelperImpl(classifiers, CLASSIFIERS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "classifiers" element
     */
    public elkfed.main.xml.Classifiers addNewClassifiers()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifiers target = null;
            target = (elkfed.main.xml.Classifiers)get_store().add_element_user(CLASSIFIERS$0);
            return target;
        }
    }
    
    /**
     * Gets array of all "extractors" elements
     */
    public elkfed.main.xml.Extractors[] getExtractorsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EXTRACTORS$2, targetList);
            elkfed.main.xml.Extractors[] result = new elkfed.main.xml.Extractors[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "extractors" element
     */
    public elkfed.main.xml.Extractors getExtractorsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractors target = null;
            target = (elkfed.main.xml.Extractors)get_store().find_element_user(EXTRACTORS$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "extractors" element
     */
    public int sizeOfExtractorsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EXTRACTORS$2);
        }
    }
    
    /**
     * Sets array of all "extractors" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setExtractorsArray(elkfed.main.xml.Extractors[] extractorsArray)
    {
        check_orphaned();
        arraySetterHelper(extractorsArray, EXTRACTORS$2);
    }
    
    /**
     * Sets ith "extractors" element
     */
    public void setExtractorsArray(int i, elkfed.main.xml.Extractors extractors)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractors target = null;
            target = (elkfed.main.xml.Extractors)get_store().find_element_user(EXTRACTORS$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(extractors);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "extractors" element
     */
    public elkfed.main.xml.Extractors insertNewExtractors(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractors target = null;
            target = (elkfed.main.xml.Extractors)get_store().insert_element_user(EXTRACTORS$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "extractors" element
     */
    public elkfed.main.xml.Extractors addNewExtractors()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Extractors target = null;
            target = (elkfed.main.xml.Extractors)get_store().add_element_user(EXTRACTORS$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "extractors" element
     */
    public void removeExtractors(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EXTRACTORS$2, i);
        }
    }
    
    /**
     * Gets the "tuning-parameters" element
     */
    public elkfed.main.xml.TuningParameters getTuningParameters()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameters target = null;
            target = (elkfed.main.xml.TuningParameters)get_store().find_element_user(TUNINGPARAMETERS$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "tuning-parameters" element
     */
    public boolean isSetTuningParameters()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TUNINGPARAMETERS$4) != 0;
        }
    }
    
    /**
     * Sets the "tuning-parameters" element
     */
    public void setTuningParameters(elkfed.main.xml.TuningParameters tuningParameters)
    {
        generatedSetterHelperImpl(tuningParameters, TUNINGPARAMETERS$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "tuning-parameters" element
     */
    public elkfed.main.xml.TuningParameters addNewTuningParameters()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.TuningParameters target = null;
            target = (elkfed.main.xml.TuningParameters)get_store().add_element_user(TUNINGPARAMETERS$4);
            return target;
        }
    }
    
    /**
     * Unsets the "tuning-parameters" element
     */
    public void unsetTuningParameters()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TUNINGPARAMETERS$4, 0);
        }
    }
    
    /**
     * Gets the "type" attribute
     */
    public java.lang.String getType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "type" attribute
     */
    public org.apache.xmlbeans.XmlString xgetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$6);
            return target;
        }
    }
    
    /**
     * True if has "type" attribute
     */
    public boolean isSetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TYPE$6) != null;
        }
    }
    
    /**
     * Sets the "type" attribute
     */
    public void setType(java.lang.String type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$6);
            }
            target.setStringValue(type);
        }
    }
    
    /**
     * Sets (as xml) the "type" attribute
     */
    public void xsetType(org.apache.xmlbeans.XmlString type)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TYPE$6);
            }
            target.set(type);
        }
    }
    
    /**
     * Unsets the "type" attribute
     */
    public void unsetType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TYPE$6);
        }
    }
}
