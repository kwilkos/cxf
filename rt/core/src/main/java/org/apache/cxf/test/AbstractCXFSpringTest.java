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

package org.apache.cxf.test;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Base class for tests that use a Spring bean specification to load up components for testing.
 * Unlike the classes that come with Spring, it doesn't drag in the JUnit 3 hierarchy, and it 
 * doesn't inject into the test itself from the beans.
 */
public abstract class AbstractCXFSpringTest extends AbstractCXFTest {
    
    private GenericApplicationContext applicationContext;
    private DefaultResourceLoader resourceLoader;

    /**
     * Load up all the beans from the XML files returned by the getConfigLocations method.
     */
    protected AbstractCXFSpringTest() {
        applicationContext = new GenericApplicationContext();
        resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
        for (String beanDefinitionPath : getConfigLocations()) {
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
            Resource resource = resourceLoader.getResource(beanDefinitionPath);
            reader.loadBeanDefinitions(resource);
        }
        applicationContext.refresh();
    }
    
    /**
     * Return an array of resource specifications. 
     * @see org.springframework.core.io.DefaultResourceLoader for the syntax.
     * @return array of resource specifications.
     */
    protected abstract String[] getConfigLocations();

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * Convenience method for the common case of retrieving a bean from the context.
     * One would expect Spring to have this. 
     * @param <T> Type of the bean object.
     * @param type Type of the bean object.
     * @param beanName ID of the bean.
     * @return The Bean.
     */
    protected <T> T getBean(Class<T> type, String beanName) {
        return type.cast(applicationContext.getBean(beanName));
    }
}
