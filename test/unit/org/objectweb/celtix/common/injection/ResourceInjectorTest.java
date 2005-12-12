package org.objectweb.celtix.common.injection;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.Resources;
import junit.framework.TestCase;
import org.objectweb.celtix.bus.resource.ResourceManagerImpl;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;


public class ResourceInjectorTest extends TestCase {
    private static final String RESOURCE_ONE = "resource one";
    private static final String RESOURCE_TWO = "resource one";

    Map<String, String> resourceMap = new HashMap<String, String>(); 

    private ResourceInjector injector; 
        
    public void setUp() { 
        resourceMap.put("resource1", RESOURCE_ONE); 
        resourceMap.put("resource2", RESOURCE_TWO); 

        ResourceManager resMgr = new ResourceManagerImpl(); 
        resMgr.addResourceResolver(new TestResolver());
        injector = new ResourceInjector(resMgr); 
    } 

    public void testFieldInjection() { 

        doInjectTest(new FieldTarget()); 
    }

    public void testSetterInjection() {

        doInjectTest(new SetterTarget()); 
    }

    public void testClassLevelInjection() {
        doInjectTest(new ClassTarget());
    }

    public void testResourcesContainer() {
        doInjectTest(new ResourcesContainerTarget()); 
    }

    public void testPostConstruct() { 

        SetterTarget target = new SetterTarget(); 
        doInjectTest(target); 
        assertTrue(target.injectionCompleteCalled()); 
    }


    protected void doInjectTest(Target target) { 

        injector.inject(target); 

        assertNotNull(target.getResource1()); 
        assertEquals(RESOURCE_ONE, target.getResource1()); 

        assertNotNull(target.getResource2()); 
        assertEquals(RESOURCE_TWO, target.getResource2()); 
    }

    class TestResolver implements ResourceResolver {
        public Object resolve(String resourceName, Class<?> resourceType) {
            assertEquals(String.class, resourceType);
            return resourceMap.get(resourceName);
        }
        public InputStream getAsStream(String name) { 
            return null;
        }
    }    
}


interface Target {
    String getResource1(); 
    String getResource2(); 
} 

class FieldTarget implements Target {

    @Resource private String resource1; 

    @Resource(name = "resource2") private String resource2foo;

    public String getResource1() { 
        return resource1; 
    } 

    public String getResource2() { 
        return resource2foo;
    } 

    public String toString() { 
        return "[" + resource1 + ":" + resource2foo + "]";
    }

}

class SetterTarget implements Target { 

    private String resource1;
    private String resource2;
    private boolean injectionCompletePublic; 
    private boolean injectionCompletePrivate; 

    public final String getResource1() {
        return this.resource1;
    }

    @Resource public final void setResource1(final String argResource1) {
        this.resource1 = argResource1;
    }
    
    public final String getResource2() {
        return this.resource2;
    }
    
    @Resource(name = "resource2") public final void setResource2(final String argResource2) {
        this.resource2 = argResource2;
    }

    @PostConstruct public void injectionIsAllFinishedNowThankYouVeryMuch() { 
        injectionCompletePublic = true;

        // stick this here to keep PMD happy...
        injectionIsAllFinishedNowThankYouVeryMuchPrivate();
    } 
    
    @PostConstruct private void injectionIsAllFinishedNowThankYouVeryMuchPrivate() { 
        injectionCompletePrivate = true;
    } 
    
    public boolean injectionCompleteCalled() { 
        return injectionCompletePrivate && injectionCompletePublic;
    }
}

@Resource(name = "resource1")
class ClassTarget implements Target {

    @Resource(name = "resource2") public String resource2foo; 
    private String res1; 

    public final void setResource1(String res) { 
        res1 = res; 
    } 

    public final String getResource1() {
        return res1;
    }

    public final String getResource2() {
        return resource2foo;
    }
}



@Resources({@Resource(name = "resource1"), 
            @Resource(name = "resource2") })
class ResourcesContainerTarget implements Target {

    private String res1; 
    private String resource2; 

    public final void setResource1(String res) { 
        res1 = res; 
    } 

    public final String getResource1() {
        return res1;
    }

    public final String getResource2() {
        return resource2;
    }
}
