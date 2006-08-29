package org.apache.cxf.binding.xml.interceptor;

import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public class XMLMessageInInterceptor extends AbstractPhaseInterceptor {

    public void handleMessage(Message message) throws Fault {
        new WrappedInInterceptor().handleMessage(message);
        new BareInInterceptor().handleMessage(message);        
    }

}
