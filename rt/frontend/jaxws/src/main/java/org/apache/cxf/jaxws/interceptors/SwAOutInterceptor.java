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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.util.DataSourceSource;

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
    private static final Logger LOG = Logger.getLogger(SwAOutInterceptor.class.getName());
    
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
            final String ct = (String) mpi.getProperty(Message.CONTENT_TYPE);
            
            String id = new StringBuilder().append(partName)
                .append("=")
                .append(UUID.randomUUID())
                .append("@apache.org").toString();
            
            Object o = outObjects.remove(bodyParts);
            if (o == null) {
                continue;
            }
            
            DataHandler dh = null;
            
            if (o instanceof Source) {
                DataSource ds = null;
                
                if (o instanceof DataSourceSource) {
                    ds = (DataSource) o; 
                } else {
                    TransformerFactory tf = TransformerFactory.newInstance();
                    try {
                        Transformer transformer = tf.newTransformer();
                        
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        transformer.transform((Source) o, new StreamResult(bos));
                        ds = new ByteArrayDataSource(bos.toByteArray(), ct);
                    } catch (TransformerException e) {
                        throw new Fault(e);
                    } 
                }
                
                dh = new DataHandler(ds);
                
            } else if (o instanceof Image) {
                // TODO: make this streamable. This is one of my pet
                // peeves in JAXB RI as well, so if you fix this, submit the 
                // code to the JAXB RI as well (see RuntimeBuiltinLeafInfoImpl)! - DD
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(ct);
                if (writers.hasNext()) {
                    ImageWriter writer = writers.next();
                    
                    try {
                        BufferedImage bimg = convertToBufferedImage((Image) o);
                        writer.setOutput(ImageIO.createImageOutputStream(bos));
                        writer.write(bimg);
                        writer.dispose();
                        bos.close();
                    } catch (IOException e) {
                        throw new Fault(e);
                    }
                }
                
                dh = new DataHandler(new ByteArrayDataSource(bos.toByteArray(), ct));
            } else if (o instanceof DataHandler) {
                dh = (DataHandler) o;
            } else {
                throw new Fault(new org.apache.cxf.common.i18n.Message("ATTACHMENT_NOT_SUPPORTED", 
                                                                       LOG, o.getClass()));
            }
            
            AttachmentImpl att = new AttachmentImpl(id);
            att.setDataHandler(dh);
            att.setHeader("Content-Type", (String)mpi.getProperty(Message.CONTENT_TYPE));
            atts.add(att);
        }
    }

    private BufferedImage convertToBufferedImage(Image image) throws IOException {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        } else {
            MediaTracker tracker = new MediaTracker(new Component() { });
            tracker.addImage(image, 0);
            try {
                tracker.waitForAll();
            } catch (InterruptedException e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
            BufferedImage bufImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics g = bufImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            return bufImage;
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
