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

package org.apache.cxf.configuration.spring;

import javax.xml.bind.DatatypeConverter;

import com.sun.xml.bind.DatatypeConverterImpl;

import junit.framework.TestCase;

import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.foo.Address;
import org.apache.cxf.configuration.foo.Foo;
import org.apache.cxf.configuration.foo.Point;

public class JaxbPropertyEditorTest extends TestCase {

    public void testPerType() throws Exception {
        
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
        
        JaxbClassPathXmlApplicationContext context = new JaxbClassPathXmlApplicationContext(new String[] {
            "/org/apache/cxf/configuration/spring/test-jaxb-beans.xml",
            "/org/apache/cxf/configuration/spring/cxf-property-editors.xml"
        });
        
        FooBean foo = (FooBean)context.getBean("complex");
        
        Point point = foo.getPosition();
        assertEquals("Unexpected value for point", 12, point.getX());
        assertEquals("Unexpected value for point", 33, point.getY());
        
        Address addr = foo.getAddress();
        assertEquals("Unexpected value for address", "Dublin", addr.getCity());
        assertEquals("Unexpected value for address", 4, addr.getZip());
        assertEquals("Unexpected value for address", "Shelbourne Rd", addr.getStreet());
        assertNull("Unexpected value for address", addr.getNr());
        
        assertEquals("Unexpected value for name", "foam", foo.getName());  
    }
    
    public void testPerPackage() throws Exception {
        
        DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
        
        JaxbClassPathXmlApplicationContext context = new JaxbClassPathXmlApplicationContext(new String[] {
            "/org/apache/cxf/configuration/spring/test-jaxb-beans.xml",
            "/org/apache/cxf/configuration/spring/cxf.xml",
            "/org/apache/cxf/configuration/spring/cxf-jaxb-property-types.xml",
            
        });
        
        FooBean foo = (FooBean)context.getBean("complex");
        
        Point point = foo.getPosition();
        assertEquals("Unexpected value for point", 12, point.getX());
        assertEquals("Unexpected value for point", 33, point.getY());
        
        Address addr = foo.getAddress();
        assertEquals("Unexpected value for address", "Dublin", addr.getCity());
        assertEquals("Unexpected value for address", 4, addr.getZip());
        assertEquals("Unexpected value for address", "Shelbourne Rd", addr.getStreet());
        assertNull("Unexpected value for address", addr.getNr());
        
        assertEquals("Unexpected value for name", "foam", foo.getName());  
    }
    
    static class FooBean extends Foo implements Configurable {
        private String beanName = "complex";

        public FooBean() {
        }
        
        public FooBean(String bn) {
            beanName = bn;
        }

        public String getBeanName() {
            return beanName;
        }
        
        
 
    }
    
    
}
