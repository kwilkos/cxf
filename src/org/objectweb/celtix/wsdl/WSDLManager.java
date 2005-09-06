package org.objectweb.celtix.wsdl;

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;

import org.w3c.dom.Element;

/**
 * WSDLManager
 *
 */
public interface WSDLManager {

    /**
     * Returns the ExtensionRegistry that the WSDLManager
     * uses when reading WSDL files.   Users can use
     * this to register their own extensors.
     * @return the ExtensionRegistry
     */
    ExtensionRegistry getExtenstionRegistry();
    
    /**
     * Returns the WSDLFactory that is used to read/write WSDL dedfinitions
     * @return the WSDLFactory
     */
    WSDLFactory getWSDLFactory();
    

    /**
     * Get the WSDL definition for the given URL.  Implementations
     * may return a copy from a local cache or load a new copy 
     * from the URL.
     * @param url - the location of the WSDL to load 
     * @return the wsdl definition
     */
    Definition getDefinition(URL url) throws WSDLException; 

    /**
     * Get the WSDL definition for the given URL.  Implementations
     * may return a copy from a local cache or load a new copy 
     * from the URL.
     * @param url - the location of the WSDL to load 
     * @return the wsdl definition
     */
    Definition getDefinition(String url) throws WSDLException;
    
    /**
     * Get the WSDL definition for the given Element.  Implementations
     * may return a copy from a local cache or load a new copy 
     * from the Element.
     * @param element - the root element of the wsdl 
     * @return the wsdl definition
     */
    Definition getDefinition(Element element) throws WSDLException;  
    
    /**
     * Get the WSDL definition for the given class.  Implementations
     * may return a copy from a local cache or load a new copy 
     * from the class.
     * @param class - the class annotated with a WebService annotation 
     * @return the wsdl definition
     */
    Definition getDefinition(Class sei) throws WSDLException;
}
