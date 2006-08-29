package org.apache.cxf.binding.xml.interceptor;

import org.apache.cxf.interceptor.BareOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public class XMLMessageOutInterceptor extends AbstractPhaseInterceptor {

    public void handleMessage(Message message) throws Fault {
        new WrappedOutInterceptor().handleMessage(message);
        new BareOutInterceptor().handleFault(message);

    }

}
