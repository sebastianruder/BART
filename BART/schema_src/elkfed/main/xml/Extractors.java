/*
 * XML Type:  Extractors
 * Namespace: 
 * Java type: elkfed.main.xml.Extractors
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml;


/**
 * An XML Extractors(@).
 *
 * This is a complex type.
 */
public interface Extractors extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Extractors.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD693993C1BE956671373FC1AB4FB0C33").resolveHandle("extractorsae18type");
    
    /**
     * Gets array of all "extractor" elements
     */
    elkfed.main.xml.Extractor[] getExtractorArray();
    
    /**
     * Gets ith "extractor" element
     */
    elkfed.main.xml.Extractor getExtractorArray(int i);
    
    /**
     * Returns number of "extractor" element
     */
    int sizeOfExtractorArray();
    
    /**
     * Sets array of all "extractor" element
     */
    void setExtractorArray(elkfed.main.xml.Extractor[] extractorArray);
    
    /**
     * Sets ith "extractor" element
     */
    void setExtractorArray(int i, elkfed.main.xml.Extractor extractor);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "extractor" element
     */
    elkfed.main.xml.Extractor insertNewExtractor(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "extractor" element
     */
    elkfed.main.xml.Extractor addNewExtractor();
    
    /**
     * Removes the ith "extractor" element
     */
    void removeExtractor(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static elkfed.main.xml.Extractors newInstance() {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static elkfed.main.xml.Extractors newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static elkfed.main.xml.Extractors parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static elkfed.main.xml.Extractors parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static elkfed.main.xml.Extractors parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static elkfed.main.xml.Extractors parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static elkfed.main.xml.Extractors parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static elkfed.main.xml.Extractors parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static elkfed.main.xml.Extractors parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static elkfed.main.xml.Extractors parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.Extractors parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.Extractors parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.Extractors) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
