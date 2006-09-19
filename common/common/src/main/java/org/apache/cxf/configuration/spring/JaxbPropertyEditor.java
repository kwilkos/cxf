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

import java.beans.PropertyEditorSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationException;

public class JaxbPropertyEditor extends PropertyEditorSupport {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxbPropertyEditor.class);
    private String packageName;
     
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Object getValue() {
        Object o = super.getValue();
        if (o instanceof Element) {
            Element el = (Element)o;
  
            try {
                return unmarshal(el);
            } catch (JAXBException ex) {
                Message msg = new Message("JAXB_PROPERTY_EDITOR_EXC", LOG, 
                                          new QName(el.getNamespaceURI(), el.getLocalName()));
                throw new ConfigurationException(msg, ex);
            }
        }

        return o;
    }

    public String getAsText() {
        Object o = super.getValue();
        if (null == o) {
            return null;
        } else if (o instanceof Element) {
            return ((Element)o).getTextContent();
        }
        return super.getAsText();
    }

    public void setAsText(String text) {
        Object o = super.getValue();
        if (null == o) {
            super.setValue(text);
        } else {
            super.setAsText(text);
        }
    }

    private Object unmarshal(Element data) throws JAXBException {
        return unmarshal(data, true);
    }

    private Object unmarshal(Element data, boolean doValidate) throws JAXBException {

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("unmarshalling: element namespaceURI: " + data.getNamespaceURI() + "\n"
                     + "                       localName: " + data.getLocalName() + "\n");
        }
        
        JAXBContext context = null;
        Object obj = null;

        context = JAXBContext.newInstance(packageName, getClass().getClassLoader());
        Unmarshaller u = context.createUnmarshaller();
        /*
         * if (doValidate) { u.setSchema(schema); }
         */
       
        obj = u.unmarshal(data);
        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>)obj;
            obj = el.getValue();
            /*
             * if (el.getName().equals(type)) { obj = el.getValue(); }
             */
        } 


        if (null != obj && LOG.isLoggable(Level.FINE)) {
            LOG.fine("Unmarshaled value into object of type: " + obj.getClass().getName()
                     + "    value: " + obj);
            
        }
        return obj;
    }
}
