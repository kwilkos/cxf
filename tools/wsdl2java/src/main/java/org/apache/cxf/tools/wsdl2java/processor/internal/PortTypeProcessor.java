package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;

import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.common.extensions.jaxws.JAXWSBinding;

import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaModel;

import org.apache.cxf.tools.util.ProcessorUtil;

public class PortTypeProcessor extends AbstractProcessor {
    private List<String> operationMap = new ArrayList<String>();
    
    public PortTypeProcessor(ProcessorEnvironment penv) {
        super(penv);
    }
    
    public void process(JavaModel jmodel, PortType portType) throws ToolException {
        operationMap.clear();
        JavaInterface intf = new JavaInterface(jmodel);
        intf.setJAXWSBinding(customizing(jmodel, portType));
        intf.setHandlerChains(CustomizationParser.getInstance().getHandlerChains());
        
        String namespace = portType.getQName().getNamespaceURI();
        String packageName = ProcessorUtil.parsePackageName(namespace, env.mapPackageName(namespace));

        String location = (String)env.get(ToolConstants.CFG_WSDLURL);
        String urlLocation;
        try {
            location = ProcessorUtil.getAbsolutePath(location);
            urlLocation = ProcessorUtil.getWSDLURL(location).toString();
            
        } catch (Exception ioe) {
            Message msg = new Message("CANNOT_FIND_WSDL", LOG, env.get(ToolConstants.CFG_WSDLURL));
            throw new ToolException(msg, ioe);
        }
        String serviceName = portType.getQName().getLocalPart();
        intf.setWebServiceName(serviceName);
        intf.setName(ProcessorUtil.mangleNameToClassName(serviceName));
        intf.setNamespace(namespace);
        intf.setPackageName(packageName);
        intf.setLocation(urlLocation);

        List operations = portType.getOperations();
       
        for (Iterator iter = operations.iterator(); iter.hasNext();) {
           
            Operation operation = (Operation)iter.next();
            if (isOverloading(operation.getName())) {
                LOG.log(Level.WARNING, "SKIP_OVERLOADED_OPERATION", operation.getName()); 
                continue;
            }
            OperationProcessor operationProcessor = new OperationProcessor(env);
            operationProcessor.process(intf, operation);
        }
        //Fixed issue 305772
        jmodel.setLocation(urlLocation);
        jmodel.addInterface(intf.getName(), intf);
       
    }

    private boolean isOverloading(String operationName) {
        if (operationMap.contains(operationName)) {
            return true;
        } else {
            operationMap.add(operationName);
        }
        return false;
    }

    private JAXWSBinding customizing(JavaModel jmodel, PortType portType) {
        String portTypeName = portType.getQName().getLocalPart();
        JAXWSBinding bindings = CustomizationParser.getInstance().getPortTypeExtension(portTypeName);
        if (bindings != null) {
            return bindings;
        } else if (jmodel.getJAXWSBinding() != null) {
            return jmodel.getJAXWSBinding();
        } else {
            // TBD: There is no extensibilityelement in port type
            return new JAXWSBinding();
        }
    }
}
