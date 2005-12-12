package org.objectweb.celtix.tools.generators.java2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public class WSDLOutputResolver extends SchemaOutputResolver {

    private final ProcessorEnvironment env;

    public WSDLOutputResolver(ProcessorEnvironment penv) {
        this.env = penv;
    }

    private File getFile(String filename) {
        String wsdlFile = (String)env.get(ToolConstants.CFG_OUTPUTFILE);
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

    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        File wsdlFile = getFile(suggestedFileName);

        Result result = new StreamResult();
        try {
            result = new StreamResult(new FileOutputStream(wsdlFile));
            result.setSystemId(wsdlFile.toString().replace('\\', '/'));
        } catch (FileNotFoundException e) {
            throw e;
        }
        return result;
    }
}
