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


import java.util.Collection;
import javax.xml.transform.TransformerException;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.wsdlto.core.WSDLToProcessor;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.PortTypeProcessor;
//import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.SEIAnnotationProcessor;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.ServiceProcessor;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.annotator.BindingAnnotator;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.annotator.WebServiceAnnotator;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    public void process() throws ToolException {
        super.process();
        ServiceInfo serviceInfo = (ServiceInfo)context.get(ServiceInfo.class);

        Collection<SchemaInfo> schemas = serviceInfo.getTypeInfo().getSchemas();
        for (SchemaInfo schema : schemas) {
            try {
                System.out.println("----Schema -----");
                DOMUtils.writeXml(schema.getElement(), System.out);
                System.out.println("---------------");
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //serviceInfo.getTypeInfo().getSchemas().size();
        JavaModel jmodel = wsdlDefinitionToJavaModel(serviceInfo);

        if (jmodel == null) {
            Message msg = new Message("FAIL_TO_CREATE_JAVA_MODEL", LOG);
            throw new ToolException(msg);
        }
    }

    private JavaModel wsdlDefinitionToJavaModel(ServiceInfo serviceInfo) throws ToolException {
        JavaModel javaModel = new JavaModel();
        context.put(JavaModel.class, javaModel);


        //javaModel.setJAXWSBinding(customizing(definition));

        // TODO refactroing the internal processors to use the service model

        PortTypeProcessor portTypeProcessor = new PortTypeProcessor(context);
        portTypeProcessor.process(serviceInfo);

        ServiceProcessor serviceProcessor = new ServiceProcessor(context);
        serviceProcessor.process(serviceInfo);


        //         SEIAnnotationProcessor seiAnnotationProcessor = new SEIAnnotationProcessor(context);
        //         seiAnnotationProcessor.process(serviceInfo);

        JavaInterface intf = javaModel.getInterfaces().values().iterator().next();
        new WebServiceAnnotator().annotate(intf);
        if (serviceInfo.getBindings().size() > 0) {
            new BindingAnnotator().annotate(intf);
        }


        return javaModel;
    }
}
