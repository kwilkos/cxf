package org.apache.cxf.jaxws.support;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;

public class JaxwsImplementorInfo {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxwsImplementorInfo.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Class<?> implementorClass;
    private Class<?> seiClass;
    private WebService implementorAnnotation;
    private WebService seiAnnotation;

    public JaxwsImplementorInfo(Class<?> ic) {
        implementorClass = ic;
        initialise();
    }   

    public Class<?> getSEIClass() {
        return seiClass;
    }

    public Class<?> getImplementorClass() {
        return implementorClass;
    }

    public String getWsdlLocation() {
        if (null != seiAnnotation) {
            return seiAnnotation.wsdlLocation();
        } else if (null != implementorAnnotation) {
            return implementorAnnotation.wsdlLocation();
        }
        return null;
    }

    /**
     * See use of targetNamespace in {@link WebService}.
     * 
     * @return the qualified name of the service.
     */
    public QName getServiceName() {
        String serviceName = null;
        String namespace = null;
        if (implementorAnnotation != null) {
            serviceName = implementorAnnotation.serviceName();
            if (null == serviceName || "".equals(serviceName)) {
                serviceName = implementorClass.getName();
            }
            namespace = implementorAnnotation.targetNamespace();
        }
        if (null != namespace && !"".equals(namespace) && null != serviceName && !"".equals(serviceName)) {
            return new QName(namespace, serviceName);
        }
        return null;
    }

    /**
     * See use of targetNamespace in {@link WebService}.
     * 
     * @return the qualified name of the endpoint.
     */
    public QName getEndpointName() {
        String portName = null;
        String namespace = null;
        if (implementorAnnotation != null) {
            portName = implementorAnnotation.portName();
            if (null == portName || "".equals(portName)) {
                portName = implementorClass.getSimpleName();
            }
            namespace = implementorAnnotation.targetNamespace();
        }
        if (null != namespace && !"".equals(namespace) && null != portName && !"".equals(portName)) {
            return new QName(namespace, portName);
        }
        return null;
    }

    private void initialise() {
        implementorAnnotation = implementorClass.getAnnotation(WebService.class);
        if (null != implementorAnnotation) {

            String sei = implementorAnnotation.endpointInterface();
            if (null != sei && !"".equals(sei)) {
                try {
                    seiClass = ClassLoaderUtils.loadClass(sei, implementorClass);
                } catch (ClassNotFoundException ex) {
                    throw new WebServiceException(BUNDLE.getString("SEI_LOAD_FAILURE_MSG"), ex);
                }
                seiAnnotation = seiClass.getAnnotation(WebService.class);
                if (null == seiAnnotation) {
                    throw new WebServiceException(BUNDLE.getString("SEI_WITHOUT_WEBSERVICE_ANNOTATION_EXC"));
                }
                String portName = seiAnnotation.portName();
                String serviceName = seiAnnotation.serviceName();
                String endpointInterface = seiAnnotation.endpointInterface();
                if ((null != portName && !"".equals(portName))
                    || (null != serviceName && !"".equals(serviceName))
                    || (null != endpointInterface && !"".equals(endpointInterface))) {
                    throw new WebServiceException(
                        BUNDLE.getString("ILLEGAL_ATTRIBUTE_IN_SEI_ANNOTATION_EXC"));
                }
            }
        }
    }

}
