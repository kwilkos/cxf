package org.apache.cxf.jaxws.spi;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CeltixBusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.EndpointUtils;
import org.apache.cxf.jaxws.ServiceImpl;

public class ProviderImpl extends javax.xml.ws.spi.Provider {
    public static final String JAXWS_PROVIDER = ProviderImpl.class.getName();
    
    private static final Logger LOG = LogUtils.getL7dLogger(ProviderImpl.class);

    @Override
    public ServiceDelegate createServiceDelegate(URL url,
                                                 QName qname,
                                                 Class cls) {
        Bus bus = new CeltixBusFactory().getDefaultBus();
        return new ServiceImpl(bus, url, qname, cls);
    }

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {

        Endpoint ep = null;
        if (EndpointUtils.isValidImplementor(implementor)) {
            Bus bus = new CeltixBusFactory().getDefaultBus();
            ep = new EndpointImpl(bus, implementor, bindingId);
            return ep;
        } else {
            throw new WebServiceException(new Message("INVALID_IMPLEMENTOR_EXC", LOG).toString());
        }
    }

    @Override
    public Endpoint createAndPublishEndpoint(String url, Object implementor) {
        Endpoint ep = createEndpoint(null, implementor);
        ep.publish(url);
        return ep;
    }

}
