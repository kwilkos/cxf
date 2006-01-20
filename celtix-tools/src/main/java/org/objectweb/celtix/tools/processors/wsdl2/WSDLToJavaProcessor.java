package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.PortType;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.wsdl2.AntGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ClientGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.FaultGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ImplGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.SEIGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServerGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServiceGenerator;
import org.objectweb.celtix.tools.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.processors.wsdl2.internal.PortTypeProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.SEIAnnotationProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ServiceProcessor;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    protected void registerGenerators(JavaModel jmodel) {
        addGenerator(ToolConstants.SEI_GENERATOR, new SEIGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.FAULT_GENERATOR, new FaultGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SVR_GENERATOR, new ServerGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.IMPL_GENERATOR, new ImplGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.CLT_GENERATOR, new ClientGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SERVICE_GENERATOR, new ServiceGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.ANT_GENERATOR, new AntGenerator(jmodel, getEnvironment()));
    }
    
    public void process() throws ToolException {
        init();
        generateTypes();
        JavaModel jmodel = wsdlDefinitionToJavaModel(getWSDLDefinition());
        if (jmodel == null) {
            throw new ToolException("Can not create java model from wsdl model");
        }
        registerGenerators(jmodel);
        doGeneration();
    }

    private void generateTypes() throws ToolException {
        if (rawJaxbModel == null) {
            return;
        }
        try {
            JAXBModel jaxbModel = new JAXBModel(rawJaxbModel);
            JCodeModel jcodeModel = jaxbModel.getS2JJAXBModel().generateCode(null, null);
            String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
            FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(dir));
            jcodeModel.build(fileCodeWriter);
        } catch (IOException e) {
            throw new ToolException("Build type failed", e);
        }
    }

    private JavaModel wsdlDefinitionToJavaModel(Definition definition) throws ToolException {
        JavaModel javaModel = new JavaModel();
        getEnvironment().put(ToolConstants.RAW_JAXB_MODEL, getRawJaxbModel());
        
        javaModel.setJAXWSBinding(customizing(definition));
        
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

    private JAXWSBinding customizing(Definition def) {
        List extElements = def.getExtensibilityElements();
        JAXWSBinding binding = CustomizationParser.getInstance().getDefinitionExtension();
        if (binding != null) {
            return binding;
        }
        
        if (extElements.size() > 0) {
            Iterator iterator = extElements.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof JAXWSBinding) {
                    binding = (JAXWSBinding) obj;
                }
            }
        }
        if (binding == null) {
            binding = new JAXWSBinding();
        }
        return binding;
    }
}
