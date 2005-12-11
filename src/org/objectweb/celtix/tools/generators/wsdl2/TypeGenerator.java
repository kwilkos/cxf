package org.objectweb.celtix.tools.generators.wsdl2;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.xjc.api.S2JJAXBModel;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class TypeGenerator extends AbstractGenerator {

    public TypeGenerator() {
        this.name = "wsdl2.types.generator";
    }

    public TypeGenerator(ProcessorEnvironment env) {
        this();
        setEnvironment(env);
    }

    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_IMPL)
            || env.optionSet(ToolConstants.CFG_INTERFACE)) {
            return true;
        } 
        return false;
    }

    @SuppressWarnings("unchecked")
    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Map<String, S2JJAXBModel> jaxbModels = (Map<String, S2JJAXBModel>) env.get("jaxbmodels");
        Collection<S2JJAXBModel> typeModels = jaxbModels.values();
        for (S2JJAXBModel rawJaxbModel : typeModels) {
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
    }
}
