package org.apache.cxf.bindings.soap2;

import org.apache.cxf.jaxb.attachments.AttachmentDeserializer;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MultipartMessageInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String ATTACHMENT_DIRECTORY = "attachment-directory";
    public static final String ATTACHMENT_MEMORY_THRESHOLD = "attachment-memory-threshold";
    public static final int THRESHHOLD = 1024 * 100;

    /**
     * contruct the soap message with attachments from mime input stream
     * 
     * @param messageParam
     */
    
    public MultipartMessageInterceptor() {
        super();
        setPhase(Phase.RECEIVE);
    }
    
    public void handleMessage(Message message) {
        
        AttachmentDeserializer ad = new AttachmentDeserializer(message);
        if (ad.preprocessMessage()) {
            message.put(Message.ATTACHMENT_DESERIALIZER, ad);
        }
    }

    public void handleFault(Message messageParam) {
    }

}
