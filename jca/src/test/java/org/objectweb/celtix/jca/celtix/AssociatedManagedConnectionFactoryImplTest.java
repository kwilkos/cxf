package org.objectweb.celtix.jca.celtix;

import java.util.Properties;

import javax.resource.ResourceException;

import javax.resource.spi.ResourceAdapter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;

public class AssociatedManagedConnectionFactoryImplTest extends ManagedConnectionFactoryImplTest {
    private static final String CELTIX_INSTALL_DIR = "celtix.install.dir";

    public AssociatedManagedConnectionFactoryImplTest(String name) {
        super(name);
    }

    public void testSetResourceAdapter() throws Exception {
        TestableAssociatedManagedConnectionFactoryImpl mci = 
            new TestableAssociatedManagedConnectionFactoryImpl();
        ResourceAdapterImpl rai = new ResourceAdapterImpl();
        mci.setResourceAdapter(rai);
        assertEquals("ResourceAdapter is set", mci.getResourceAdapter(), rai);
    }

    public void testSetWrongResourceAdapterThrowException() throws Exception {
        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        ResourceAdapter rai = EasyMock.createMock(ResourceAdapter.class);
        try {
            mci.setResourceAdapter(rai);
            fail("exception expected");
        } catch (ResourceException re) {
            assertTrue("wrong ResourceAdapter set", re.getMessage().indexOf("ResourceAdapterImpl") != -1);
        }
    }

    public void testRegisterBusThrowExceptionIfResourceAdapterNotSet() throws Exception {
        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        try {
            mci.registerBus();
            fail("exception expected");
        } catch (ResourceException re) {
            assertTrue("ResourceAdapter not set", re.getMessage().indexOf("null") != -1);
        }
    }

    public void testBusInitializedAndRegisteredToResourceAdapter() throws ResourceException, Exception {
        //TODO need to check the bus init get invovled with classloader
        /*
        System.setProperty("test.bus.class", DummyBus.class.getName());
        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        DummyResourceAdapterImpl rai = new DummyResourceAdapterImpl();
        mci.setResourceAdapter(rai);

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            // do this for MockObject creation
            Thread.currentThread().setContextClassLoader(mci.getClass().getClassLoader());

            Class dummyBusClass = Class.forName(DummyBus.class.getName(), true, mci.getClass()
                .getClassLoader());
            Field initializeCount = dummyBusClass.getField("initializeCount");

            mci.setCeltixInstallDir(DummyBus.vobRoot());
            mci.setCeltixCEURL(DummyBus.CeltixCEURL);
            ConnectionManager cm = (ConnectionManager)MockObjectFactory.create(Class
                .forName(ConnectionManager.class.getName(), true, mci.getClass().getClassLoader()));

            mci.createConnectionFactory(cm);
            assertEquals("bus should be initialized once", 1, initializeCount.getInt(null));
            assertEquals("bus registered once after first call", 1, rai.registeredCount);
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }*/
    }

    public void testMergeNonDuplicateResourceAdapterProps() throws ResourceException {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        ResourceAdapterImpl rai = new ResourceAdapterImpl(props);

        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        mci.setCeltixInstallDir("value2");

        assertEquals("before associate, one props", 1, mci.getPluginProps().size());
        assertTrue("before associate, Celtix_INSTALL_DIR_PROPERTY is set", mci.getPluginProps()
            .containsKey(CELTIX_INSTALL_DIR));
        assertTrue("before associate, key1 not set", !mci.getPluginProps().containsKey("key1"));

        mci.setResourceAdapter(rai);
        assertEquals("after associate, two props", 2, mci.getPluginProps().size());
        assertTrue("after associate, key1 is set", mci.getPluginProps().containsKey("key1"));
    }

    public void testMergeDuplicateResourceAdapterProps() throws ResourceException {
        Properties props = new Properties();
        props.setProperty(CELTIX_INSTALL_DIR, "value1");
        ResourceAdapterImpl rai = new ResourceAdapterImpl(props);

        TestableAssociatedManagedConnectionFactoryImpl mci = 
            new TestableAssociatedManagedConnectionFactoryImpl();
        mci.setCeltixInstallDir("value2");

        assertEquals("before associate, one props", 1, mci.getPluginProps().size());
        assertEquals("before associate,  CELTIX_INSTALL_DIR_PROPERTY set to value2", "value2", mci
            .getPluginProps().getProperty(CELTIX_INSTALL_DIR));

        mci.setResourceAdapter(rai);

        assertEquals("after associate, still one props", 1, mci.getPluginProps().size());
        assertEquals("after associate, CELTIX_INSTALL_DIR_PROPERTY value no change", "value2", mci
            .getPluginProps().getProperty(CELTIX_INSTALL_DIR));
    }

    public void testMergeEmptyResourceAdapterProps() throws ResourceException {
        Properties props = new Properties();
        ResourceAdapterImpl rai = new ResourceAdapterImpl(props);
        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        mci.setCeltixInstallDir("value1");
        assertEquals("before associate, one props", 1, mci.getPluginProps().size());
        mci.setResourceAdapter(rai);
        assertEquals("after associate, still one props", 1, mci.getPluginProps().size());
    }

    protected ManagedConnectionFactoryImpl createManagedConnectionFactoryImpl() {
        TestableAssociatedManagedConnectionFactoryImpl mci =
            new TestableAssociatedManagedConnectionFactoryImpl();
        try {
            mci.setResourceAdapter(new DummyResourceAdapterImpl());
        } catch (Exception e) {
            System.out.println("failed to setResourceAdapter" + e);
        }
        return mci;
    }

    public static Test suite() {
        return new TestSuite(AssociatedManagedConnectionFactoryImplTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {AssociatedManagedConnectionFactoryImplTest.class.getName()});
    }
}

class DummyResourceAdapterImpl extends ResourceAdapterImpl {
    int registeredCount;

    DummyResourceAdapterImpl() {
        super();
    }

    public void registerBus(Bus bus) {
        registeredCount++;
    }
}

class TestableAssociatedManagedConnectionFactoryImpl extends AssociatedManagedConnectionFactoryImpl {
   
}
