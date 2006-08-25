package org.objectweb.celtix.tools.generators.java2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.WSDLModel;

public class WSDLOutputResolver extends SchemaOutputResolver {

    private final ProcessorEnvironment env;
    private final WSDLModel wmodel;

    public WSDLOutputResolver(ProcessorEnvironment penv, WSDLModel model) {
        this.env = penv;
        this.wmodel = model;
    }

    private File getFile(String filename) {
        Object obj = env.get(ToolConstants.CFG_OUTPUTFILE);
        String wsdlFile = obj == null ? "./" : (String)obj;
        File file = null;
        if (wsdlFile != null) {
            file = new File(wsdlFile);
            if (file.isDirectory()) {
                file = new File(file, filename);
            } else {
                file = new File(file.getParent(), filename);
            }
        } else {
            file = new File(".", filename);
        }
        return file;
    }

    public Result createOutput(String namespaceUri, String suggestedFileName) {
        wmodel.addSchemaNSFileToMap(namespaceUri, suggestedFileName);
        File wsdlFile = getFile(suggestedFileName);
        Result result = null;
        try {
            result = new StreamResult(new FileOutputStream(wsdlFile));
            result.setSystemId(wsdlFile.toString().replace('\\', '/'));
        } catch (FileNotFoundException e) {
            throw new ToolException("Can not create the schema file", e);
        }
        return result;
    }
}
