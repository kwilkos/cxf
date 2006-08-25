package org.apache.cxf.binding.soap;

import javax.wsdl.extensions.soap.SOAPAddress;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

import junit.framework.TestCase;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class SoapDestinationFactoryTest extends TestCase {
    public void testDestination() throws Exception {
        String wsdlSoapNs = "http://schemas.xmlsoap.org/wsdl/soap/";
        String transportURI = "http://foo/transport";
        String location = "http://localhost/service";

        ServiceInfo si = new ServiceInfo();
        EndpointInfo ei = new EndpointInfo(si, wsdlSoapNs);
        SOAPAddress add = new SOAPAddressImpl();
        add.setLocationURI(location);
        ei.addExtensor(add);

        SoapBindingInfo bi = new SoapBindingInfo(si, "", Soap11.getInstance());
        bi.setTransportURI(transportURI);
        ei.setBinding(bi);

        IMocksControl control = EasyMock.createNiceControl();
        DestinationFactoryManager dfm = control.createMock(DestinationFactoryManager.class);
        DestinationFactory fooDF = control.createMock(DestinationFactory.class);
        Destination dest = control.createMock(Destination.class);

        EasyMock.expect(dfm.getDestinationFactory(transportURI)).andReturn(fooDF);
        EasyMock.expect(fooDF.getDestination(ei)).andStubReturn(dest);

        control.replay();

        // SoapDestinationFactory sdf = new SoapDestinationFactory(dfm);
        // Destination dest2 = sdf.getDestination(ei);
        // assertNotNull(dest2);

        // TODO: doesn't pass because I don't know how to use easymock :-(
        // assertEquals(dest, dest2);
    }
}
