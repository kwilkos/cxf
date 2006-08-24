package org.apache.cxf.jaxws.interceptors;

import java.util.List;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptors.Fault;
import org.apache.cxf.jaxb.WrapperHelper;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;



public class WrapperClassInInterceptor extends AbstractPhaseInterceptor<Message> {

    public WrapperClassInInterceptor() {
        super();
        setPhase(Phase.PRE_LOGICAL);
    }

    public void handleMessage(Message message) throws Fault {
        BindingOperationInfo boi = message.getExchange().get(BindingOperationInfo.class);
        if (boi.isUnwrappedCapable()) {
            BindingOperationInfo boi2 = boi.getUnwrappedOperation();
            OperationInfo op = boi2.getOperationInfo();
            MessageInfo messageInfo = message.get(MessageInfo.class);
            BindingMessageInfo bmi;
            if (messageInfo == boi.getOperationInfo().getInput()) {
                messageInfo = op.getInput();
                bmi = boi2.getInput();
            } else {
                messageInfo = op.getOutput();
                bmi = boi2.getOutput();
            }
            
            message.put(MessageInfo.class, messageInfo);
            message.put(BindingMessageInfo.class, bmi);
            message.getExchange().put(BindingOperationInfo.class, boi2);
            message.getExchange().put(OperationInfo.class, op);
            
            List<?> lst = message.getContent(List.class);
            if (lst.size() == 1) {
                if (messageInfo.getMessageParts().size() > 0) {
                    Object wrappedObject = lst.get(0);
                    lst.clear();
                    
                    for (MessagePartInfo part : messageInfo.getMessageParts()) {
                        try {
                            Object obj = WrapperHelper.getWrappedPart(part.getName().getLocalPart(),
                                                                  wrappedObject);
                        
                            CastUtils.cast(lst, Object.class).add(obj);
                        } catch (Exception e) {
                            //TODO - fault
                            throw new Fault(e);
                        }
                    }
                } else {
                    lst.clear();
                }
            }           
        }
    }

    
}
