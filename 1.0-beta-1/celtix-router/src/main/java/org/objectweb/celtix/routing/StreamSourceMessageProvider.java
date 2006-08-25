package org.objectweb.celtix.routing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class StreamSourceMessageProvider implements Provider<StreamSource> {
    protected List<Dispatch<StreamSource>> dList;
    private final Definition wsdlModel;    
    private final RouteType route;
    private final URL wsdlLocation;
    private boolean doInit;

    /**
     * Injectable context.
     */
    @Resource
    private WebServiceContext wsCtx;
    
    public StreamSourceMessageProvider(Definition model, RouteType rt) {
        wsdlModel = model;
        route = rt;
        doInit = true;
        
        try {
            wsdlLocation = new URL(wsdlModel.getDocumentBaseURI());
        } catch (MalformedURLException mue) {
            throw new WebServiceException("Invalid wsdl url", mue);
        }
    }
    
    @Resource
    public void setContext(WebServiceContext ctx) { 
        wsCtx = ctx;
    }
    
    public StreamSource invoke(StreamSource request) {
        if (doInit) {
            init();
        }
        
        Dispatch<StreamSource> dispatch = dList.get(0);

        //TODO Set Request/Response Context like Transport Attributes.
        updateRequestContext(dispatch.getRequestContext());

        //TODO Use Async API
        StreamSource resp = dispatch.invoke(request);
        
        updateWebServiceContext(dispatch.getResponseContext());
        return resp;
    }
    
    protected synchronized void init() {
        if (doInit) {
            List<DestinationType> dtList = route.getDestination();
            if (null == dList) {
                dList = new ArrayList<Dispatch<StreamSource>>(dtList.size());
            }

            for (DestinationType dt : dtList) {
                Service dtService = createService(wsdlLocation, dt.getService());
                Dispatch<StreamSource> streamDispatch = createDispatch(dtService, dt.getPort());
                dList.add(streamDispatch);
            }
            doInit = false;
        }
    }
    
    protected Service createService(URL wsdlUrl, QName serviceName) {
        //TODO Set Executor used by the Source Endpoint onto Service
        //Currently destination service uses bus workqueue.
        return Service.create(wsdlUrl, serviceName);
    }
    
    protected Dispatch<StreamSource> createDispatch(Service destService, String portName) {
        QName port = new QName(destService.getServiceName().getNamespaceURI(), portName);
        //Dispatch<StreamSource> d = 
        return destService.createDispatch(port, 
                                      StreamSource.class, 
                                      Service.Mode.MESSAGE);
    }
    
    private void updateRequestContext(Map<String, Object> reqCtx) {
        MessageContext sourceMsgCtx = wsCtx.getMessageContext();
        reqCtx.put(BindingProvider.USERNAME_PROPERTY, 
                   sourceMsgCtx.get(BindingProvider.USERNAME_PROPERTY));
        reqCtx.put(BindingProvider.PASSWORD_PROPERTY, 
                   sourceMsgCtx.get(BindingProvider.PASSWORD_PROPERTY));        
    }
    
    private void updateWebServiceContext(Map<String, Object> respCtx) {
        //TODO
    }
    
}
