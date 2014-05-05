/*
 * XML Type:  Classifiers
 * Namespace: 
 * Java type: elkfed.main.xml.Classifiers
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml.impl;
/**
 * An XML Classifiers(@).
 *
 * This is a complex type.
 */
public class ClassifiersImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements elkfed.main.xml.Classifiers
{
    private static final long serialVersionUID = 1L;
    
    public ClassifiersImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CLASSIFIER$0 = 
        new javax.xml.namespace.QName("", "classifier");
    private static final javax.xml.namespace.QName RANKER$2 = 
        new javax.xml.namespace.QName("", "ranker");
    
    
    /**
     * Gets array of all "classifier" elements
     */
    public elkfed.main.xml.Classifier[] getClassifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(CLASSIFIER$0, targetList);
            elkfed.main.xml.Classifier[] result = new elkfed.main.xml.Classifier[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "classifier" element
     */
    public elkfed.main.xml.Classifier getClassifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifier target = null;
            target = (elkfed.main.xml.Classifier)get_store().find_element_user(CLASSIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "classifier" element
     */
    public int sizeOfClassifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CLASSIFIER$0);
        }
    }
    
    /**
     * Sets array of all "classifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setClassifierArray(elkfed.main.xml.Classifier[] classifierArray)
    {
        check_orphaned();
        arraySetterHelper(classifierArray, CLASSIFIER$0);
    }
    
    /**
     * Sets ith "classifier" element
     */
    public void setClassifierArray(int i, elkfed.main.xml.Classifier classifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifier target = null;
            target = (elkfed.main.xml.Classifier)get_store().find_element_user(CLASSIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(classifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "classifier" element
     */
    public elkfed.main.xml.Classifier insertNewClassifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Classifier target = null;
            target = (elkfed.main.xml.Classifier)get_store().insert_element_user(CLASSIFIER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "classifier" element
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
     * Removes the ith "classifier" element
     */
    public void removeClassifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CLASSIFIER$0, i);
        }
    }
    
    /**
     * Gets array of all "ranker" elements
     */
    public elkfed.main.xml.Ranker[] getRankerArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RANKER$2, targetList);
            elkfed.main.xml.Ranker[] result = new elkfed.main.xml.Ranker[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "ranker" element
     */
    public elkfed.main.xml.Ranker getRankerArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Ranker target = null;
            target = (elkfed.main.xml.Ranker)get_store().find_element_user(RANKER$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "ranker" element
     */
    public int sizeOfRankerArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RANKER$2);
        }
    }
    
    /**
     * Sets array of all "ranker" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRankerArray(elkfed.main.xml.Ranker[] rankerArray)
    {
        check_orphaned();
        arraySetterHelper(rankerArray, RANKER$2);
    }
    
    /**
     * Sets ith "ranker" element
     */
    public void setRankerArray(int i, elkfed.main.xml.Ranker ranker)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Ranker target = null;
            target = (elkfed.main.xml.Ranker)get_store().find_element_user(RANKER$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(ranker);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "ranker" element
     */
    public elkfed.main.xml.Ranker insertNewRanker(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Ranker target = null;
            target = (elkfed.main.xml.Ranker)get_store().insert_element_user(RANKER$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "ranker" element
     */
    public elkfed.main.xml.Ranker addNewRanker()
    {
        synchronized (monitor())
        {
            check_orphaned();
            elkfed.main.xml.Ranker target = null;
            target = (elkfed.main.xml.Ranker)get_store().add_element_user(RANKER$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "ranker" element
     */
    public void removeRanker(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RANKER$2, i);
        }
    }
}
