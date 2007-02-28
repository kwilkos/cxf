/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.common.injection;



import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import junit.framework.TestCase;

import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;

import org.easymock.classextension.EasyMock;

public class ResourceInjectorTest extends TestCase {
    private static final String RESOURCE_ONE = "resource one";
    private static final String RESOURCE_TWO = "resource one";

    private ResourceInjector injector; 
        
    public void setUpResourceManager(String pfx) { 

        ResourceManager resMgr = EasyMock.createMock(ResourceManager.class);
        List<ResourceResolver> resolvers = new ArrayList<ResourceResolver>();
        
        resMgr.getResourceResolvers();
        EasyMock.expectLastCall().andReturn(resolvers);
        resMgr.resolveResource(pfx + "resource1", String.class, resolvers);
        EasyMock.expectLastCall().andReturn(RESOURCE_ONE);
        resMgr.resolveResource("resource2", String.class, resolvers);
        EasyMock.expectLastCall().andReturn(RESOURCE_TWO);
        EasyMock.replay(resMgr);
        
        injector = new ResourceInjector(resMgr); 
    } 

    public void testFieldInjection() { 
        setUpResourceManager(FieldTarget.class.getCanonicalName() + "/");
        doInjectTest(new FieldTarget()); 
    }

    public void testSetterInjection() {
        setUpResourceManager(SetterTarget.class.getCanonicalName() + "/");
        doInjectTest(new SetterTarget()); 
    }

    public void testClassLevelInjection() {
        setUpResourceManager("");
        doInjectTest(new ClassTarget());
    }

    public void testResourcesContainer() {
        setUpResourceManager("");
        doInjectTest(new ResourcesContainerTarget()); 
    }

    public void testPostConstruct() { 
        setUpResourceManager(SetterTarget.class.getCanonicalName() + "/");

        SetterTarget target = new SetterTarget(); 
        doInjectTest(target); 
        assertTrue(target.injectionCompleteCalled()); 
    }

    public void testPreDestroy() { 
        injector = new ResourceInjector(null, null);
        SetterTarget target = new SetterTarget(); 
        injector.destroy(target); 
        assertTrue(target.preDestroyCalled()); 
    }

    protected void doInjectTest(Target target) { 

        injector.inject(target); 

        assertNotNull(target.getResource1()); 
        assertEquals(RESOURCE_ONE, target.getResource1()); 

        assertNotNull(target.getResource2()); 
        assertEquals(RESOURCE_TWO, target.getResource2()); 
    }

}


interface Target {
    String getResource1(); 
    String getResource2(); 
} 

class FieldTarget implements Target {

    @Resource
    private String resource1; 

    @Resource(name = "resource2")
    private String resource2foo;

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
    private boolean preDestroy; 
    private boolean preDestroyPrivate; 

    public final String getResource1() {
        return this.resource1;
    }

    @Resource
    public final void setResource1(final String argResource1) {
        this.resource1 = argResource1;
    }
    
    public final String getResource2() {
        return this.resource2;
    }
    
    @Resource(name = "resource2")
    public final void setResource2(final String argResource2) {
        this.resource2 = argResource2;
    }

    @PostConstruct
    public void injectionIsAllFinishedNowThankYouVeryMuch() { 
        injectionCompletePublic = true;

        // stick this here to keep PMD happy...
        injectionIsAllFinishedNowThankYouVeryMuchPrivate();
    } 
    
    @PostConstruct
    private void injectionIsAllFinishedNowThankYouVeryMuchPrivate() { 
        injectionCompletePrivate = true;
    } 
    
    @PreDestroy
    public void preDestroyMethod() { 
        preDestroy = true;
    } 
    
    @PreDestroy
    private void preDestroyMethodPrivate() { 
        preDestroyPrivate = true;
    } 
    
    public boolean injectionCompleteCalled() { 
        return injectionCompletePrivate && injectionCompletePublic;
    }

    public boolean preDestroyCalled() { 
        return preDestroy && preDestroyPrivate;
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
