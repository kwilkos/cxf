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

package org.apache.cxf.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;

public class BareOutInterceptor extends AbstractOutDatabindingInterceptor {

    public BareOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {      
        Exchange exchange = message.getExchange();
        BindingOperationInfo operation = (BindingOperationInfo)exchange.get(BindingOperationInfo.class
            .getName());
        
        if (operation == null) {
            return;
        }
        
        DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message);

        int countParts = 0;
        List<MessagePartInfo> parts = null;
        if (!isRequestor(message)) {
            parts = operation.getOutput().getMessageInfo().getMessageParts();
        } else {
            parts = operation.getInput().getMessageInfo().getMessageParts();
        }
        countParts = parts.size();

        if (countParts > 0) {
            List<?> objs = message.getContent(List.class);
            Object[] args = objs.toArray();
            Object[] els = parts.toArray();

            if (args.length != els.length) {
                int holder = 0;
                if (args.length > els.length) {
                    //detect Holder in params
                    Method method = message.getContent(Method.class);
                    holder = checkHolder(method);
                }
                if ((args.length - holder) != els.length) {
                    message.setContent(Exception.class,
                                   new RuntimeException("The number of arguments is not equal!"));
                }
            }
            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
            for (int idx = 0; idx < countParts; idx++) {
                Object arg = args[idx];
                MessagePartInfo part = (MessagePartInfo)els[idx];
                if (part.isInSoapHeader()) {
                    //this part should be in header, should donot write to soap body
                    continue;
                }
                QName elName = ServiceModelUtil.getPartName(part);
                dataWriter.write(arg, elName, xmlWriter);
            }
        }
    }
    
    private int checkHolder(Method method) {
        int holder = 0;
        if (method != null) {
            
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < paramAnnotations.length; i++) {
                Annotation[] annotation = paramAnnotations[i];
                for (int j = 0; j < annotation.length; j++) {
                    if (annotation[j] instanceof WebParam 
                        && (((WebParam)annotation[j]).mode().equals(WebParam.Mode.OUT)
                            || ((WebParam)annotation[j]).mode().equals(WebParam.Mode.INOUT))) {
                        holder++;
                    }
                }
            }
        }
        return holder;
    }       
}
