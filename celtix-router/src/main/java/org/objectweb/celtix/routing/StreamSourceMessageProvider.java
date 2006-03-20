package org.objectweb.celtix.routing;
//import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class StreamSourceMessageProvider implements Provider<StreamSource> {
    private final Definition wsdlModel;    
    private final RouteType route;
    private final URL wsdlLocation;
    private boolean doInit;
    
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

    public StreamSource invoke(StreamSource request) {
        //StreamSource response = new StreamSource();
        return null;
    }
    
    protected synchronized void init() {
        if (doInit) {
            List<DestinationType> dtList = route.getDestination();

            for (DestinationType dt : dtList) {
                createService(wsdlLocation, dt.getService());
                createProxy();
                doInit = false;
            }
        }
    }
    
    protected void createService(URL wsdlUrl, QName serviceName) {
        //TODO
    }
    
    protected void createProxy() {
        
    }
}
