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

package org.apache.cxf.bus.spring;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.InputSource;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;

/**
 * This is a mutant version of the bean definition reader
 * which allows us to avoid validating our internal CXF
 * configuration files.
 */
public class ControlledValidationXmlBeanDefinitionReader extends XmlBeanDefinitionReader {

    // Spring has no 'getter' for this.
    private int visibleValidationMode = VALIDATION_AUTO;

    /**
     * @param beanFactory
     */
    public ControlledValidationXmlBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
        super(beanFactory);
        this.setDocumentLoader(new TunedDocumentLoader());
    }

    @Override
    protected int doLoadBeanDefinitions(InputSource inputSource, 
                                        Resource resource) throws BeanDefinitionStoreException {
        // sadly, the Spring class we are extending has the critical function
        // getValidationModeForResource
        // marked private instead of protected, so trickery is called for here.
        boolean suppressValidation = false;
        try {
            URL url = resource.getURL();
            if (url.getFile().contains("META-INF/cxf/")) {
                suppressValidation = true;
            }
        } catch (IOException e) {
            // this space intentionally left blank.
        }
        
        int savedValidation = visibleValidationMode;
        if (suppressValidation) {
            setValidationMode(VALIDATION_NONE);
        }
        int r = super.doLoadBeanDefinitions(inputSource, resource);
        setValidationMode(savedValidation);
        return r;
    }

    @Override
    public void setValidationMode(int validationMode) {
        visibleValidationMode = validationMode;
        super.setValidationMode(validationMode);
    }

}
