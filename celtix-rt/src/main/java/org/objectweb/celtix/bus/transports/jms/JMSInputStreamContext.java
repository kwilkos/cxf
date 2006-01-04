package org.objectweb.celtix.bus.transports.jms;

import java.io.InputStream;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;

public  class JMSInputStreamContext extends GenericMessageContext implements InputStreamMessageContext {
    
    protected OutputStreamMessageContext matchingOSMsgCtx;
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
    
    public void setMatchingOutCtx(OutputStreamMessageContext os) {
        matchingOSMsgCtx = os;
    }
    
    public OutputStreamMessageContext getMatchingOutCtx() {
        return matchingOSMsgCtx;
    }
    
    public void setFault(boolean isFault) {
        
    }
    
    public boolean isFault() {
        return  false;
    }

}
