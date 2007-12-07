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

package org.apache.cxf.tools.wsdlto.javascript;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.javascript.BasicNameManager;
import org.apache.cxf.javascript.JavascriptQueryHandler;
import org.apache.cxf.javascript.NamespacePrefixAccumulator;
import org.apache.cxf.javascript.service.ServiceJavascriptBuilder;
import org.apache.cxf.javascript.types.SchemaJavascriptBuilder;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ClassNameProcessor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.wsdlto.core.WSDLToProcessor;

public class WSDLToJavaScriptProcessor extends WSDLToProcessor {
    private static final Charset UTF8 = Charset.forName("utf-8");
    
    public void process() throws ToolException {
        super.process();

        ServiceInfo serviceInfo = context.get(ServiceInfo.class);

        //File jsFile = getOutputFile(builder.getOutputFile(), serviceInfo.getName().getLocalPart() + ".js");
        File jsFile = null;

        BasicNameManager nameManager = new BasicNameManager(serviceInfo, null);
        NamespacePrefixAccumulator prefixManager = new NamespacePrefixAccumulator(serviceInfo
            .getXmlSchemaCollection());
        Collection<SchemaInfo> schemata = serviceInfo.getSchemas();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(jsFile);
            if (null != context.get(ToolConstants.CFG_JAVASCRIPT_UTILS)) {
                JavascriptQueryHandler.writeUtilsToResponseStream(WSDLToJavaScriptProcessor.class, fileOutputStream);
            }
            
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, UTF8);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
                
            for (SchemaInfo schema : schemata) {
                SchemaJavascriptBuilder jsBuilder = new SchemaJavascriptBuilder(serviceInfo
                    .getXmlSchemaCollection(), prefixManager, nameManager, schema);
                String allThatJavascript = jsBuilder.generateCodeForSchema(schema);
                writer.append(allThatJavascript);
            }

            ServiceJavascriptBuilder serviceBuilder = new ServiceJavascriptBuilder(serviceInfo, prefixManager,
                                                                                   nameManager);
            serviceBuilder.walk();
            String serviceJavascript = serviceBuilder.getCode();
            writer.append(serviceJavascript);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new ToolException(e);
        } catch (IOException e) {
            throw new ToolException(e);
        }

    }
}
