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
package org.apache.cxf.jaxws.interceptors;

import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;

public class SwAInInterceptor extends AbstractSoapInterceptor {

    public SwAInInterceptor() {
        super();
        setPhase(Phase.PRE_INVOKE);
        getBefore().add(HolderInInterceptor.class.getName());
    }

    public void handleMessage(SoapMessage message) throws Fault {
        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        if (bop == null) {
            return;
        }
        
        if (bop.isUnwrapped()) {
            bop = bop.getWrappedOperation();
        }
        
        boolean client = isRequestor(message);
        BindingMessageInfo bmi = client ? bop.getOutput() : bop.getInput();
        
        if (bmi == null) {
            return;
        }
        
        SoapBodyInfo sbi = bmi.getExtensor(SoapBodyInfo.class);
        
        if (sbi == null || sbi.getAttachments() == null || sbi.getAttachments().size() == 0) {
            return;
        }
        
        List<Object> inObjects = CastUtils.cast(message.getContent(List.class));

        for (MessagePartInfo mpi : sbi.getAttachments()) {
            String partName = mpi.getConcreteName().getLocalPart();
            
            String start = partName + "=";
            for (Attachment a : message.getAttachments()) {
                if (a.getId().startsWith(start)) {
//                    String ct = (String) mpi.getProperty(Message.CONTENT_TYPE);
//
//                    System.out.println("Content type " + ct);
//                    Object content = null;
//                    try {
//                        DataFlavor flavor = new DataFlavor(ct);
//                        content = a.getDataHandler().getTransferData(flavor);
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (UnsupportedFlavorException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (ClassNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                    System.out.println("Content " + content);
                    inObjects.add(a.getDataHandler());
                }
            }
        }
    }
}
