package org.objectweb.celtix.js.rhino;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;

public class ServerAppTest extends TestCase {

    private String epAddr = "http://celtix.objectweb.org/";

    private ProviderFactory phMock;
    private String emptyFile;

    protected void setUp() throws Exception {
        super.setUp();
        phMock = EasyMock.createMock(ProviderFactory.class);
        emptyFile = getClass().getResource("empty/empty.js").getFile();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private ServerApp createServerApp() {
        return new ServerApp() {
                protected ProviderFactory createProviderFactory() {
                    return phMock;
                }
            };
    }

    public void testNoArgs() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message", ServerApp.NO_FILES_ERR, ex.getMessage());
        }
        EasyMock.verify(phMock);
    }

    public void testUknownOption() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {"-x"};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith(ServerApp.UNKNOWN_OPTION));
        }
        EasyMock.verify(phMock);
    }

    public void testMissingOptionA() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {"-a"};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message", ServerApp.WRONG_ADDR_ERR, ex.getMessage());
        }
        EasyMock.verify(phMock);
    }

    public void testBrokenOptionA() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {"-a", "not-a-url"};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message", ServerApp.WRONG_ADDR_ERR, ex.getMessage());
        }
        EasyMock.verify(phMock);
    }

    public void testMissingOptionB() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {"-b"};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message", ServerApp.WRONG_BASE_ERR, ex.getMessage());
        }
        EasyMock.verify(phMock);
    }

    public void testBrokenOptionB() {
        EasyMock.replay(phMock);
        try {
            ServerApp app = createServerApp();
            String[] args = {"-b", "not-a-url"};
            app.start(args);
            fail("expected exception did not occur");
        } catch (Exception ex) {
            assertEquals("wrong exception message", ServerApp.WRONG_BASE_ERR, ex.getMessage());
        }
        EasyMock.verify(phMock);
    }

    public void testFileOnly() throws Exception {
        phMock.createAndPublish(new File(emptyFile), null, false);
        EasyMock.replay(phMock);
        ServerApp app = createServerApp();
        String[] args = {emptyFile};
        app.start(args);
        EasyMock.verify(phMock);
    }

    public void testOptionsAB() throws Exception {
        phMock.createAndPublish(new File(emptyFile), epAddr, true);
        EasyMock.replay(phMock);
        ServerApp app = createServerApp();
        String[] args = {"-a", epAddr, "-b", epAddr, emptyFile};
        app.start(args);
        EasyMock.verify(phMock);
    }

    public void testOptionA() throws Exception {
        phMock.createAndPublish(new File(emptyFile), epAddr, false);
        EasyMock.replay(phMock);
        ServerApp app = createServerApp();
        String[] args = {"-a", epAddr, emptyFile};
        app.start(args);
        EasyMock.verify(phMock);
    }

    public void testOptionAWithOptionV() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(bout);
        PrintStream orig = System.out;
        try {
            System.setOut(pout);
            phMock.createAndPublish(new File(emptyFile), epAddr, false);
            EasyMock.replay(phMock);
            ServerApp app = createServerApp();
            String[] args = {"-a", epAddr, "-v", emptyFile};
            app.start(args);
            EasyMock.verify(phMock);
            pout.flush();
            assertTrue(new String(bout.toByteArray()).contains("processing file"));
        } finally {
            System.setOut(orig);            
        }
    }

    public void testOptionB() throws Exception {
        phMock.createAndPublish(new File(emptyFile), epAddr, true);
        EasyMock.replay(phMock);
        ServerApp app = createServerApp();
        String[] args = {"-b", epAddr, emptyFile};
        app.start(args);
        EasyMock.verify(phMock);
    }

    public void testOptionBWithOptionV() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(bout);
        PrintStream orig = System.out;
        try {
            System.setOut(pout);
            
            phMock.createAndPublish(new File(emptyFile), epAddr, true);
            EasyMock.replay(phMock);
            ServerApp app = createServerApp();
            String[] args = {"-b", epAddr, "-v", emptyFile};
            app.start(args);
            EasyMock.verify(phMock);
            assertTrue(new String(bout.toByteArray()).contains("processing file"));
        } finally {
            System.setOut(orig);            
        }
    }

    public void testDirectory() throws Exception {
        File f = new File(emptyFile);
        String dir = f.getParent();
        assertTrue(dir != null);
        EasyMock.checkOrder(phMock, false);
        phMock.createAndPublish(new File(emptyFile), epAddr, true);
        String file = getClass().getResource("empty/empty2.jsx").getFile();
        phMock.createAndPublish(new File(file), epAddr, true);
        file = getClass().getResource("empty/empty3.js").getFile();
        phMock.createAndPublish(new File(file), epAddr, true);
        file = getClass().getResource("empty/empty4.jsx").getFile();
        phMock.createAndPublish(new File(file), epAddr, true);
        EasyMock.replay(phMock);
        ServerApp app = createServerApp();
        String[] args = {"-b", epAddr, dir};
        app.start(args);
        EasyMock.verify(phMock);
    }

}
