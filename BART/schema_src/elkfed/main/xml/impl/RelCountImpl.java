/*
 * XML Type:  RelCount
 * Namespace: 
 * Java type: elkfed.main.xml.RelCount
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML RelCount(@).
 *
 * This is a complex type.
 */
public class RelCountImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.RelCount
{
    private static final long serialVersionUID = 1L;
    
    public RelCountImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName NAME$0 = 
        new javax.xml.namespace.QName("", "name");
    private static final javax.xml.namespace.QName TP$2 = 
        new javax.xml.namespace.QName("", "tp");
    private static final javax.xml.namespace.QName FP$4 = 
        new javax.xml.namespace.QName("", "fp");
    private static final javax.xml.namespace.QName FN$6 = 
        new javax.xml.namespace.QName("", "fn");
    
    
    /**
     * Gets the "name" attribute
     */
    public java.lang.String getName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$0);
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
            return get_store().find_attribute_user(NAME$0) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(NAME$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(NAME$0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(NAME$0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(NAME$0);
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
            get_store().remove_attribute(NAME$0);
        }
    }
    
    /**
     * Gets the "tp" attribute
     */
    public int getTp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TP$2);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "tp" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetTp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(TP$2);
            return target;
        }
    }
    
    /**
     * True if has "tp" attribute
     */
    public boolean isSetTp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(TP$2) != null;
        }
    }
    
    /**
     * Sets the "tp" attribute
     */
    public void setTp(int tp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(TP$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(TP$2);
            }
            target.setIntValue(tp);
        }
    }
    
    /**
     * Sets (as xml) the "tp" attribute
     */
    public void xsetTp(org.apache.xmlbeans.XmlInt tp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(TP$2);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(TP$2);
            }
            target.set(tp);
        }
    }
    
    /**
     * Unsets the "tp" attribute
     */
    public void unsetTp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(TP$2);
        }
    }
    
    /**
     * Gets the "fp" attribute
     */
    public int getFp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FP$4);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "fp" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetFp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(FP$4);
            return target;
        }
    }
    
    /**
     * True if has "fp" attribute
     */
    public boolean isSetFp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(FP$4) != null;
        }
    }
    
    /**
     * Sets the "fp" attribute
     */
    public void setFp(int fp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FP$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(FP$4);
            }
            target.setIntValue(fp);
        }
    }
    
    /**
     * Sets (as xml) the "fp" attribute
     */
    public void xsetFp(org.apache.xmlbeans.XmlInt fp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(FP$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(FP$4);
            }
            target.set(fp);
        }
    }
    
    /**
     * Unsets the "fp" attribute
     */
    public void unsetFp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(FP$4);
        }
    }
    
    /**
     * Gets the "fn" attribute
     */
    public int getFn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FN$6);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "fn" attribute
     */
    public org.apache.xmlbeans.XmlInt xgetFn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(FN$6);
            return target;
        }
    }
    
    /**
     * True if has "fn" attribute
     */
    public boolean isSetFn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(FN$6) != null;
        }
    }
    
    /**
     * Sets the "fn" attribute
     */
    public void setFn(int fn)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(FN$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(FN$6);
            }
            target.setIntValue(fn);
        }
    }
    
    /**
     * Sets (as xml) the "fn" attribute
     */
    public void xsetFn(org.apache.xmlbeans.XmlInt fn)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_attribute_user(FN$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_attribute_user(FN$6);
            }
            target.set(fn);
        }
    }
    
    /**
     * Unsets the "fn" attribute
     */
    public void unsetFn()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(FN$6);
        }
    }
}
