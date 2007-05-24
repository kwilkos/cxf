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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;

public class SwAOutInterceptor extends AbstractSoapInterceptor {

    public SwAOutInterceptor() {
        super();
        addAfter(HolderOutInterceptor.class.getName());
        addBefore(WrapperClassOutInterceptor.class.getName());
        setPhase(Phase.PRE_LOGICAL);
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
        BindingMessageInfo bmi = client ? bop.getInput() : bop.getOutput();
        
        if (bmi == null) {
            return;
        }
        
        SoapBodyInfo sbi = bmi.getExtensor(SoapBodyInfo.class);
        
        if (sbi == null || sbi.getAttachments() == null || sbi.getAttachments().size() == 0) {
            Service s = message.getExchange().get(Service.class);
            DataBinding db = s.getDataBinding();
            if (db instanceof JAXBDataBinding) {
                JAXBContext context = ((JAXBDataBinding)db).getContext();
                if (context instanceof JAXBContextImpl) {
                    JAXBContextImpl riCtx = (JAXBContextImpl) context;
                    if (riCtx.hasSwaRef()) {
                        setupAttachmentOutput(message);
                    }
                }
            }
            return;
        }
        
        Collection<Attachment> atts = setupAttachmentOutput(message);

        List<Object> outObjects = CastUtils.cast(message.getContent(List.class));

        int bodyParts = sbi.getParts().size();
        for (MessagePartInfo mpi : sbi.getAttachments()) {
            String partName = mpi.getConcreteName().getLocalPart();
            
            String id = new StringBuilder().append(partName)
                .append("=")
                .append(UUID.randomUUID())
                .append("@apache.org").toString();
            
            Object o = outObjects.remove(bodyParts);
            
            AttachmentImpl att = new AttachmentImpl(id);
            att.setDataHandler((DataHandler) o);
            att.setHeader("Content-Type", (String)mpi.getProperty(Message.CONTENT_TYPE));
            atts.add(att);
        }
    }

    private Collection<Attachment> setupAttachmentOutput(SoapMessage message) {
        // We have attachments, so add the interceptor
        message.getInterceptorChain().add(new AttachmentOutInterceptor());
        // We should probably come up with another property for this
        message.put(AttachmentOutInterceptor.WRITE_ATTACHMENTS, Boolean.TRUE);
        
        
        Collection<Attachment> atts = message.getAttachments();
        if (atts == null) {
            atts = new ArrayList<Attachment>();
            message.setAttachments(atts);
        }
        return atts;
    }
}
