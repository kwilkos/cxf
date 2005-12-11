package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Part;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaException;
import org.objectweb.celtix.tools.common.model.JavaExceptionClass;
import org.objectweb.celtix.tools.common.model.JavaField;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.utils.ProcessorUtil;
    
public class FaultProcessor {

    private final ProcessorEnvironment env;

    public FaultProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }

    public void process(JavaMethod method, Map<String, Fault> faults) throws Exception {
        if (faults == null) {
            return;
        }

        Collection<Fault> faultsValue = faults.values();        
        for (Fault fault : faultsValue) {
            processFault(method, fault);
        }
    }

    @SuppressWarnings("unchecked")
    private void processFault(JavaMethod method, Fault fault) throws Exception {
        JavaModel model = method.getInterface().getJavaModel();        
        Message faultMessage = fault.getMessage();
        String name = ProcessorUtil.mangleNameToClassName(faultMessage.getQName().getLocalPart());
        String namespace = faultMessage.getQName().getNamespaceURI();
        method.addException(new JavaException(name, name, namespace));

        Map<String, Part> faultParts = faultMessage.getParts();
        Collection<Part> faultValues = faultParts.values();
        
        JavaExceptionClass expClass = new JavaExceptionClass(model);
        String packageName = ProcessorUtil.parsePackageName(namespace,
                                                            (String)env.get(ToolConstants.CFG_PACKAGENAME));
        expClass.setName(name);
        expClass.setNamespace(namespace);
        expClass.setPackageName(packageName);
        
        for (Part part : faultValues) {
            String fName = ProcessorUtil.resolvePartName(part);
            String fType = ProcessorUtil.resolvePartType(part);
            String fNamespace = ProcessorUtil.resolvePartNamespace(part);
            String fPackageName = ProcessorUtil.parsePackageName(fNamespace,
                                                                 (String)env.get(ToolConstants.
                                                                                 CFG_PACKAGENAME));
            JavaField fField = new JavaField(fName, fType, fNamespace);
            
            if (!method.getInterface().getPackageName().equals(fPackageName)) {
                fField.setClassName(fPackageName + "." + fType);
            }
            expClass.addField(fField);
        }
        model.addExceptionClass(name, expClass);
    }
}
