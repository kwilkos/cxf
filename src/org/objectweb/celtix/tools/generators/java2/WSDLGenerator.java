package org.objectweb.celtix.tools.generators.java2;

import java.io.IOException;

import javax.xml.bind.SchemaOutputResolver;

import com.sun.xml.bind.api.JAXBRIContext;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.WSDLModel;

public class WSDLGenerator {
    private final WSDLModel wmodel;
    private final ProcessorEnvironment env;
    public WSDLGenerator(WSDLModel model, ProcessorEnvironment penv) {
        wmodel = model;
        this.env = penv;
    }

    public void generate() {
        writeDefinition(wmodel);
        JAXBRIContext jxbContext = wmodel.getJaxbContext();
        SchemaOutputResolver resolver = new WSDLOutputResolver(env);

        try {
            jxbContext.generateSchema(resolver);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean writeDefinition(WSDLModel model) {
        return true;
    }
}
