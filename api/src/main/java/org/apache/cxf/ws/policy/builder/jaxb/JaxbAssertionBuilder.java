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

package org.apache.cxf.ws.policy.builder.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxb.JAXBUtils;
import org.apache.cxf.ws.policy.AssertionBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;

public class JaxbAssertionBuilder<T> implements AssertionBuilder {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxbAssertionBuilder.class);
    private Unmarshaller unmarshaller;
    private Collection<QName> supportedTypes;
    
    /**
     * Constructs a JAXBAssertionBuilder from the QName of the schema type
     * @param qn the schema type
     * @throws JAXBException
     * @throws ClassNotFoundException
     */
    public JaxbAssertionBuilder(QName qn) throws JAXBException, ClassNotFoundException {
        this(JAXBUtils.namespaceURIToPackage(qn.getNamespaceURI())
            + "." + JAXBUtils.nameToIdentifier(qn.getLocalPart(), JAXBUtils.IdentifierType.CLASS), qn);
    }
    
    /**
     * Constructs a JAXBAssertionBuilder from the specified class name and schema type. 
     * @param className the name of the class to which the schema type is mapped
     * @param qn the schema type
     * @throws JAXBException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public JaxbAssertionBuilder(String className, QName qn) throws JAXBException, ClassNotFoundException {
        this(ClassLoaderUtils.loadClass(className, JaxbAssertionBuilder.class), qn);
    }
    
    /**
    * Constructs a JAXBAssertionBuilder from the specified class and schema type. 
    * @param type the class to which the schema type is mapped
    * @param qn the schema type
    * @throws JAXBException
    * @throws ClassNotFoundException
    */
    public JaxbAssertionBuilder(Class<T> type, QName qn) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(type.getPackage().getName());
        unmarshaller = context.createUnmarshaller();
        supportedTypes = Collections.singletonList(qn);
    }
       
    @SuppressWarnings("unchecked")
    public Assertion build(Element element) {
        Object obj = null;
        try {
            obj = unmarshaller.unmarshal(element);
        } catch (JAXBException ex) {
            LogUtils.log(LOG, Level.SEVERE, "UNMARSHAL_ELEMENT_EXC", ex);
        }
        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>)obj;
            obj = el.getValue();
        } 

        if (null != obj) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Unmarshaled element into object of type: " + obj.getClass().getName()
                     + "    value: " + obj);
            }
            QName name = new QName(element.getNamespaceURI(), element.getLocalName());
            boolean optional = false;
            String value = element.getAttributeNS(
                               Constants.Q_ELEM_OPTIONAL_ATTR.getNamespaceURI(), 
                               Constants.Q_ELEM_OPTIONAL_ATTR.getLocalPart());
            if (Boolean.valueOf(value)) {
                optional = true;
            }
            JaxbAssertion<T> a = new JaxbAssertion<T>(name, optional);
            a.setData((T)obj);
            return a;
        }
        return null;
    }
    
    public Collection<QName> getKnownElements() {
        return supportedTypes;
    }

    public Assertion buildCompatible(Assertion a, Assertion b) {
        return null;
    }
    
    

}
