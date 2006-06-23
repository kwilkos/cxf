package org.objectweb.celtix.js.rhino;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;

import org.mozilla.javascript.Scriptable;


public class AbstractDOMProviderTest extends TestCase {

    private String epAddr = "http://celtix.objectweb.org/";

    private Scriptable scriptMock;

    protected void setUp() throws Exception {
        super.setUp();
        scriptMock = EasyMock.createMock(Scriptable.class);
    }

    public void testNoWsdlLocation() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMMessageProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_WSDL_LOCATION, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testNoSvcName() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMPayloadProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_SERVICE_NAME, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testNoPortName() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("portName", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMMessageProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_PORT_NAME, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testNoTgtNamespace() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("portName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("targetNamespace", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMMessageProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_TGT_NAMESPACE, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testNoAddr() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("portName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("targetNamespace", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("EndpointAddress", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMPayloadProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_EP_ADDR, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testNoInvoke() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("portName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("targetNamespace", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("EndpointAddress", scriptMock))
            .andReturn(epAddr);
        EasyMock.expect(scriptMock.get("BindingType", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.expect(scriptMock.get("invoke", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMPayloadProvider(scriptMock, scriptMock,
                                                         null, false, false);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.NO_INVOKE, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }

    public void testIllegalInvoke() throws Exception {
        EasyMock.expect(scriptMock.get("wsdlLocation", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("serviceName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("portName", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("targetNamespace", scriptMock))
            .andReturn("found");
        EasyMock.expect(scriptMock.get("BindingType", scriptMock))
            .andReturn(Scriptable.NOT_FOUND);
        EasyMock.expect(scriptMock.get("invoke", scriptMock))
            .andReturn("string");
        EasyMock.replay(scriptMock);
        AbstractDOMProvider adp = new DOMMessageProvider(scriptMock, scriptMock,
                                                         epAddr, true, true);
        try {
            adp.publish();
            fail("expected exception did not occur");
        } catch (AbstractDOMProvider.JSDOMProviderException ex) {
            assertEquals("wrong exception message",
                         AbstractDOMProvider.ILLEGAL_INVOKE_TYPE, ex.getMessage());
        }
        EasyMock.verify(scriptMock);
    }
}
