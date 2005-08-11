package org.objectweb.celtix.wsdl;

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;

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
}
