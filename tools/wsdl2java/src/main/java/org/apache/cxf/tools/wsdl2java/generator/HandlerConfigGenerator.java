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

package org.apache.cxf.tools.wsdl2java.generator;

import java.io.Writer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.util.ProcessorUtil;

public class HandlerConfigGenerator extends AbstractGenerator {

    private static final String HANDLER_CHAIN_NAME = "";
    private JavaInterface intf;
    private JavaAnnotation handlerChainAnnotation; 
  
    public HandlerConfigGenerator(JavaInterface i, ProcessorEnvironment env) {
        
        this.name = ToolConstants.HANDLER_GENERATOR;
        this.intf = i;
        super.setEnvironment(env);
    }

    public JavaAnnotation getHandlerAnnotation() {
        return handlerChainAnnotation;
    }

    public boolean passthrough() {
        if (this.intf.getHandlerChains() == null) {
            return true;
        }
        return false;
    }
    
    public void generate() throws ToolException {
        if (passthrough()) {
            return;
        }

        Element e = this.intf.getHandlerChains();
        NodeList nl = e.getElementsByTagNameNS(ToolConstants.HANDLER_CHAINS_URI,
                                               ToolConstants.HANDLER_CHAIN);
        if (nl.getLength() > 0) {
            String fName = ProcessorUtil.getHandlerConfigFileName(this.intf.getName());
            handlerChainAnnotation = new JavaAnnotation("HandlerChain");
            handlerChainAnnotation.addArgument("name", HANDLER_CHAIN_NAME);
            handlerChainAnnotation.addArgument("file", fName + ".xml");
            generateHandlerChainFile(e, parseOutputName(this.intf.getPackageName(),
                                                        fName,
                                                        ".xml"));
        }
    }

    private void generateHandlerChainFile(Element hChains, Writer writer) throws ToolException {
        
        XMLUtils.generateXMLFile(hChains, writer);
    }
}
