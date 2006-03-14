package org.objectweb.celtix.bus.ws.addressing.soap;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.objectweb.celtix.bus.ws.addressing.Names;
import org.objectweb.celtix.common.logging.LogUtils;

// importation convention: if the same class name is used for 
// 2005/08 and 2004/08, then the former version is imported
// and the latter is fully qualified when used
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.RelatesToType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.Relationship;


/**
 * This class is responsible for transforming between the native 
 * WS-Addressing schema version (i.e. 2005/08) and exposed
 * version (currently may be 2005/08 or 2004/08).
 * <p>
 * The native version is that used throughout the stack, were the
 * WS-A types are represented via the JAXB generated types for the
 * 2005/08 schema.
 * <p>
 * The exposed version is that used when the WS-A types are 
 * externalized, i.e. are encoded in the headers of outgoing 
 * messages. For outgoing requests, the exposed version is 
 * determined from configuration. For outgoing responses, the
 * exposed version is determined by the exposed version of
 * the corresponding request.
 * <p>
 * The motivation for using different native and exposed types
 * is usually to facilitate a WS-* standard based on an earlier 
 * version of WS-Adressing (for example WS-RM depends on the
 * 2004/08 version).
 */
public class VersionTransformer 
    extends org.objectweb.celtix.bus.ws.addressing.VersionTransformer {

    public static final Set<QName> HEADERS;
    private static final Logger LOG = LogUtils.getL7dLogger(VersionTransformer.class);
    
    protected MAPCodec codec;
    
    /**
     * Constructor.
     * 
     * @param mapCodec the MAPCodec to use
     */
    public VersionTransformer(MAPCodec mapCodec) {
        codec = mapCodec;
    }
    
    /**
     * Encode message in exposed version.
     * 
     * @param exposeAs specifies the WS-Addressing version to expose
     * @param value the value to encode
     * @param localName the localName for the header 
     * @param clz the class
     * @param header the SOAP header
     * @param marshaller the JAXB marshaller to use
     */
    public <T> void encodeAsExposed(String exposeAs,
                                    T value,
                                    String localName,
                                    Class<T> clz,
                                    SOAPHeader header,
                                    Marshaller marshaller) 
        throws JAXBException {
        if (value != null) {
            if (NATIVE_VERSION.equals(exposeAs)) {
                codec.encodeMAP(value,
                                new QName(exposeAs, localName),
                                clz,
                                header,
                                marshaller);
            } else if (Names200408.WSA_NAMESPACE_NAME.equals(exposeAs)) {
                if (AttributedURIType.class.equals(clz)) {
                    codec.encodeMAP(convert((AttributedURIType)value),
                                    new QName(exposeAs, localName),
                                    AttributedURI.class,
                                    header,
                                    marshaller);
                } else if (EndpointReferenceType.class.equals(clz)) {
                    codec.encodeMAP(convert((EndpointReferenceType)value),
                                    new QName(exposeAs, localName),
                                    Names200408.EPR_TYPE,
                                    header,
                                    marshaller);
                    
                } else if (RelatesToType.class.equals(clz)) {
                    codec.encodeMAP(convert((RelatesToType)value),
                                    new QName(exposeAs, localName),
                                    Relationship.class,
                                    header,
                                    marshaller);
                    
                }
            } 
        }
    }
    
    /**
     * Decodes a MAP from a exposed version.
     *
     * @param encodedAs specifies the encoded version
     * @param clz the class
     * @param headerElement the SOAP header element
     * @param marshaller the JAXB marshaller to use
     * @return the decoded value
     */
    @SuppressWarnings("unchecked")
    public <T> T decodeAsNative(String encodedAs,
                                Class<T> clz,
                                SOAPHeaderElement headerElement,
                                Unmarshaller unmarshaller) 
        throws JAXBException {
        T ret = null;
        LOG.fine("decodeAsNative: encodedAs: " + encodedAs);
        LOG.fine("                class: " + clz.getName());

        if (NATIVE_VERSION.equals(encodedAs)) {
            ret = codec.decodeMAP(clz, headerElement, unmarshaller);
        } else if (Names200408.WSA_NAMESPACE_NAME.equals(encodedAs)) {
            if (AttributedURIType.class.equals(clz)) {
                return (T)convert(codec.decodeMAP(AttributedURI.class, 
                                                  headerElement, 
                                                  unmarshaller));
            } else if (EndpointReferenceType.class.equals(clz)) {
                return (T)convert(codec.decodeMAP(Names200408.EPR_TYPE, 
                                                  headerElement, 
                                                  unmarshaller));
            }  else if (RelatesToType.class.equals(clz)) {
                return (T)convert(codec.decodeMAP(Relationship.class, 
                                                  headerElement, 
                                                  unmarshaller));
            }           
        }
        return ret;
    }
           
    /**
     * Augment the set of headers understood by the protocol binding
     * with the 2004/08 header QNames.
     */
    static {
        Set<QName> headers = new HashSet<QName>();
        headers.addAll(Names.HEADERS);
        Names200408.addHeaders(headers);
        HEADERS = Collections.unmodifiableSet(headers);
    }
    
    /**
     * Holder for 2004/08 Names
     */
    public static final class Names200408 
        extends org.objectweb.celtix.bus.ws.addressing.VersionTransformer.Names200408 {
        
        protected Names200408() {
        }
        
        /**
         * Adds 2004/08 headers to set.
         * 
         * @param headers set of headers
         */
        private static void addHeaders(Set<QName> headers) {
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_FROM_NAME));
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_TO_NAME));
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_REPLYTO_NAME));
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_FAULTTO_NAME));
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_ACTION_NAME));
            headers.add(new QName(WSA_NAMESPACE_NAME, 
                                  Names.WSA_MESSAGEID_NAME));
        }
    }
}
