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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.mapper;

import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaType;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal.ProcessorUtil;


public final class ParameterMapper {

    private ParameterMapper() {
    }
    
    public static JavaParameter map(MessagePartInfo part, JavaType.Style style, ToolContext context) {
        String name = ProcessorUtil.resolvePartName(part);
        String namespace = ProcessorUtil.resolvePartNamespace(part);
        String type = ProcessorUtil.resolvePartType(part, context);
        
        JavaParameter parameter = new JavaParameter(name, type, namespace);
        parameter.setPartName(part.getName().getLocalPart());
        parameter.setQName(ProcessorUtil.getElementName(part));
        
        parameter.setClassName(ProcessorUtil.getFullClzName(part, context, false));

        if (style == JavaType.Style.INOUT || style == JavaType.Style.OUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
            
            parameter.setHolderClass(ProcessorUtil.getFullClzName(part, context, true));
        }
        parameter.setStyle(style);
        return parameter;
    }

   
}
