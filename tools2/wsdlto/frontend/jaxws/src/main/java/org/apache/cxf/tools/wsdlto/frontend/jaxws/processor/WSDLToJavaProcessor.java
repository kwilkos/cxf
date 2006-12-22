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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor;

import javax.wsdl.Definition;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.wsdl2java.processor.internal.SEIAnnotationProcessor;
import org.apache.cxf.tools.wsdl2java.processor.internal.ServiceProcessor;
import org.apache.cxf.tools.wsdlto.core.WSDLToProcessor;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    public void process() throws ToolException {
        super.process();

        JavaModel jmodel = wsdlDefinitionToJavaModel(getServiceInfo());
        
        if (jmodel == null) {
            Message msg = new Message("FAIL_TO_CREATE_JAVA_MODEL", LOG);
            throw new ToolException(msg);
        }

        context.put(JavaModel.class, jmodel);
    }

    private JavaModel wsdlDefinitionToJavaModel(ServiceInfo serviceInfo) throws ToolException {
        JavaModel javaModel = new JavaModel();
       /* getEnvironment().put(ToolConstants.RAW_JAXB_MODEL, getRawJaxbModel());*/

        // TODO replace the definition with service model

        //javaModel.setJAXWSBinding(customizing(definition));

        // TODO refactroing the internal processors to use the service model

        //Map<QName, PortType> portTypes = getPortTypes(definition);
        //for (Iterator iter = portTypes.keySet().iterator(); iter.hasNext();) {
        //PortType portType = (PortType)portTypes.get(iter.next());
        //PortTypeProcessor portTypeProcessor = new PortTypeProcessor(getEnvironment());
        //portTypeProcessor.process(javaModel, portType);
        //}

        ServiceProcessor serviceProcessor = new ServiceProcessor(context, getWSDLDefinition());
        serviceProcessor.process(javaModel);
        
        SEIAnnotationProcessor seiAnnotationProcessor = new SEIAnnotationProcessor(context);
        seiAnnotationProcessor.process(javaModel, getWSDLDefinition());
        return javaModel;
    }

    private Definition getWSDLDefinition() {
        // TODO remove this method after the refactoring of procesors were done
        return null;
    }

    // TODO replace the definition with service model
    //     private JAXWSBinding customizing(Definition def) {
    //         JAXWSBinding binding = CustomizationParser.getInstance().getDefinitionExtension();
    //         if (binding != null) {
    //             return binding;
    //         }
    
    //         List extElements = def.getExtensibilityElements();
    //         if (extElements.size() > 0) {
    //             Iterator iterator = extElements.iterator();
    //             while (iterator.hasNext()) {
    //                 Object obj = iterator.next();
    //                 if (obj instanceof JAXWSBinding) {
    //                     binding = (JAXWSBinding)obj;
    //                 }
    //             }
    //         }
    
    //         if (binding == null) {
    //             binding = new JAXWSBinding();
    //         }
    //         return binding;
    //     }
}
