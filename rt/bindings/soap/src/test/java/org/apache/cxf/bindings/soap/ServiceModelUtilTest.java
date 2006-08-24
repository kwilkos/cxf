package org.apache.cxf.bindings.soap;


import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class ServiceModelUtilTest extends TestCase {
    private static final String WSDL_PATH = "test-soap-header.wsdl";
    private Definition def;
    private Service service;
    private ServiceInfo serviceInfo;

    private IMocksControl control;
    private Bus bus;
    private BindingFactoryManager bindingFactoryManager;
    
    public void setUp() throws Exception {
        String wsdlUrl = getClass().getResource(WSDL_PATH).toString();
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        def = wsdlReader.readWSDL(wsdlUrl);

        WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);
        for (Service serv : CastUtils.cast(def.getServices().values(), Service.class)) {
            if (serv != null) {
                service = serv;
                break;
            }
        }

        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);

        EasyMock.expect(bus.getExtension(BindingFactoryManager.class)).andReturn(bindingFactoryManager);

        control.replay();
        serviceInfo = wsdlServiceBuilder.buildService(def, service);
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testGetSchema() throws Exception {
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();
        QName name = new QName(serviceInfo.getName().getNamespaceURI(), "inHeader");
        BindingOperationInfo inHeader = bindingInfo.getOperation(name);
        BindingMessageInfo input = inHeader.getInput();
        assertNotNull(input);
        assertEquals(input.getMessageInfo().getName().getLocalPart(), "inHeaderRequest");
        assertEquals(input.getMessageInfo().getName().getNamespaceURI(),
                     "http://org.apache.cxf/headers");
        assertEquals(input.getMessageInfo().getMessageParts().size(), 2);
        assertTrue(input.getMessageInfo().getMessageParts().get(0).isElement());
        assertEquals(
            input.getMessageInfo().getMessageParts().get(0).getElementQName().getLocalPart(), "inHeader");
        assertEquals(input.getMessageInfo().getMessageParts().get(0).getElementQName().getNamespaceURI(),
                     "http://org.apache.cxf/headers");
        
        assertTrue(input.getMessageInfo().getMessageParts().get(0).isElement());
        assertEquals(
            input.getMessageInfo().getMessageParts().get(1).getElementQName().getLocalPart(), "passenger");
        assertEquals(input.getMessageInfo().getMessageParts().get(1).getElementQName().getNamespaceURI(),
                     "http://mycompany.example.com/employees");
        assertTrue(input.getMessageInfo().getMessageParts().get(1).isElement());
        
        MessagePartInfo messagePartInfo = input.getMessageInfo().getMessageParts().get(0);
        SchemaInfo schemaInfo = ServiceModelUtil.getSchema(serviceInfo, messagePartInfo);
        assertEquals(schemaInfo.getNamespaceURI(), "http://org.apache.cxf/headers");
        
        messagePartInfo = input.getMessageInfo().getMessageParts().get(1);
        schemaInfo = ServiceModelUtil.getSchema(serviceInfo, messagePartInfo);
        assertEquals(schemaInfo.getNamespaceURI(), "http://mycompany.example.com/employees");
    }
}
