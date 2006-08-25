package org.objectweb.celtix.bus.configuration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.common.util.PackageUtils;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.configuration.types.ExtensionType;
import org.objectweb.celtix.configuration.types.ExtensionTypeList;
import org.springframework.core.io.UrlResource;

/**
 * Utility class to support use of data types defined in std-types.xsd
 *
 */
public final class StandardTypesHelper {
    
    private static final Logger LOG = LogUtils.getL7dLogger(StandardTypesHelper.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    /**
     * prevents instantiation
     */
    private StandardTypesHelper() {
    }
    
    /**
     * Checks if a factory supports a namespace.
     * 
     * @param mapping the data structure containing the class name of the
     *            factory and its list of supported namespaces.
     * @param namespace the namespace.
     * @return true if the namespace is supported.
     */
    public static boolean supportsNamespace(ClassNamespaceMappingType mapping, String namespace) {
        for (String ns : mapping.getNamespace()) {
            if (ns.equals(namespace)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Parses an xml fragment specifying a list of class to
     * namespace mappings.
     * 
     * @param is the input stream.
     * @return the JAXB representation of the mappings list.
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static List<ClassNamespaceMappingType> parseClassNamespaceMappingsFragment(
        InputStream is, ClassLoader cl) throws JAXBException {
        JAXBContext context = null;
        Object obj = null;

        String packageName = PackageUtils.getPackageName(
            org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType.class);
            

        context = JAXBContext.newInstance(packageName, cl);
        Unmarshaller u = context.createUnmarshaller();

        obj = u.unmarshal(is);

        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>)obj;
            obj = el.getValue();
        }
        if (null != obj) {
            return ((ClassNamespaceMappingListType)obj).getMap();
        }
        return null;
    }
    
    /**
     * Returns a list of transport factory (classnames) to namespace
     * mappings based on all resources found on the classpath.
     * 
     * @param resourceName the name of the resource.
     */
    public static List<ClassNamespaceMappingType> getFactoryNamespaceMappings(
        String resourceName, ClassLoader cl) {
        Enumeration<URL> candidates;
        List<ClassNamespaceMappingType> factoryNamespaceMappings = new ArrayList<ClassNamespaceMappingType>();
        try {
            candidates = cl.getResources(resourceName);
            while (candidates.hasMoreElements()) {
                URL url = candidates.nextElement();
                List<ClassNamespaceMappingType> mappings = null;
                try {
                    UrlResource ur = new UrlResource(url);
                    InputStream is = ur.getInputStream();
                    mappings = parseClassNamespaceMappingsFragment(is, cl);
                } catch (JAXBException ex) {
                    Message msg = new Message("LOAD_RESOURCE_EXC", BUNDLE, url);
                    LOG.log(Level.WARNING, msg.toString(), ex);
                } catch (IOException ex) {
                    Message msg = new Message("LOAD_RESOURCE_EXC", BUNDLE, url);
                    LOG.log(Level.WARNING, msg.toString(), ex);
                }
                if (null != mappings) {
                    factoryNamespaceMappings.addAll(mappings);
                }
            }
        } catch (IOException ex) {
            Message msg = new Message("GET_RESOURCES_EXC", BUNDLE, resourceName);
            LOG.log(Level.WARNING, msg.toString(), ex);
        }
        return factoryNamespaceMappings;
    }
    
    /**
     * Parses an xml fragment specifying a list of extensions elements.
     * 
     * @param is the input stream.
     * @return the JAXB representation of the extensions list.
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static List<ExtensionType> parseExtensionsFragment(InputStream is, ClassLoader cl) 
        throws JAXBException {
        JAXBContext context = null;
        Object obj = null;

        String packageName = PackageUtils.getPackageName(ExtensionTypeList.class);

        context = JAXBContext.newInstance(packageName, cl);
        Unmarshaller u = context.createUnmarshaller();

        obj = u.unmarshal(is);

        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>)obj;
            obj = el.getValue();
        }
        if (null != obj) {
            return ((ExtensionTypeList)obj).getExtension();
        }
        return null;
    }
    
    /**
     * Returns a list of extensions (parent class name - element type class name pairs)
     * based on all resources found on the classpath.
     * 
     * @param resourceName the name of the resource.
     */
    public static List<ExtensionType> getExtensions(
        String resourceName, ClassLoader cl) {
        Enumeration<URL> candidates;
        List<ExtensionType> extensions = new ArrayList<ExtensionType>();
        try {
            candidates = cl.getResources(resourceName);
            while (candidates.hasMoreElements()) {
                URL url = candidates.nextElement();
                List<ExtensionType> candidateExtensions = null;
                try {
                    UrlResource ur = new UrlResource(url);
                    InputStream is = ur.getInputStream();
                    candidateExtensions = parseExtensionsFragment(is, cl);
                } catch (JAXBException ex) {
                    Message msg = new Message("LOAD_RESOURCE_EXC", BUNDLE, url);
                    LOG.log(Level.WARNING, msg.toString(), ex);
                } catch (IOException ex) {
                    Message msg = new Message("LOAD_RESOURCE_EXC", BUNDLE, url);
                    LOG.log(Level.WARNING, msg.toString(), ex);
                }
                if (null != candidateExtensions) {
                    extensions.addAll(candidateExtensions);
                }
            }
        } catch (IOException ex) {
            Message msg = new Message("GET_RESOURCES_EXC", BUNDLE, resourceName);
            LOG.log(Level.WARNING, msg.toString(), ex);
        }
        return extensions;
    }

}
