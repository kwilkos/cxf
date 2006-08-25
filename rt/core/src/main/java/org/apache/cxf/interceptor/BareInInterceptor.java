package org.apache.cxf.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

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

        message.setContent(List.class, parameters);
    }
}
