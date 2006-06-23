package org.objectweb.celtix.js.rhino;

import java.io.File;

import javax.xml.ws.Service;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;

import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;


public class ProviderFactoryTest extends TestCase {

    private String epAddr = "http://celtix.objectweb.org/";

    private ProviderFactory ph;
    private AbstractDOMProvider dpMock;

    protected void setUp() throws Exception {
        super.setUp();
        dpMock = EasyMock.createMock(AbstractDOMProvider.class);
        ph = new ProviderFactory(epAddr) {
                protected AbstractDOMProvider createProvider(Service.Mode mode,
                                                             Scriptable scope,
                                                             Scriptable wspVar,
                                                             String epAddress,
                                                             boolean isBase,
                                                             boolean e4x)
                    throws Exception {
                    return dpMock;
                }
            };
    }

    public void testMsgJSFile() throws Exception {
        dpMock.publish();
        dpMock.publish();
        EasyMock.replay(dpMock);
        File f = new File(getClass().getResource("msg.js").getFile());
        ph.createAndPublish(f);
        EasyMock.verify(dpMock);
    }

    public void testBadJSFile() throws Exception {
        EasyMock.replay(dpMock);
        final String fname = "broken.js";
        File f = new File(getClass().getResource(fname).getFile());
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (EvaluatorException ex) {
            assertTrue("wrong exception", ex.getMessage().startsWith("syntax error"));
        }
        EasyMock.verify(dpMock);
    }

    public void testEmptyJSFile() throws Exception {
        EasyMock.replay(dpMock);
        final String fname = "empty.js";
        File f = new File(getClass().getResource(fname).getFile());
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message",
                         f.getPath() + ProviderFactory.NO_PROVIDER,
                         ex.getMessage());
        }
        EasyMock.verify(dpMock);
    }

    public void testNoSuchJSFile() throws Exception {
        EasyMock.replay(dpMock);
        final String fname = "none.js";
        File f = new File(fname);
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message",
                         f.getPath() + ProviderFactory.NO_SUCH_FILE,
                         ex.getMessage());
        }
        EasyMock.verify(dpMock);
    }

    public void testIllegalServiceMode() throws Exception {
        EasyMock.replay(dpMock);
        final String fname = "illegal1.js";
        File f = new File(getClass().getResource(fname).getFile());
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message",
                         f.getPath() + ProviderFactory.ILLEGAL_SVCMD_MODE + "bogus",
                         ex.getMessage());
        }
        EasyMock.verify(dpMock);
    }

    public void testIllegalServiceModeType() throws Exception {
        EasyMock.replay(dpMock);
        final String fname = "illegal2.js";
        File f = new File(getClass().getResource(fname).getFile());
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message",
                         f.getPath() + ProviderFactory.ILLEGAL_SVCMD_TYPE,
                         ex.getMessage());
        }
        EasyMock.verify(dpMock);
    }

    public void testProviderException() throws Exception {
        dpMock.publish();
        EasyMock.expectLastCall()
            .andThrow(new AbstractDOMProvider.JSDOMProviderException(AbstractDOMProvider.NO_EP_ADDR));
        EasyMock.replay(dpMock);
        File f = new File(getClass().getResource("msg.js").getFile());
        try {
            ph.createAndPublish(f);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message",
                         f.getPath() + ": " + AbstractDOMProvider.NO_EP_ADDR,
                         ex.getMessage());
        }
        EasyMock.verify(dpMock);
    }
}
