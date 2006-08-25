package org.objectweb.celtix.systest.ws.rm;

import javax.xml.namespace.QName;

import org.objectweb.celtix.bus.busimpl.BusConfigurationBuilder;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.bus.jaxws.configuration.types.SystemHandlerChainType;
import org.objectweb.celtix.bus.ws.addressing.MAPAggregator;
import org.objectweb.celtix.bus.ws.addressing.soap.MAPCodec;
import org.objectweb.celtix.bus.ws.rm.RMHandler;
import org.objectweb.celtix.bus.ws.rm.soap.PersistenceHandler;
import org.objectweb.celtix.bus.ws.rm.soap.RMSoapHandler;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;

public class TestConfigurator {

    private static final String DEFAULT_BUS_ID = "celtix";

    private ConfigurationBuilder builder;

    public TestConfigurator() {
        builder = ConfigurationBuilderFactory.getBuilder();

    }

    public void configureClient(QName serviceName, String portName) {
        configureClient(DEFAULT_BUS_ID, serviceName, portName);
    }

    public void configureClient(String busId, QName serviceName, String portName) {
        Configuration portCfg = createPortConfiguration(busId, serviceName, portName);
        configureHandlers(portCfg, false);
    }

    public void configureServer(QName serviceName) {
        configureServer(DEFAULT_BUS_ID, serviceName);
    }

    public void configureServer(String busId, QName serviceName) {
        Configuration endpointCfg = createEndpointConfiguration(busId, serviceName);
        configureHandlers(endpointCfg, true);
    }
    
    private Configuration createEndpointConfiguration(String busId, QName serviceName) {
        Configuration busCfg = getBusConfiguration(busId);
        return builder.buildConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, 
                                                               serviceName.toString(), busCfg);
    }
    
    private Configuration createPortConfiguration(String busId, QName serviceName, String portName) {
        Configuration busCfg = getBusConfiguration(busId);
        String id = serviceName.toString() + "/" + portName;
        return builder.buildConfiguration(ServiceImpl.PORT_CONFIGURATION_URI, id, busCfg);
    }
    
    private Configuration getBusConfiguration(String busId) {
        Configuration busCfg = builder.getConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, busId);
        if (null == busCfg) {
            busCfg = builder.buildConfiguration(BusConfigurationBuilder.BUS_CONFIGURATION_URI, busId);
        }
        return busCfg;
    }

    private void configureHandlers(Configuration config, boolean isServer) {
        SystemHandlerChainType systemHandlers = config.getObject(SystemHandlerChainType.class,
                                                                 "systemHandlerChain");        

        org.objectweb.celtix.bus.jaxws.configuration.types.ObjectFactory factory
            = new org.objectweb.celtix.bus.jaxws.configuration.types.ObjectFactory();
        
        HandlerChainType handlerChain = null;
        HandlerType handler = null;
        
        if (null == systemHandlers) {
            systemHandlers = factory.createSystemHandlerChainType();

            boolean withRM = true;
            
            // pre-logical

            handlerChain = factory.createHandlerChainType();            
            handler = factory.createHandlerType();
            handler.setHandlerClass(MAPAggregator.class.getName());
            handler.setHandlerName("logical addressing handler");
            handlerChain.getHandler().add(handler);
            if (withRM) {
                handler = factory.createHandlerType();
                handler.setHandlerClass(RMHandler.class.getName());
                handler.setHandlerName("logical rm handler");
                handlerChain.getHandler().add(handler);
            }
            if (!isServer) {
                handler = factory.createHandlerType();
                handler.setHandlerClass(LogicalMessageContextRecorder.class.getName());
                handler.setHandlerName("logical message context recorder");
                handlerChain.getHandler().add(handler);
            }

            systemHandlers.setPreLogical(handlerChain);

            // post-protocol
            
            handlerChain = factory.createHandlerChainType();
            if (withRM) {
                handler = factory.createHandlerType();
                handler.setHandlerClass(RMSoapHandler.class.getName());
                handler.setHandlerName("protocol rm handler");
                handlerChain.getHandler().add(handler);
            }
           
            handler = factory.createHandlerType();
            handler.setHandlerClass(MAPCodec.class.getName());
            handler.setHandlerName("protocol addressing handler");
            handlerChain.getHandler().add(handler);
            
            boolean persist = true;
            if (persist) {
                handler = factory.createHandlerType();
                handler.setHandlerClass(PersistenceHandler.class.getName());
                handler.setHandlerName("protocol rm persistence handler");
                handlerChain.getHandler().add(handler);
            }

            systemHandlers.setPostProtocol(handlerChain);

            config.setObject("systemHandlerChain", systemHandlers);
        }
        
        handlerChain = config.getObject(HandlerChainType.class, "handlerChain");
        if (null == handlerChain && !isServer) {
            handlerChain = factory.createHandlerChainType();            
            handler = factory.createHandlerType();
            handler.setHandlerClass(SOAPMessageRecorder.class.getName());
            handler.setHandlerName("soap message recorder stream handler");
            handlerChain.getHandler().add(handler);
            
            config.setObject("handlerChain", handlerChain);
        }
        
        // create rm handler configuration 

        builder.buildConfiguration(RMHandler.RM_CONFIGURATION_URI,
                                   RMHandler.RM_CONFIGURATION_ID,
                                   config);        
    }
}
