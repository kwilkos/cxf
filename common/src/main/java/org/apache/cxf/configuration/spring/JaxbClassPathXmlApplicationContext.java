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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JaxbClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
    
    public JaxbClassPathXmlApplicationContext(String configLocation) throws BeansException {
        super(configLocation);
    }

    public JaxbClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
        super(configLocations);
    }
    
    

    @Override
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
        reader.setDocumentReaderClass(JaxbBeanDefinitionDocumentReader.class);
        // TODO: check why VALIDATION_XSD complains about mixed content in
        // value elements - this should be legal according to the xsd
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        reader.setNamespaceAware(true);  
              
    }
   
}
