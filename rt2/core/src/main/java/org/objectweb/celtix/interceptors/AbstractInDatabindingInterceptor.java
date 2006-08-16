package org.objectweb.celtix.interceptors;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.databinding.DataReaderFactory;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceModelUtil;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;

public abstract class AbstractInDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils
        .getBundle(AbstractInDatabindingInterceptor.class);

    protected boolean isInboundMessage(Message message) {
        return message.containsKey(Message.INBOUND_MESSAGE);
    }

    protected DataReader<XMLStreamReader> getDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataReaderFactory();

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = factory.createReader(XMLStreamReader.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.objectweb.celtix.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                service.getName()));
        }
        return dataReader;
    }

    protected DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = message.getContent(XMLStreamReader.class);
        return new DepthXMLStreamReader(xr);
    }

    protected OperationInfo findOperation(Collection<OperationInfo> operations, List<Object> parameters) {
        // first check for exact matches
        for (OperationInfo o : operations) {
            List messageParts = o.getInput().getMessageParts();
            if (messageParts.size() == parameters.size()) {
                return o;
//                if (checkExactParameters(messageParts, parameters))
//                    return o;
            }
        }

//        // now check for assignable matches
//        for (OperationInfo o : operations) {
//            List messageParts = o.getInput().getMessageParts();
//            if (messageParts.size() == parameters.size()) {
//                if (checkParameters(messageParts, parameters))
//                    return o;
//            }
//        }
        return null;
    }
//
//    /**
//     * Return true only if the message parts exactly match the classes of the
//     * parameters
//     * 
//     * @param messageParts
//     * @param parameters
//     * @return
//     */
//    private boolean checkExactParameters(List messageParts, List parameters) {
//        Iterator messagePartIterator = messageParts.iterator();
//        for (Iterator parameterIterator = parameters.iterator(); parameterIterator.hasNext();) {
//            Object param = parameterIterator.next();
//            MessagePartInfo mpi = (MessagePartInfo)messagePartIterator.next();
//            if (!mpi.getTypeClass().equals(param.getClass())) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean checkParameters(List messageParts, List parameters) {
//        Iterator messagePartIterator = messageParts.iterator();
//        for (Iterator parameterIterator = parameters.iterator(); parameterIterator.hasNext();) {
//            Object param = parameterIterator.next();
//            MessagePartInfo mpi = (MessagePartInfo)messagePartIterator.next();
//
//            if (!mpi.getTypeClass().isAssignableFrom(param.getClass())) {
//                if (!param.getClass().isPrimitive() && mpi.getTypeClass().isPrimitive()) {
//                    return checkPrimitiveMatch(mpi.getTypeClass(), param.getClass());
//                } else {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean checkPrimitiveMatch(Class clazz, Class typeClass) {
//        if ((typeClass == Integer.class && clazz == int.class)
//            || (typeClass == Double.class && clazz == double.class)
//            || (typeClass == Long.class && clazz == long.class)
//            || (typeClass == Float.class && clazz == float.class)
//            || (typeClass == Short.class && clazz == short.class)
//            || (typeClass == Boolean.class && clazz == boolean.class)
//            || (typeClass == Byte.class && clazz == byte.class))
//            return true;
//
//        return false;
//    }
}
