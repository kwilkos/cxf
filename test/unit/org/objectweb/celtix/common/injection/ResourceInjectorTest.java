package org.objectweb.celtix.common.injection;


import java.util.HashMap;
import java.util.Map;
import javax.annotation.InjectionComplete;
import javax.annotation.Resource;
import junit.framework.TestCase;


public class ResourceInjectorTest extends TestCase {
    private static final String RESOURCE_ONE = "resource one";
    private static final String RESOURCE_TWO = "resource one";

    Map<String, String> resourceMap = new HashMap<String, String>(); 

    private ResourceInjector injector = new ResourceInjector(new TestResolver());
        
    public void setUp() { 
        resourceMap.put("resource1", RESOURCE_ONE); 
        resourceMap.put("resource2", RESOURCE_TWO); 
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

    public void testTypeMismatch() { 
        
    }

    public void testResourcesContainer() {
    }

    public void testInjectionComplete() { 

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
            return resourceMap.get(resourceName);
        }
    }    
}


interface Target {
    String getResource1(); 
    String getResource2(); 
} 

class FieldTarget implements Target {

    @Resource public String resource1; 

    @Resource(name = "resource2") public String resource2foo;

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
    private boolean injectionComplete; 

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

    @InjectionComplete public void injectionIsAllFinishedNowThankYouVeryMuch() { 
        injectionComplete = true;
    } 
    
    public boolean injectionCompleteCalled() { 
        return injectionComplete;
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
