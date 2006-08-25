package org.objectweb.celtix.jca.celtix;

import java.net.MalformedURLException;
import java.net.URL;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.hello_world_soap_http.Greeter;



public abstract class ManagedConnectionTestBase extends TestCase {
    protected Subject subj;

    protected CeltixConnectionRequestInfo cri;

    protected CeltixConnectionRequestInfo cri2;

    protected ManagedConnectionImpl mci;

    protected ManagedConnectionFactoryImpl factory = 
        EasyMock.createMock(ManagedConnectionFactoryImpl.class);

    protected Bus mockBus;
    //EasyMock.createMock(Bus.class);

    protected ConnectionEventListener mockListener = 
        EasyMock.createMock(ConnectionEventListener.class);
    
    public ManagedConnectionTestBase(String name) {
        super(name);
    }
    
    public void setUp() throws ResourceException, MalformedURLException, BusException {
               
        subj = new Subject();
        
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        
        QName serviceName2 = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService2");
        
        QName portName = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort");
        
        QName portName2 = new QName("http://objectweb.org/hello_world_soap_http", "SoapPort2");

        cri = new CeltixConnectionRequestInfo(Greeter.class, wsdl, serviceName, portName);

        cri2 = new CeltixConnectionRequestInfo(Greeter.class, wsdl, serviceName2, portName2);

        mockBus = Bus.init();
        
        EasyMock.reset(factory); 
        
        factory.getBus();
        
        EasyMock.expectLastCall().andReturn(mockBus).anyTimes();
        EasyMock.replay(factory);
        
        
        mci = new ManagedConnectionImpl(factory, cri, subj);        
      
        
        mci.addConnectionEventListener(mockListener);
    }
    
}
