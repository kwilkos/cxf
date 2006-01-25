package org.objectweb.celtix.bus.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.handlers.HandlerChainBuilder;

public class HandlerResolverImpl implements HandlerResolver {

    private final Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();
    private Configuration serviceConfiguration;

    public HandlerResolverImpl(Configuration c) {
        serviceConfiguration = c;
    }

    public HandlerResolverImpl() {
        this(null);
    }

    public List<Handler> getHandlerChain(PortInfo portInfo) {

        List<Handler> handlerChain = handlerMap.get(portInfo);
        if (handlerChain == null) {
            handlerChain = createHandlerChain(portInfo);
            handlerMap.put(portInfo, handlerChain);
        }
        return handlerChain;
    }

    private List<Handler> createHandlerChain(PortInfo portInfo) {

        Configuration portConfiguration = null;
        List<Handler> chain = null;
        if (null != serviceConfiguration) {
            portConfiguration = serviceConfiguration
                .getChild("http://celtix.objectweb.org/bus/jaxws/port-config", portInfo.getPortName()
                    .getLocalPart());
        }
        if (null != portConfiguration) {
            HandlerChainBuilder builder = new HandlerChainBuilder();
            HandlerChainType hc = (HandlerChainType)portConfiguration.getObject("handlerChain");
            chain = builder.buildHandlerChainFromConfiguration(hc);
        }
        if (null == chain) {
            chain = new ArrayList<Handler>();
        }
        return chain;
    }
}
