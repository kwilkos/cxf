/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.java2wsdl.generator.jaxws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.java2wsdl.processor.JavaToWSDLProcessor;

public class WSDLOutputResolver extends SchemaOutputResolver {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    private final ToolContext env;
    private final WSDLModel wmodel;

    public WSDLOutputResolver(ToolContext penv, WSDLModel model) {
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
