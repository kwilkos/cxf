package org.objectweb.celtix.jca.celtix;


import java.net.URL;
import javax.resource.spi.ConnectionRequestInfo;
import javax.xml.namespace.QName;


public class CeltixConnectionRequestInfo implements ConnectionRequestInfo {
    private Class iface;
    private URL wsdlLocation;
    private QName serviceName;
    private QName portName;

    public CeltixConnectionRequestInfo(Class aIface, URL aWsdlLocation, 
                                       QName aServiceName, QName aPortName) {
        this.iface = aIface;
        this.wsdlLocation = aWsdlLocation;
        this.serviceName = aServiceName;
        this.portName = aPortName;
    }

    public Class<?> getInterface() {
        return iface;
    }

    public URL getWsdlLocationUrl() {
        return wsdlLocation;
    }

    public QName getServiceQName() {
        return serviceName;
    }

    public QName getPortQName() {
        return portName;
    }



    public boolean equals(java.lang.Object other) {
        boolean ret = this == other; 

        if (!ret && other instanceof CeltixConnectionRequestInfo) {
            CeltixConnectionRequestInfo cri = (CeltixConnectionRequestInfo)other; 

            ret = areEquals(iface, cri.iface) && areEquals(wsdlLocation, cri.wsdlLocation)
                   && areEquals(serviceName, cri.serviceName) && areEquals(portName, cri.portName);
           
        }
        return ret;
    }
  
    public int hashCode() {
        return getInterface().hashCode() + (serviceName != null ? serviceName.hashCode() : 0);
    }  

    public String toString() {
        StringBuffer buf = new StringBuffer(256);

        buf.append(iface).append(":").append(portName).append(":").append(serviceName).append("@").append(
                wsdlLocation);

        return buf.toString();
    }


    private boolean areEquals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj1 == obj2; 
        } else {
            return obj1.equals(obj2);
        }            
    }
   
}
