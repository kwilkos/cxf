package org.objectweb.celtix.context;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.ws.handler.MessageContext;

/**
 * A MessageContext that provide access to the underlying streams
 * involved in a message exchange
 */
public interface StreamMessageContext extends MessageContext {

    /**
     * Get the underlying InputStream from which the incoming message
     * can be read
     *
     * @return the InputStream or null if there is no InputStream
     * available (for example on the outbound leg of a message
     * exchange.
     */
    InputStream getInputStream(); 

    
    /**
     * Set the InputStream to read the message from.  Can be used to
     * replace the InputStream from which the message is read.  This
     * can be used to carry out some sort of transformation on the
     * data before it is sent over the network.
     *
     */
    void setInputStream(InputStream in); 

    /**
     * Get the underlying OutputStream from which the outgoing message
     * can be read
     *
     * @return the OutputStream or null if there is no OutputStream
     * available (for example on the inbound leg of a message
     * exchange.
     */
    OutputStream getOutputStream(); 

    /**
     * Set the OutputStream to write the message to.  Can be used to
     * replace the OutputStream to which the message is writetn.  This
     * can be used to carry out some sort of transformation on the
     * data as it is being read from the network.
     *
     */
    void setOutputStream(OutputStream in); 
}
