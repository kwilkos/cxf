package org.apache.cxf.tools.java2wsdl.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.java2wsdl.processor.JavaToWSDLProcessor;

public class WSDLOutputResolver extends SchemaOutputResolver {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
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
            Message msg = new Message("CANNOT_CREATE_SCHEMA_FILE", LOG);
            throw new ToolException(msg, e);
        }
        return result;
    }
}
