package org.objectweb.celtix.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.databinding.DataWriter;
import org.objectweb.celtix.jaxb.WrapperHelper;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public class WrappedOutInterceptor extends AbstractOutDatabindingInterceptor {
    public static final String SINGLE_WRAPPED_PART = "single.wrapped.out.part";

    public WrappedOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
        DataWriter<XMLStreamWriter> dataWriter = getDataWriter(message);

        // This is definitely the wrong way to do unwrapping. We need to find
        // a better way for the frontend to interact with the interceptors, but
        // in the interest of getting things working I'm leaving this for now.

        List<?> objs = message.getContent(List.class);

        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);
        OperationInfo op = bop.getOperationInfo();

        Class wrapped = (Class)op.getProperty(SINGLE_WRAPPED_PART);
        Object wrapperType = null;
        try {
            wrapperType = wrapped.newInstance();

            OperationInfo uop = op.getUnwrappedOperation();
            if (wrapped != null) {
                int i = 0;
                for (MessagePartInfo p : uop.getOutput().getMessageParts()) {
                    Object part = objs.get(i);

                    WrapperHelper.setWrappedPart(p.getName().getLocalPart(), wrapperType, part);

                    i++;
                }

                dataWriter.write(wrapperType, xmlWriter);
            } else {
                if (objs != null && objs.size() > 0) {
                    dataWriter.write(objs.get(0), xmlWriter);
                }
            }

        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
