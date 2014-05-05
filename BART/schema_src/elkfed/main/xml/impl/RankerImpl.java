/*
 * XML Type:  Ranker
 * Namespace: 
 * Java type: elkfed.main.xml.Ranker
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Ranker(@).
 *
 * This is a complex type.
 */
public class RankerImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Ranker
{
    private static final long serialVersionUID = 1L;
    
    public RankerImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CLASSIFIER$0 = 
        new javax.xml.namespace.QName("", "classifier");
    private static final javax.xml.namespace.QName TYPE$2 = 
        new javax.xml.namespace.QName("", "type");
    private static final javax.xml.namespace.QName MODEL$4 = 
        new javax.xml.namespace.QName("", "model");
    private static final javax.xml.namespace.QName LEARNER$6 = 
        new javax.xml.namespace.QName("", "learner");
    private static final javax.xml.namespace.QName OPTIONS$8 = 
        new javax.xml.namespace.QName("", "options");
    
    
    /**
     * Gets the "classifier" element
     */
    public elkfed.main.xml.Classifier getClassifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifier target = null;
            target = (elkfed.main.xml.Classifier)get_store().find_element_user(CLASSIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "classifier" element
     */
    public boolean isSetClassifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CLASSIFIER$0) != 0;
        }
    }
    
    /**
     * Sets the "classifier" element
     */
    public void setClassifier(elkfed.main.xml.Classifier classifier)
    {
        generatedSetterHelperImpl(classifier, CLASSIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "classifier" element
     */
    public elkfed.main.xml.Classifier addNewClassifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifier target = null;
            target = (elkfed.main.xml.Classifier)get_store().add_element_user(CLASSIFIER$0);
            return target;
        }
    }
    
    /**
     * Unsets the "classifier" element
     */
    public void unsetClassifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CLASSIFIER$0, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$2);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$2);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TYPE$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TYPE$2);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(TYPE$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(TYPE$2);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODEL$4);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MODEL$4);
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
            return get_store().find_attribute_user(MODEL$4) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(MODEL$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(MODEL$4);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(MODEL$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(MODEL$4);
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
            get_store().remove_attribute(MODEL$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LEARNER$6);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(LEARNER$6);
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
            return get_store().find_attribute_user(LEARNER$6) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LEARNER$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LEARNER$6);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(LEARNER$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(LEARNER$6);
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
            get_store().remove_attribute(LEARNER$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(OPTIONS$8);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(OPTIONS$8);
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
            return get_store().find_attribute_user(OPTIONS$8) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(OPTIONS$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(OPTIONS$8);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(OPTIONS$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(OPTIONS$8);
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
            get_store().remove_attribute(OPTIONS$8);
        }
    }
}
