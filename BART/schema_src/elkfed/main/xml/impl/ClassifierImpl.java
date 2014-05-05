/*
 * XML Type:  Classifier
 * Namespace: 
 * Java type: elkfed.main.xml.Classifier
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Classifier(@).
 *
 * This is a complex type.
 */
public class ClassifierImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Classifier
{
    private static final long serialVersionUID = 1L;
    
    public ClassifierImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TYPE$0 = 
        new javax.xml.namespace.QName("", "type");
    private static final javax.xml.namespace.QName MODEL$2 = 
        new javax.xml.namespace.QName("", "model");
    private static final javax.xml.namespace.QName LEARNER$4 = 
        new javax.xml.namespace.QName("", "learner");
    private static final javax.xml.namespace.QName OPTIONS$6 = 
        new javax.xml.namespace.QName("", "options");
    
    
    /**
     * Gets the "type" attribute
     */
    public java.lang.String getType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$0);
            return target;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TYPE$0);
            }
            target.set(type);
        }
    }
    
    /**
     * Gets the "model" attribute
     */
    public java.lang.String getModel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODEL$2);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "model" attribute
     */
    public org.apache.xmlbeans.XmlString xgetModel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MODEL$2);
            return target;
        }
    }
    
    /**
     * True if has "model" attribute
     */
    public boolean isSetModel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(MODEL$2) != null;
        }
    }
    
    /**
     * Sets the "model" attribute
     */
    public void setModel(java.lang.String model)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODEL$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MODEL$2);
            }
            target.setStringValue(model);
        }
    }
    
    /**
     * Sets (as xml) the "model" attribute
     */
    public void xsetModel(org.apache.xmlbeans.XmlString model)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MODEL$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(MODEL$2);
            }
            target.set(model);
        }
    }
    
    /**
     * Unsets the "model" attribute
     */
    public void unsetModel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(MODEL$2);
        }
    }
    
    /**
     * Gets the "learner" attribute
     */
    public java.lang.String getLearner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LEARNER$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "learner" attribute
     */
    public org.apache.xmlbeans.XmlString xgetLearner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(LEARNER$4);
            return target;
        }
    }
    
    /**
     * True if has "learner" attribute
     */
    public boolean isSetLearner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LEARNER$4) != null;
        }
    }
    
    /**
     * Sets the "learner" attribute
     */
    public void setLearner(java.lang.String learner)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LEARNER$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LEARNER$4);
            }
            target.setStringValue(learner);
        }
    }
    
    /**
     * Sets (as xml) the "learner" attribute
     */
    public void xsetLearner(org.apache.xmlbeans.XmlString learner)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(LEARNER$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(LEARNER$4);
            }
            target.set(learner);
        }
    }
    
    /**
     * Unsets the "learner" attribute
     */
    public void unsetLearner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LEARNER$4);
        }
    }
    
    /**
     * Gets the "options" attribute
     */
    public java.lang.String getOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(OPTIONS$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "options" attribute
     */
    public org.apache.xmlbeans.XmlString xgetOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(OPTIONS$6);
            return target;
        }
    }
    
    /**
     * True if has "options" attribute
     */
    public boolean isSetOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(OPTIONS$6) != null;
        }
    }
    
    /**
     * Sets the "options" attribute
     */
    public void setOptions(java.lang.String options)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(OPTIONS$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(OPTIONS$6);
            }
            target.setStringValue(options);
        }
    }
    
    /**
     * Sets (as xml) the "options" attribute
     */
    public void xsetOptions(org.apache.xmlbeans.XmlString options)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(OPTIONS$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(OPTIONS$6);
            }
            target.set(options);
        }
    }
    
    /**
     * Unsets the "options" attribute
     */
    public void unsetOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(OPTIONS$6);
        }
    }
}
