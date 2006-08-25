package org.objectweb.celtix.interceptors;

import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;

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
