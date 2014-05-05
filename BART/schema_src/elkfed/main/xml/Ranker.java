/*
 * XML Type:  Ranker
 * Namespace: 
 * Java type: elkfed.main.xml.Ranker
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml;


/**
 * An XML Ranker(@).
 *
 * This is a complex type.
 */
public interface Ranker extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Ranker.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD693993C1BE956671373FC1AB4FB0C33").resolveHandle("rankerc1a2type");
    
    /**
     * Gets the "classifier" element
     */
    elkfed.main.xml.Classifier getClassifier();
    
    /**
     * True if has "classifier" element
     */
    boolean isSetClassifier();
    
    /**
     * Sets the "classifier" element
     */
    void setClassifier(elkfed.main.xml.Classifier classifier);
    
    /**
     * Appends and returns a new empty "classifier" element
     */
    elkfed.main.xml.Classifier addNewClassifier();
    
    /**
     * Unsets the "classifier" element
     */
    void unsetClassifier();
    
    /**
     * Gets the "type" attribute
     */
    java.lang.String getType();
    
    /**
     * Gets (as xml) the "type" attribute
     */
    org.apache.xmlbeans.XmlString xgetType();
    
    /**
     * Sets the "type" attribute
     */
    void setType(java.lang.String type);
    
    /**
     * Sets (as xml) the "type" attribute
     */
    void xsetType(org.apache.xmlbeans.XmlString type);
    
    /**
     * Gets the "model" attribute
     */
    java.lang.String getModel();
    
    /**
     * Gets (as xml) the "model" attribute
     */
    org.apache.xmlbeans.XmlString xgetModel();
    
    /**
     * True if has "model" attribute
     */
    boolean isSetModel();
    
    /**
     * Sets the "model" attribute
     */
    void setModel(java.lang.String model);
    
    /**
     * Sets (as xml) the "model" attribute
     */
    void xsetModel(org.apache.xmlbeans.XmlString model);
    
    /**
     * Unsets the "model" attribute
     */
    void unsetModel();
    
    /**
     * Gets the "learner" attribute
     */
    java.lang.String getLearner();
    
    /**
     * Gets (as xml) the "learner" attribute
     */
    org.apache.xmlbeans.XmlString xgetLearner();
    
    /**
     * True if has "learner" attribute
     */
    boolean isSetLearner();
    
    /**
     * Sets the "learner" attribute
     */
    void setLearner(java.lang.String learner);
    
    /**
     * Sets (as xml) the "learner" attribute
     */
    void xsetLearner(org.apache.xmlbeans.XmlString learner);
    
    /**
     * Unsets the "learner" attribute
     */
    void unsetLearner();
    
    /**
     * Gets the "options" attribute
     */
    java.lang.String getOptions();
    
    /**
     * Gets (as xml) the "options" attribute
     */
    org.apache.xmlbeans.XmlString xgetOptions();
    
    /**
     * True if has "options" attribute
     */
    boolean isSetOptions();
    
    /**
     * Sets the "options" attribute
     */
    void setOptions(java.lang.String options);
    
    /**
     * Sets (as xml) the "options" attribute
     */
    void xsetOptions(org.apache.xmlbeans.XmlString options);
    
    /**
     * Unsets the "options" attribute
     */
    void unsetOptions();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static elkfed.main.xml.Ranker newInstance() {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static elkfed.main.xml.Ranker newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static elkfed.main.xml.Ranker parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static elkfed.main.xml.Ranker parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static elkfed.main.xml.Ranker parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static elkfed.main.xml.Ranker parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static elkfed.main.xml.Ranker parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static elkfed.main.xml.Ranker parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static elkfed.main.xml.Ranker parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static elkfed.main.xml.Ranker parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.Ranker parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.Ranker parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.Ranker) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
