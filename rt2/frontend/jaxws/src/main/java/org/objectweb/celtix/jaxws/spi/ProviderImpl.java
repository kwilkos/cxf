package org.objectweb.celtix.jaxws.spi;

import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxws.EndpointImpl;
import org.objectweb.celtix.jaxws.EndpointUtils;
import org.objectweb.celtix.jaxws.ServiceImpl;

public class ProviderImpl extends javax.xml.ws.spi.Provider {
    public static final String JAXWS_PROVIDER = ProviderImpl.class.getName();
    
    private static final Logger LOG = LogUtils.getL7dLogger(ProviderImpl.class);
    
    // TODO: use bus factory instead
    
    private static Bus currentBus;

    @Override
    public ServiceDelegate createServiceDelegate(URL url,
                                                 QName qname,
                                                 Class cls) {
        return new ServiceImpl(getCurrentBus(), url, qname, cls);
    }

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {

        Endpoint ep = null;
        if (EndpointUtils.isValidImplementor(implementor)) {
            ep = new EndpointImpl(getCurrentBus(), implementor, bindingId);
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
    
    private Bus getCurrentBus() {
        if (currentBus == null) {
            currentBus = new CeltixBus();
        }
        return currentBus;
    }

}
