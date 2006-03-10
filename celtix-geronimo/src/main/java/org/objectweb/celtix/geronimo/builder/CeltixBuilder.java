package org.objectweb.celtix.geronimo.builder;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.handler.Handler;

import com.sun.java.xml.ns.j2ee.ObjectFactory;
import com.sun.java.xml.ns.j2ee.PortComponentType;
import com.sun.java.xml.ns.j2ee.WebserviceDescriptionType;
import com.sun.java.xml.ns.j2ee.WebservicesType;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.StoredObject;


public class CeltixBuilder implements WebServiceBuilder, GBeanLifecycle {

    public static final GBeanInfo GBEAN_INFO;
    private static final String POJO_CLASS_ATTR = "pojoClassName"; 
    private static final String WEB_SERVICE_CONTAINER_ATTR = "webServiceContainer";
    
    private JAXBContext ctx;


    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CeltixBuilder.class, 
                                                                     NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        System.out.println("CeltixBuilder returning GBean info");
        return GBEAN_INFO;
    }

    
    public Map<String, PortInfo> parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB, 
                                         Map correctedPortLocations)
        throws DeploymentException {

        Map<String, PortInfo> map = new HashMap<String, PortInfo>();
        
        try { 
            InputStream in = wsDDUrl.openStream();
            if (in == null) {
                throw new DeploymentException("unable to read descriptor " + wsDDUrl);
            }
            
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(in);
            
            WebservicesType wst = null; 
            if (obj instanceof JAXBElement) {
                wst = (WebservicesType)((JAXBElement)obj).getValue();
            }
            
            for (WebserviceDescriptionType desc : wst.getWebserviceDescription()) {
                String wsdlFile = desc.getWsdlFile().getValue();
                
                for (PortComponentType port : desc.getPortComponent()) {
                    String servlet = port.getServiceImplBean().getServletLink().getValue();
                    String sei = port.getServiceEndpointInterface().getValue();
                    String portName = port.getPortComponentName().getValue();
                    
                    PortInfo portInfo = new PortInfo();
                    
                    portInfo.setServletLink(servlet);
                    portInfo.setServiceEndpointInterfaceName(sei);
                    portInfo.setPortName(portName);
                    portInfo.setWsdlFile(wsdlFile);
                    portInfo.setHandlers(port.getHandler());
 
                    map.put(servlet, portInfo);
                }
            }
            
            return map;
       
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new DeploymentException("unable to read " + wsDDUrl, ex);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new DeploymentException("unable to parse webservices.xml", ex);
        }
    }

    
    public void configurePOJO(GBeanData targetGBean, JarFile moduleFile, Object pi, String seiClassName,
                              ClassLoader classLoader)
        throws DeploymentException {

        assert pi instanceof PortInfo : "received incorrect portInfo object";
    
        try { 
            PortInfo portInfo = (PortInfo)pi;
            
            System.out.println("CeltixBuilder configurePOJO");
            
            checkSEIAvailable(seiClassName, classLoader);
            targetGBean.setAttribute(POJO_CLASS_ATTR, seiClassName); 
            /*List<Handler> handlers =*/ buildHandlerChain(portInfo);
            
            CeltixWebServiceContainer container = new CeltixWebServiceContainer();
            targetGBean.setAttribute(WEB_SERVICE_CONTAINER_ATTR, new StoredObject(container));
        } catch (IOException ex) {
            throw new DeploymentException("unable to store CeltixWebServiceContainer", ex);
        }
    }

    public void configureEJB(GBeanData targetGBean, JarFile moduleFile, Object portInfo, 
                             ClassLoader classLoader)
        throws DeploymentException {

        System.out.println("CeltixBuilder configureEJB");
        throw new DeploymentException("configureEJB NYI");
    }

    public void doStart() throws Exception {
        ctx = JAXBContext.newInstance(ObjectFactory.class);
    }

    public void doStop() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void doFail() {
        // TODO Auto-generated method stub
        
    }
    
    private void checkSEIAvailable(String className, ClassLoader loader) throws DeploymentException {
        try {
            loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("unable to load Service Endpoint Interface: " + className, ex);
        }
    }
    
    private List<Handler> buildHandlerChain(PortInfo portInfo) {
        return new ArrayList<Handler>();
    }
}
