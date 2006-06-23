package org.objectweb.celtix.jbi.se;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;

/** Manage deployment of service units to the Celtix service engine
 * 
 */
public class CeltixServiceUnitManager implements ServiceUnitManager {
    
    private static final Logger LOG = LogUtils.getL7dLogger(CeltixServiceUnitManager.class);
    
    private ComponentContext ctx; 
    private final Map<String, CeltixServiceUnit> serviceUnits 
        = new HashMap<String, CeltixServiceUnit>();
    private final Map<ServiceEndpoint, CeltixServiceUnit> csuMap 
        = new HashMap<ServiceEndpoint, CeltixServiceUnit>();
    
    private final Bus bus;
    private final ComponentClassLoader componentParentLoader; 
    
    public CeltixServiceUnitManager(Bus b, ComponentContext c, ComponentClassLoader loader) {
        ctx = c;
        bus = b;
        componentParentLoader = loader;
    }
    
    // Implementation of javax.jbi.component.ServiceUnitManager
    
    public final void shutDown(final String suName) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.SHUTDOWN", LOG) + suName);
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
        if (!serviceUnits.containsKey(suName)) {
            throw new DeploymentException(new Message("UNDEPLOYED.SU", LOG).toString() + suName);
        }
        serviceUnits.remove(suName);
    }
    
    public final String deploy(final String suName, final String suRootPath) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.DEPLOY", LOG) + suName + " path: " + suRootPath);
        
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
        if (serviceUnits.containsKey(suName)) {
            throw new DeploymentException(new Message("DUPLICATED.SU", LOG) + suName);
        }
        
        if (suRootPath == null) {
            throw new DeploymentException(new Message("SU.ROOT.NULL", LOG).toString());
        } 
        if (suRootPath.length() == 0) {
            throw new DeploymentException(new Message("SU.ROOT.EMPTY", LOG).toString());
        }
        
        String msg =  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\" "  
            +  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " 
            +  "version=\"1.0\" " 
            +  "xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi/management-message " 
            +  "./managementMessage.xsd\">"
            + "<jbi-task-result>"
            + "<frmwk-task-result>" 
            + "<frmwk-task-result-details>" 
            + "<task-result-details>" 
            + "<task-id>deploy</task-id>" 
            + "<task-result>SUCCESS</task-result>" 
            + "</task-result-details>" 
            + "<locale>en_US</locale>" 
            + "</frmwk-task-result-details>"
            + "<is-cause-framework>YES</is-cause-framework>"
            + "</frmwk-task-result>"
            + "<component-task-result>"
            + "<component-name>" + ctx.getComponentName() + "</component-name>"
            + "<component-task-result-details>"
            + "<task-result-details>"
            + "<task-id>deploy</task-id>"
            + "<task-result>SUCCESS</task-result>"
            + "</task-result-details>"
            + "</component-task-result-details>"
            + "</component-task-result>"
            + "</jbi-task-result>"
            + "</jbi-task>";
        
        return msg;
    }
    
    public final String undeploy(final String suName, final String suRootPath) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.UNDEPLOY", LOG) + suName + " path: " + suRootPath);
        
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
                
        if (suRootPath == null) {
            throw new DeploymentException(new Message("SU.ROOT.NULL", LOG).toString());
        } 
        if (suRootPath.length() == 0) {
            throw new DeploymentException(new Message("SU.ROOT.EMPTY", LOG).toString());
        }
        
        
        String msg =  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\" "  
            +  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " 
            +  "version=\"1.0\" " 
            +  "xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi/management-message " 
            +  "./managementMessage.xsd\">"
            + "<jbi-task-result>"
            + "<frmwk-task-result>" 
            + "<frmwk-task-result-details>" 
            + "<task-result-details>" 
            + "<task-id>undeploy</task-id>" 
            + "<task-result>SUCCESS</task-result>" 
            + "</task-result-details>" 
            + "<locale>en_US</locale>" 
            + "</frmwk-task-result-details>" 
            + "</frmwk-task-result>"
            + "<component-task-result>"
            + "<component-name>" + ctx.getComponentName() + "</component-name>"
            + "<component-task-result-details>"
            + "<task-result-details>"
            + "<task-id>undeploy</task-id>"
            + "<task-result>SUCCESS</task-result>"
            + "</task-result-details>"
            + "</component-task-result-details>"
            + "</component-task-result>"
            + "</jbi-task-result>"
            + "</jbi-task>";
        serviceUnits.remove(suName);
        return msg;
    }
    
    public final void init(final String suName, final String suRootPath) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.INIT", LOG) + suName + " path: " + suRootPath);
     
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
                
        if (suRootPath == null) {
            throw new DeploymentException(new Message("SU.ROOT.NULL", LOG).toString());
        } 
        if (suRootPath.length() == 0) {
            throw new DeploymentException(new Message("SU.ROOT.EMPTY", LOG).toString());
        }
        
        try { 
            Thread.currentThread().setContextClassLoader(componentParentLoader);
            CeltixServiceUnit csu = new CeltixServiceUnit(bus, suRootPath, componentParentLoader);
            csu.prepare(ctx);
            serviceUnits.put(suName, csu);    
        } catch (Exception ex) { 
            ex.printStackTrace();
            throw new DeploymentException(ex);
        }
    }
    
    public final void start(final String suName) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.START", LOG) + suName);
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
        if (!serviceUnits.containsKey(suName)) {
            throw new DeploymentException(new Message("UNDEPLOYED.SU", LOG) + suName);
        }
        
        CeltixServiceUnit csu = serviceUnits.get(suName); 
        assert csu != null;
        csu.start(ctx, this); 
    }
    
    public void putServiceEndpoint(ServiceEndpoint ref, CeltixServiceUnit csu) {
        csuMap.put(ref, csu);
    }
    
    public final CeltixServiceUnit getServiceUnitForEndpoint(ServiceEndpoint ep) { 
        return csuMap.get(ep);
    } 
    
    public final void stop(final String suName) throws DeploymentException {
        LOG.info(new Message("SU.MANAGER.STOP", LOG) + suName);
        if (suName == null) {
            throw new DeploymentException(new Message("SU.NAME.NULL", LOG).toString());
        } 
        if (suName.length() == 0) {
            throw new DeploymentException(new Message("SU.NAME.EMPTY", LOG).toString());
        }
        if (!serviceUnits.containsKey(suName)) {
            throw new DeploymentException(new Message("UNDEPLOYED.SU", LOG) + suName);
        }
        serviceUnits.get(suName).stop(ctx);
    }
    
    Document getServiceDescription(final ServiceEndpoint serviceEndpoint) { 
        Document ret = null;
        
        if (csuMap.keySet().contains(serviceEndpoint)) { 
            CeltixServiceUnit csu = csuMap.get(serviceEndpoint);
            ret = csu.getWsdlAsDocument();
        } 
        return ret;
    } 
}
