package org.objectweb.celtix.tools.processors.wsdl2;

import java.util.*;
import javax.wsdl.Definition;
import javax.wsdl.PortType;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.generators.wsdl2.ClientGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.FaultGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ImplGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.SEIGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServerGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServiceGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.TypeGenerator;
import org.objectweb.celtix.tools.processors.wsdl2.internal.PortTypeProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.SEIAnnotationProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ServiceProcessor;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    protected void registerGenerators(JavaModel jmodel) {
        addGenerator(ToolConstants.SEI_GENERATOR, new SEIGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.FAULT_GENERATOR, new FaultGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.TYPE_GENERATOR, new TypeGenerator(getEnvironment()));
        addGenerator(ToolConstants.IMPL_GENERATOR, new ImplGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SVR_GENERATOR, new ServerGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.CLT_GENERATOR, new ClientGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SERVICE_GENERATOR, new ServiceGenerator(jmodel, getEnvironment()));
    }
    
    public void process() throws Exception {
        init();
        JavaModel jmodel = wsdlDefinitionToJavaModel(getWSDLDefinition());
        registerGenerators(jmodel);
        doGeneration();
    }

    private JavaModel wsdlDefinitionToJavaModel(Definition definition) throws Exception {
        JavaModel javaModel = new JavaModel();

        getEnvironment().put("jaxbmodels", getJAXBModels());
        
        Map portTypes = definition.getPortTypes();

        for (Iterator iter = portTypes.keySet().iterator(); iter.hasNext();) {
            PortType portType = (PortType)portTypes.get(iter.next());
            PortTypeProcessor portTypeProcessor = new PortTypeProcessor(getEnvironment());
            portTypeProcessor.process(javaModel, portType);
        }

        ServiceProcessor serviceProcessor = new ServiceProcessor(env);
        serviceProcessor.process(javaModel, getWSDLDefinition());

        SEIAnnotationProcessor seiAnnotationProcessor = new SEIAnnotationProcessor(env);
        seiAnnotationProcessor.process(javaModel);

        return javaModel;
    }
}
