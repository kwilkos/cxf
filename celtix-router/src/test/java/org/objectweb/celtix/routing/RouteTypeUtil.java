package org.objectweb.celtix.routing;

import java.util.List;

import javax.xml.namespace.QName;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;

public final class RouteTypeUtil {
    private RouteTypeUtil() {
        //Complete
    }

    public static RouteType createRouteType(String routeName,
                                      QName srcService, String srcPort,
                                      QName destService, String destPort) {
        SourceType st = new SourceType();
        st.setService(srcService);
        st.setPort(srcPort);

        DestinationType dt = new DestinationType();
        dt.setPort(destPort);
        dt.setService(destService);

        RouteType rt = new RouteType();
        rt.setName(routeName);

        List<SourceType> sList = rt.getSource();
        sList.add(st);
        List<DestinationType> dList = rt.getDestination();
        dList.add(dt);

        return rt;
    }
}
