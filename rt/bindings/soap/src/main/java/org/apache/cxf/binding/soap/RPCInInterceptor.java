package org.apache.cxf.binding.soap;

import org.apache.cxf.interceptor.WrappedInInterceptor;

public class RPCInInterceptor extends WrappedInInterceptor {
//        
//    private BindingOperationInfo getOperation(SoapMessage message, DepthXMLStreamReader xmlReader) {
//        if (!StaxUtils.toNextElement(xmlReader)) {
//            message.setContent(Exception.class,
//                               new RuntimeException("There must be a method name element."));
//        }
//
//        String opName = xmlReader.getLocalName();
//        if (!isInboundMessage(message) && opName.endsWith("Response")) {
//            opName = opName.substring(0, opName.length() - 8);
//        }
//
//        BindingOperationInfo operation = ServiceModelUtil.getOperation(message,
//                                                                     new QName(xmlReader.getNamespaceURI(),
//                                                                                 opName));
//        if (operation == null) {
//            message.setContent(Exception.class,
//                               new RuntimeException("Could not find operation:" + opName));
//        }
//        return operation;
//    }
//
//    public void handleMessage(Message message) {
//        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
//        
//        BindingOperationInfo operation = null;
//        if (!isOperationResolved(message)) {
//            operation = getOperation(message, xmlReader);
//            // Store operation into the message.
//            message.put(Message.INVOCATION_OPERATION, operation.getName().getLocalPart());
//        }
//
//        MessageInfo msg;
//        DataReader<XMLStreamReader> dr = getDataReader(message,
//                                                       operation.getOperationInfo());
//         
//        if (isInboundMessage(message)) {
//            msg = operation.getInput().getMessageInfo();
//        } else {
//            msg = operation.getOutput().getMessageInfo();
//        }
//
//        List<Object> parameters = new ArrayList<Object>();
//
//        StaxUtils.nextEvent(xmlReader);
//        while (StaxUtils.toNextElement(xmlReader)) {
//            int idx = parameters.size();
//            MessagePartInfo p = msg.getMessageParts().get(idx);
//            if (p == null) {
//                message.setContent(Exception.class,
//                                   new RuntimeException("Parameter "
//                                                        + xmlReader.getName()
//                                                        + " does not exist!"));
//            }
//            QName name = xmlReader.getName();
//            QName elName = ServiceModelUtil.getRPCPartName(p);
//
//            if (!elName.getLocalPart().equals(name.getLocalPart())) {
//                String expMessage = "Parameter "
//                    + name
//                    + " does not equal to the name in the servicemodel!";
//                message.setContent(Exception.class, new RuntimeException(expMessage));
//            }
//            parameters.add(dr.read(elName,
//                                   xmlReader,
//                                   getParameterTypeClass(message, idx)));
//        }
//        
//        message.put(Message.INVOCATION_OBJECTS, parameters);
//    }
}

