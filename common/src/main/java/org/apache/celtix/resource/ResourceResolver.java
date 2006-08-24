package org.apache.cxf.resource;

import java.io.InputStream;

/**
 * Resolves resource.  A ResourceResolver is used to find references
 * to resources that are being injected into classes
 *
 */
public interface ResourceResolver {

    
    /**
     * Resolve a resource given its name and type.
     *
     * @param resourceName name of the resource to resolve.
     * @param resourceType type of the resource to resolve.
     * @return an instance of the resource or <code>null</code> if the
     * resource cannot be resolved.
     */
    <T> T resolve(String resourceName, Class<T> resourceType);


    /**
     * Resolve a resource given its name and return an InputStream to it.
     *
     * @param resourceName name of the resource to resolve.
     * @return an InputStream for the resource or null if it could not be found.
     */
    InputStream getAsStream(String name);
}
