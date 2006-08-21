package org.objectweb.celtix.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.objectweb.celtix.databinding.DataReader;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;

public class BareInInterceptor extends AbstractInDatabindingInterceptor {

    public BareInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) {
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
        Exchange exchange = message.getExchange();

        BindingOperationInfo operation = exchange.get(BindingOperationInfo.class);

        DataReader<XMLStreamReader> dr = getDataReader(message);
        List<Object> parameters = new ArrayList<Object>();

        StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            parameters.add(dr.read(xmlReader));
        }

        // If we didn't know the operation going into this, lets try to figure
        // it out
        if (operation == null) {
            Endpoint ep = exchange.get(Endpoint.class);
            Service service = ep.getService();

            OperationInfo op = findOperation(service.getServiceInfo().getInterface().getOperations(),
                                             parameters);

            for (BindingOperationInfo bop : ep.getEndpointInfo().getBinding().getOperations()) {
                if (bop.getOperationInfo().equals(op)) {
                    exchange.put(BindingOperationInfo.class, bop);
                    break;
                }
            }
        }

        message.setContent(Object.class, parameters);
    }
}
