package org.apache.cxf.binding.xml;

import org.apache.cxf.message.AbstractWrappedMessage;
import org.apache.cxf.message.Message;

public class XMLMessage extends AbstractWrappedMessage {

    public XMLMessage(Message message) {
        super(message);
    }
}
