package org.objectweb.celtix.bindings.soap2;

import org.objectweb.celtix.jaxb.attachments.AttachmentDeserializer;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;

public class MultipartMessageInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String ATTACHMENT_DIRECTORY = "attachment-directory";
    public static final String ATTACHMENT_MEMORY_THRESHOLD = "attachment-memory-threshold";
    public static final int THRESHHOLD = 1024 * 100;

    /**
     * contruct the soap message with attachments from mime input stream
     * 
     * @param messageParam
     */
    public void handleMessage(Message message) {
        
        AttachmentDeserializer ad = new AttachmentDeserializer(message);
        if (ad.preprocessMessage()) {
            message.put(Message.ATTACHMENT_DESERIALIZER, ad);
        }
    }

    public void handleFault(Message messageParam) {
    }

}
