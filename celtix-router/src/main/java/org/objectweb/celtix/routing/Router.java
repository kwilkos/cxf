package org.objectweb.celtix.routing;

import javax.wsdl.Definition;
import org.objectweb.celtix.routing.configuration.RouteType;


public interface Router {

    Definition getWSDLModel();

    RouteType getRoute();

    void init();

    void publish();
}
