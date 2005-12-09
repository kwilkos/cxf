package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class PortTypeProcessor {

    private final ProcessorEnvironment env;
    
    public PortTypeProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }
    
    public void process(JavaModel jmodel, PortType portType) throws Exception {
        JavaInterface intf = new JavaInterface(jmodel);
        String namespace = portType.getQName().getNamespaceURI();
        String packageName = ProcessorUtil.parsePackageName(namespace,
                                                            (String[])env.get(ToolConstants.CFG_PACKAGENAME));
        String location = ProcessorUtil.getAbsolutePath((String) env.get(ToolConstants.CFG_WSDLURL));
        
        intf.setName(portType.getQName().getLocalPart());
        intf.setNamespace(namespace);
        intf.setPackageName(packageName);
        intf.setLocation(location);

        List operations = portType.getOperations();
        for (Iterator iter = operations.iterator(); iter.hasNext();) {
            Operation operation = (Operation) iter.next();
            OperationProcessor operationProcessor = new OperationProcessor(env);
            operationProcessor.process(intf, operation);
        }
        jmodel.setLocation(location);
        jmodel.addInterface(intf.getName() , intf);
    }    
}
