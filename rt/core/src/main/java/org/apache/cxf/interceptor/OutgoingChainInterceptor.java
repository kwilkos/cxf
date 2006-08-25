package org.apache.cxf.interceptor;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;

public class OutgoingChainInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutgoingChainInterceptor() {
        super();
        setPhase(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Exchange ex = message.getExchange();
        Message out = ex.getOutMessage();
        
        BindingOperationInfo bin = ex.get(BindingOperationInfo.class);
        out.put(MessageInfo.class, bin.getOperationInfo().getOutput());
        out.put(BindingMessageInfo.class, bin.getOutput());
        out.getInterceptorChain().doIntercept(out);
    }
}
