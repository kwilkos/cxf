package org.objectweb.celtix.bus.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.types.StringListType;
import org.objectweb.celtix.resource.ClassLoaderResolver;
import org.objectweb.celtix.resource.ClasspathResolver;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;


public class ResourceManagerImplTest extends TestCase {

    private static final String TEST_RESOURCE = "this is the found resource"; 
    private static final String TEST_RESOURCE_NAME = "testResource";
    private static final int DEFAULT_RESOLVER_COUNT = 2; 

    private ResourceManager mgr = new ResourceManagerImpl(); 
    private final ResourceResolver resolver1 = EasyMock.createMock(ResourceResolver.class);
    private final ResourceResolver resolver2 = EasyMock.createMock(ResourceResolver.class);
    
    public void setUp() { 
        mgr.addResourceResolver(resolver2);
        mgr.addResourceResolver(resolver1);
    } 


    public void testDefaultResolvers() { 

        mgr = new ResourceManagerImpl(); 
        assertNotNull(mgr.getResourceResolvers()); 
        assertEquals(DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        assertEquals(ClassLoaderResolver.class, mgr.getResourceResolvers().get(0).getClass());
        assertEquals(ClasspathResolver.class, mgr.getResourceResolvers().get(1).getClass());
    } 

    public void testGetResourceAsStream() { 

        ByteArrayInputStream resourceStream = new ByteArrayInputStream(TEST_RESOURCE.getBytes());

        resolver1.getAsStream(TEST_RESOURCE_NAME);
        EasyMock.expectLastCall().andReturn(resourceStream);
        EasyMock.replay(resolver1);
        EasyMock.replay(resolver2);

        InputStream ret = mgr.getResourceAsStream(TEST_RESOURCE_NAME);
        assertSame(resourceStream, ret);
        
        EasyMock.verify(resolver1);
        EasyMock.verify(resolver2);

        EasyMock.reset(resolver1);
        EasyMock.reset(resolver2);

        // do it again but let the second resolver handler it

        resolver1.getAsStream(TEST_RESOURCE_NAME);
        EasyMock.expectLastCall().andReturn(null);
        resolver2.getAsStream(TEST_RESOURCE_NAME);
        EasyMock.expectLastCall().andReturn(resourceStream);

        EasyMock.replay(resolver1);
        EasyMock.replay(resolver2);

        ret = mgr.getResourceAsStream(TEST_RESOURCE_NAME);
        assertSame(resourceStream, ret);
        
        EasyMock.verify(resolver1);
        EasyMock.verify(resolver2);
    } 


    public void testResolveResource() { 

        resolver1.resolve(TEST_RESOURCE_NAME, String.class);
        EasyMock.expectLastCall().andReturn(TEST_RESOURCE);
        EasyMock.replay(resolver1);
        EasyMock.replay(resolver2);

        Object ret = mgr.resolveResource(TEST_RESOURCE_NAME, String.class);
        assertEquals(TEST_RESOURCE, ret);
        
        EasyMock.verify(resolver1);
        EasyMock.verify(resolver2);

        EasyMock.reset(resolver1);
        EasyMock.reset(resolver2);

        // do it again but let the second resolver handler it

        resolver1.resolve(TEST_RESOURCE_NAME, String.class);
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(resolver1);
        resolver2.resolve(TEST_RESOURCE_NAME, String.class);
        EasyMock.expectLastCall().andReturn(TEST_RESOURCE);
        EasyMock.replay(resolver2);

        ret = mgr.resolveResource(TEST_RESOURCE_NAME, String.class);
        assertEquals(TEST_RESOURCE, ret);
        
        EasyMock.verify(resolver1);
        EasyMock.verify(resolver2);
    } 
    


    public void testAddRemoveResourceResolver() { 
        
        // use a clean one for this test
        mgr = new ResourceManagerImpl();
        assertEquals(DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        mgr.addResourceResolver(resolver1); 
        assertEquals(1  + DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        mgr.addResourceResolver(resolver1); 
        assertEquals(1 + DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());

        mgr.removeResourceResolver(resolver1); 
        assertEquals(DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        
        mgr.addResourceResolver(resolver1); 
        mgr.addResourceResolver(resolver2); 
        assertEquals(2 + DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        mgr.removeResourceResolver(resolver1); 
        assertEquals(1 + DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
        mgr.removeResourceResolver(resolver2); 
        assertEquals(DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());

        mgr.removeResourceResolver(resolver1); 
        assertEquals(DEFAULT_RESOLVER_COUNT, mgr.getResourceResolvers().size());
    } 


    public void testCtorWithBus() throws Exception { 
        
        Bus bus = EasyMock.createMock(Bus.class);
        Configuration conf = EasyMock.createMock(Configuration.class);
        
        StringListType resolverList = new StringListType(); 
        resolverList.getItem().add(ClassLoaderResolver.class.getName()); 
        resolverList.getItem().add(ClasspathResolver.class.getName()); 

        new ClassLoaderResolver();

        bus.getConfiguration();
        EasyMock.expectLastCall().andReturn(conf); 
        conf.getObject("resourceResolvers");
        EasyMock.expectLastCall().andReturn(resolverList); 

        EasyMock.replay(bus);
        EasyMock.replay(conf);

        ResourceManagerImpl rmi = new ResourceManagerImpl(bus);

        assertNotNull(rmi.getResourceResolvers());
        assertEquals(2, rmi.getResourceResolvers().size()); 
        assertEquals(ClassLoaderResolver.class, rmi.getResourceResolvers().get(0).getClass());
        assertEquals(ClasspathResolver.class, rmi.getResourceResolvers().get(1).getClass());

        EasyMock.verify(bus);
        EasyMock.verify(conf);
    } 

    public static Test suite() {
        return new TestSuite(ResourceManagerImplTest.class);
    }
    
    public static void main(String[] args) {
        TestRunner.main(new String[] {ResourceManagerImplTest.class.getName()});
    }
}
