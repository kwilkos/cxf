package org.objectweb.celtix.transports.jms;

import java.io.InputStream;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;

public  class JMSInputStreamContext extends GenericMessageContext implements InputStreamMessageContext {
    
    protected InputStream inStream;
    
    public JMSInputStreamContext(InputStream ins) {
        inStream = ins;
    }

    public InputStream getInputStream() {
        return inStream;
    }

    public void setInputStream(InputStream ins) {
        inStream = ins;
    }
    
    public void setFault(boolean isFault) {
        
    }
    
    public boolean isFault() {
        return  false;
    }

}
