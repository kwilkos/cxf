package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public final class TestUtils {
    private TestUtils() {
    }
    
    public static org.objectweb.celtix.ws.addressing.EndpointReferenceType getEPR(String s) {
        return EndpointReferenceUtils.getEndpointReference("http://nada.nothing.nowhere.null/" + s);
    }
    
    public static org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType getOldEPR(String s) {
        return VersionTransformer.convert(getEPR(s));
    }

}
