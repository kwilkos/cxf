package org.objectweb.celtix.resource;

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
     * @return an instance of the resource or <code>null</code> if the
     * resource cannot be resolved.
     */
    Object resolve(String resourceName, Class<?> resourceType);
}
