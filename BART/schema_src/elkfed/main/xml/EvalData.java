/*
 * XML Type:  EvalData
 * Namespace: 
 * Java type: elkfed.main.xml.EvalData
 *
 * Automatically generated - do not modify.
 */
package elkfed.main.xml;


/**
 * An XML EvalData(@).
 *
 * This is a complex type.
 */
public interface EvalData extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EvalData.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sD693993C1BE956671373FC1AB4FB0C33").resolveHandle("evaldata5beftype");
    
    /**
     * Gets array of all "param" elements
     */
    elkfed.main.xml.Parameter[] getParamArray();
    
    /**
     * Gets ith "param" element
     */
    elkfed.main.xml.Parameter getParamArray(int i);
    
    /**
     * Returns number of "param" element
     */
    int sizeOfParamArray();
    
    /**
     * Sets array of all "param" element
     */
    void setParamArray(elkfed.main.xml.Parameter[] paramArray);
    
    /**
     * Sets ith "param" element
     */
    void setParamArray(int i, elkfed.main.xml.Parameter param);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "param" element
     */
    elkfed.main.xml.Parameter insertNewParam(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "param" element
     */
    elkfed.main.xml.Parameter addNewParam();
    
    /**
     * Removes the ith "param" element
     */
    void removeParam(int i);
    
    /**
     * Gets array of all "group" elements
     */
    elkfed.main.xml.Group[] getGroupArray();
    
    /**
     * Gets ith "group" element
     */
    elkfed.main.xml.Group getGroupArray(int i);
    
    /**
     * Returns number of "group" element
     */
    int sizeOfGroupArray();
    
    /**
     * Sets array of all "group" element
     */
    void setGroupArray(elkfed.main.xml.Group[] groupArray);
    
    /**
     * Sets ith "group" element
     */
    void setGroupArray(int i, elkfed.main.xml.Group group);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "group" element
     */
    elkfed.main.xml.Group insertNewGroup(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "group" element
     */
    elkfed.main.xml.Group addNewGroup();
    
    /**
     * Removes the ith "group" element
     */
    void removeGroup(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static elkfed.main.xml.EvalData newInstance() {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static elkfed.main.xml.EvalData newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static elkfed.main.xml.EvalData parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static elkfed.main.xml.EvalData parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static elkfed.main.xml.EvalData parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static elkfed.main.xml.EvalData parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static elkfed.main.xml.EvalData parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static elkfed.main.xml.EvalData parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static elkfed.main.xml.EvalData parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static elkfed.main.xml.EvalData parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.EvalData parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static elkfed.main.xml.EvalData parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (elkfed.main.xml.EvalData) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
