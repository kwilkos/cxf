package org.objectweb.celtix.jbi.se.state;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jbi.se.CeltixServiceUnitManager;
import org.objectweb.celtix.jbi.transport.JBITransportFactory;

public abstract class AbstractServiceEngineStateMachine implements ServiceEngineStateMachine {

    static final String CELTIX_CONFIG_FILE = "celtix-config.xml";
    static final String PROVIDER_PROP = "javax.xml.ws.spi.Provider";
    static CeltixServiceUnitManager suManager;
    static ComponentContext ctx;
    static Bus bus;
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractServiceEngineStateMachine.class);
    private static final String JBI_TRANSPORT_ID = "http://celtix.object.org/transport/jbi";
    private static final String HTTP_TRANSPORT_ID = "http://schemas.xmlsoap.org/soap/http";
    private static final String HTTP_TRANSPORT_ID1 = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final String JMS_TRANSPORT_ID = "http://celtix.objectweb.org/transports/jms";

    public void changeState(SEOperation operation, ComponentContext context) throws JBIException {
        
    }

    void configureJBITransportFactory(DeliveryChannel chnl, CeltixServiceUnitManager mgr)
        throws BusException { 
        getTransportFactory().setDeliveryChannel(chnl);
    }


    JBITransportFactory getTransportFactory() throws BusException { 
        assert bus != null;
    
        try { 
            JBITransportFactory transportFactory = 
                (JBITransportFactory)bus.getTransportFactoryManager()
                    .getTransportFactory(JBI_TRANSPORT_ID);
            
            return transportFactory;
        } catch (BusException ex) { 
            LOG.log(Level.SEVERE, new Message("SE.FAILED.INIT.BUS", LOG).toString(), ex);
            throw ex;
        }
    }
    
    void registerJBITransport(Bus argBus, CeltixServiceUnitManager mgr) throws JBIException { 
        try { 
           
            getTransportFactory().init(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
            bus.getTransportFactoryManager().registerTransportFactory(
                JBI_TRANSPORT_ID, getTransportFactory());
            bus.getTransportFactoryManager().registerTransportFactory(
                HTTP_TRANSPORT_ID, getTransportFactory());
            bus.getTransportFactoryManager().registerTransportFactory(
                HTTP_TRANSPORT_ID1, getTransportFactory());
            bus.getTransportFactoryManager().registerTransportFactory(
                JMS_TRANSPORT_ID, getTransportFactory());
        } catch (Exception ex) {
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString(), ex);
        }
    } 
    
    public static CeltixServiceUnitManager getSUManager() {
        return suManager;
    }
    
}
