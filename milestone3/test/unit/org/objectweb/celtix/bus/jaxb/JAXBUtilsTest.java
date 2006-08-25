package org.objectweb.celtix.bus.jaxb;


import junit.framework.TestCase;

public class JAXBUtilsTest extends TestCase {
    
    public void testBuiltInTypeToJavaType() {
        assertEquals("boolean", JAXBUtils.builtInTypeToJavaType("boolean"));
        assertEquals("javax.xml.datatype.XMLGregorianCalendar", JAXBUtils.builtInTypeToJavaType("gYear"));
        assertNull(JAXBUtils.builtInTypeToJavaType("other"));
    }

    public void testPackageNames() {
        assertEquals("org.objectweb.celtix.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/configuration/types"));
        assertEquals("org.objectweb.celtix.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/configuration/types.xsd"));
        assertEquals("org.objectweb.celtix.config_types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/config-types"));
        assertEquals("org.objectweb.celtix._default.types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/default/types"));
        assertEquals("org.objectweb.celtix.config._4types",
                     JAXBUtils.namespaceURIToPackage("http://celtix.objectweb.org/config/4types."));
        assertEquals("com.iona.configuration.types",
                     JAXBUtils.namespaceURIToPackage("http://www.iona.com/configuration/types"));
        assertEquals("org.objectweb.celtix.config.types",
                     JAXBUtils.namespaceURIToPackage("urn://celtix-objectweb-org/config/types"));
        assertEquals("types", JAXBUtils.namespaceURIToPackage("types"));
    } 
    
    public void testNameToIdentifier() {
        assertEquals("_return", 
                     JAXBUtils.nameToIdentifier("return", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("getReturn", 
                     JAXBUtils.nameToIdentifier("return", JAXBUtils.IdentifierType.GETTER));
        assertEquals("setReturn", 
                     JAXBUtils.nameToIdentifier("return", JAXBUtils.IdentifierType.SETTER));
        

        assertEquals("_public", 
                     JAXBUtils.nameToIdentifier("public", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("getPublic", 
                     JAXBUtils.nameToIdentifier("public", JAXBUtils.IdentifierType.GETTER));
        assertEquals("setPublic", 
                     JAXBUtils.nameToIdentifier("public", JAXBUtils.IdentifierType.SETTER));

        assertEquals("arg0", 
                     JAXBUtils.nameToIdentifier("arg0", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("getArg0", 
                     JAXBUtils.nameToIdentifier("arg0", JAXBUtils.IdentifierType.GETTER));
        assertEquals("setArg0", 
                     JAXBUtils.nameToIdentifier("arg0", JAXBUtils.IdentifierType.SETTER));
        
        assertEquals("mixedCaseName", 
                     JAXBUtils.nameToIdentifier("mixedCaseName", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("MixedCaseName", 
                     JAXBUtils.nameToIdentifier("mixedCaseName", JAXBUtils.IdentifierType.CLASS));
        assertEquals("setMixedCaseName", 
                     JAXBUtils.nameToIdentifier("mixedCaseName", JAXBUtils.IdentifierType.SETTER));
        assertEquals("MIXED_CASE_NAME", 
                     JAXBUtils.nameToIdentifier("mixedCaseName", JAXBUtils.IdentifierType.CONSTANT));
        
        assertEquals("answer42", 
                     JAXBUtils.nameToIdentifier("Answer42", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("Answer42", 
                     JAXBUtils.nameToIdentifier("Answer42", JAXBUtils.IdentifierType.CLASS)); 
        assertEquals("getAnswer42", 
                     JAXBUtils.nameToIdentifier("Answer42", JAXBUtils.IdentifierType.GETTER));
        assertEquals("ANSWER_42", 
                     JAXBUtils.nameToIdentifier("Answer42", JAXBUtils.IdentifierType.CONSTANT));
        
        assertEquals("nameWithDashes", 
                     JAXBUtils.nameToIdentifier("name-with-dashes", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("NameWithDashes", 
                     JAXBUtils.nameToIdentifier("name-with-dashes", JAXBUtils.IdentifierType.CLASS));
        assertEquals("setNameWithDashes", 
                     JAXBUtils.nameToIdentifier("name-with-dashes", JAXBUtils.IdentifierType.SETTER));
        assertEquals("NAME_WITH_DASHES", 
                     JAXBUtils.nameToIdentifier("name-with-dashes", JAXBUtils.IdentifierType.CONSTANT));
        
        assertEquals("otherPunctChars", 
                     JAXBUtils.nameToIdentifier("other_punct-chars", JAXBUtils.IdentifierType.VARIABLE));
        assertEquals("OtherPunctChars", 
                     JAXBUtils.nameToIdentifier("other_punct-chars", JAXBUtils.IdentifierType.CLASS));
        assertEquals("getOtherPunctChars", 
                     JAXBUtils.nameToIdentifier("other_punct-chars", JAXBUtils.IdentifierType.GETTER));
        assertEquals("OTHER_PUNCT_CHARS", 
                     JAXBUtils.nameToIdentifier("other_punct-chars", JAXBUtils.IdentifierType.CONSTANT));
    }
}
