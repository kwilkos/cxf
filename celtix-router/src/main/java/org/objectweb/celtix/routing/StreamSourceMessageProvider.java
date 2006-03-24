package org.objectweb.celtix.routing;
//import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
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
    protected List<Dispatch<StreamSource>> dList;
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
        if (doInit) {
            init();
        }
        //TODO Set Request/Response Context like Transport Attributes.
        //TODO Use Async API        
        return dList.get(0).invoke(request);
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
}
