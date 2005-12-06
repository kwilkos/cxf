package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.Element;

import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import org.apache.velocity.app.Velocity;
import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.generators.AbstractGenerator;

public class WSDLToProcessor implements Processor {

    protected Definition wsdlDefinition;
    protected ProcessorEnvironment env;
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;
    protected Map<String, S2JJAXBModel>jaxbModels = new HashMap<String, S2JJAXBModel>();

    private final Map<String, AbstractGenerator> generators = new HashMap<String, AbstractGenerator>();

    private void parseWSDL(String wsdlURL) throws WSDLException {
        wsdlFactory = WSDLFactory.newInstance();
        wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        wsdlDefinition = wsdlReader.readWSDL(wsdlURL);
    }

    private String getVelocityLogFile(String logfile) {
        String logdir = System.getProperty("user.home");
        if (logdir == null || logdir.length() == 0) {
            logdir = System.getProperty("user.dir");
        }
        return logdir + File.separator + logfile;
    }
    private void initVelocity() {
        try {
            Properties props = new Properties();
            String clzName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
            props.put("resource.loader", "class");
            props.put("class.resource.loader.class", clzName);
            props.put("runtime.log", getVelocityLogFile("velocity.log"));
            
            Velocity.init(props);
        } catch (Exception e) {
            System.err.println("Can't initialize velocity engine");
        }
    }

    private void initJAXBModel() {
        Types typesElement = wsdlDefinition.getTypes();
        if (typesElement == null) {
            if (env.isVerbose()) {
                System.err.println("No schema provided in the wsdl file");
            }
            return;
        }
        Iterator ite = typesElement.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof Schema) {
                Schema schema = (Schema)obj;
                Element element = schema.getElement();
                String targetNamespace = element.getAttribute("targetNamespace");
                SchemaCompiler schemaCompiler = XJC.createSchemaCompiler();
                schemaCompiler.parseSchema("types#", element);
                jaxbModels.put(targetNamespace, schemaCompiler.bind());
            }
        }
    }

    
    protected void init() {
        try {
            parseWSDL((String) env.get(ToolConstants.CFG_WSDLURL));
            initVelocity();
            initJAXBModel();
        } catch (WSDLException we) {
            System.err.println("Can not create wsdl model");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, S2JJAXBModel> getJAXBModels() {
        return this.jaxbModels;
    }
    
    public Definition getWSDLDefinition() {
        return this.wsdlDefinition;
    }
    
    public void addGenerator(String name, AbstractGenerator gen) {
        generators.put(name, gen);
    }

    public void process() throws Exception {
    }

    protected void doGeneration() throws Exception {
        for (String genName : generators.keySet()) {
            AbstractGenerator gen = generators.get(genName);
            gen.generate();
        }
    }
    
    public void setEnvironment(ProcessorEnvironment penv) {
        this.env = penv;
    }

    public ProcessorEnvironment getEnvironment() {
        return this.env;
    }
}
