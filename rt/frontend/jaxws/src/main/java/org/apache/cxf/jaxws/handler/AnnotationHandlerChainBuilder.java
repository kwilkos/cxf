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

package org.apache.cxf.jaxws.handler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxws.javaee.HandlerChainType;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;

public class AnnotationHandlerChainBuilder extends HandlerChainBuilder {

    private static final Logger LOG = LogUtils.getL7dLogger(AnnotationHandlerChainBuilder.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    public AnnotationHandlerChainBuilder() {
    }

    public AnnotationHandlerChainBuilder(Bus bus) {
        super(bus);
    }

    /**
     * @param clz
     * @param existingHandlers
     * @return
     */
    public List<Handler> buildHandlerChainFromClass(Class<?> clz, List<Handler> existingHandlers) {
        LOG.fine("building handler chain");
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz, true);
        List<Handler> chain = null;
        if (hcAnn == null) {
            LOG.fine("no HandlerChain annotation on " + clz);
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate();

            try {
                JAXBContext jc = JAXBContext
                        .newInstance(org.apache.cxf.jaxws.javaee.ObjectFactory.class);
                Unmarshaller u = jc.createUnmarshaller();                
                URL handlerFileURL  = resolveHandlerChainFile(clz, hcAnn.getFileName()); 
                JAXBElement<?> o = (JAXBElement<?>)u.unmarshal(handlerFileURL);

                HandlerChainsType handlerChainsType = (HandlerChainsType) o.getValue();

                if (null == handlerChainsType || handlerChainsType.getHandlerChain().size() == 0) {
                    throw new WebServiceException(BUNDLE
                            .getString("CHAIN_NOT_SPECIFIED_EXC"));
                }
                
                chain = new ArrayList<Handler>();
                for (HandlerChainType hc : handlerChainsType.getHandlerChain()) {
                    chain.addAll(buildHandlerChain(hc, clz.getClassLoader()));
                }

            } catch (Exception e) {
                throw new WebServiceException(BUNDLE.getString("CHAIN_NOT_SPECIFIED_EXC"), e);
            }
        }
        assert chain != null;
        if (existingHandlers != null) {
            chain.addAll(existingHandlers);
        }
        return sortHandlers(chain);
    }

    protected URL resolveHandlerChainAnnotationFile(Class clazz, String name) {
        return clazz.getResource(name);
    }

    public List<Handler> buildHandlerChainFromClass(Class<?> clz) {
        return buildHandlerChainFromClass(clz, null);
    }

    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz, boolean searchSEI) {        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Checking for HandlerChain annotation on " + clz.getName());
        }
        HandlerChainAnnotation hcAnn = null;
        HandlerChain ann = clz.getAnnotation(HandlerChain.class);
        if (ann == null) {
            if (searchSEI) {
                /* HandlerChain annotation can be specified on the SEI
                 * but the implementation bean might not implement the SEI.          
                 */
                WebService ws = clz.getAnnotation(WebService.class);
                if (ws != null && !StringUtils.isEmpty(ws.endpointInterface())) {
                    String seiClassName = ws.endpointInterface().trim();
                    Class seiClass = null;
                    try {
                        seiClass = clz.getClassLoader().loadClass(seiClassName);
                    } catch (ClassNotFoundException e) {
                        throw new WebServiceException(BUNDLE.getString("SEI_LOAD_FAILURE_EXC"), e);
                    }

                    // check SEI class and its interfaces for HandlerChain annotation
                    hcAnn = findHandlerChainAnnotation(seiClass, false);
                }
            }
            if (hcAnn == null) {
                // check interfaces for HandlerChain annotation
                for (Class<?> iface : clz.getInterfaces()) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Checking for HandlerChain annotation on " + iface.getName());
                    }
                    ann = iface.getAnnotation(HandlerChain.class);
                    if (ann != null) {
                        hcAnn = new HandlerChainAnnotation(ann, iface);
                        break;
                    }
                }
            }
        } else {
            hcAnn = new HandlerChainAnnotation(ann, clz);
        }
        
        return hcAnn;
    }
    
    private static class HandlerChainAnnotation {
        private final Class<?> declaringClass;
        private final HandlerChain ann;

        HandlerChainAnnotation(HandlerChain hc, Class<?> clz) {
            ann = hc;
            declaringClass = clz;
        }

        public Class<?> getDeclaringClass() {
            return declaringClass;
        }

        public String getFileName() {
            return ann.file();
        }

        public void validate() {
            if (null == ann.file() || "".equals(ann.file())) {
                throw new WebServiceException(BUNDLE.getString("ANNOTATION_WITHOUT_URL_EXC"));
            }
        }

        public String toString() {
            return "[" + declaringClass + "," + ann + "]";
        }
    }
}
