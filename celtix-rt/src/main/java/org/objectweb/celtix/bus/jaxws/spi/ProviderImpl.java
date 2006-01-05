package org.objectweb.celtix.bus.jaxws.spi;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.common.logging.LogUtils;

public class ProviderImpl extends javax.xml.ws.spi.Provider {
    public static final String JAXWS_PROVIDER = ProviderImpl.class.getName();
    
    private static final Logger LOG = LogUtils.getL7dLogger(ProviderImpl.class);

    @Override
    public ServiceDelegate createServiceDelegate(URL url,
                                                 QName qname,
                                                 Class cls) {
        return new ServiceImpl(Bus.getCurrent(), url, qname, cls);
    }

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {
        Endpoint ep = null;
        if (EndpointUtils.isValidImplementor(implementor)) {
            try {
                ep = new EndpointImpl(Bus.getCurrent(), implementor, bindingId);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "ENDPOINT_CREATION_FAILED_MSG", ex);
            }
            return ep;
        }
        LOG.severe("INVALID_IMPLEMENTOR_MSG");
        return null;
    }

    @Override
    public Endpoint createAndPublishEndpoint(String url, Object implementor) {
        Endpoint ep = createEndpoint(null, implementor);
        ep.publish(url);
        return ep;
    }

}
