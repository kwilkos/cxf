package org.apache.cxf.message;

import java.util.Iterator;

import javax.activation.DataHandler;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public interface Attachment {
    DataHandler getDataHandler();

    /**
     * @return The attachment id.
     */
    String getId();
    
    String getHeader(String name);
    
    Iterator<String> getHeaderNames();
    
    /**
     * Whether or not this is an XOP package. This will affect the 
     * serialization of the attachment. If true, it will be serialized
     * as binary data, and not Base64Binary.
     * 
     * @return
     */
    boolean isXOP();
}
