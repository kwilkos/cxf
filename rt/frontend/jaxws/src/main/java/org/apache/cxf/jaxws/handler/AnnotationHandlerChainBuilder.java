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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.configuration.spring.JaxbClassPathXmlApplicationContext;
import org.apache.cxf.jaxws.javaee.HandlerChainType;
import org.apache.cxf.jaxws.javaee.HandlerConfigBean;
import org.springframework.context.ApplicationContext;

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
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz);
        List<Handler> chain = null;
        if (hcAnn == null) {
            LOG.fine("no HandlerChain annotation on " + clz);
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate();
            String handlerFile = hcAnn.getFileName();

            // This Configurer needs to be different from system configurer as
            // it is only aware of handler configure file.
            ApplicationContext ac = new JaxbClassPathXmlApplicationContext(new String[] {
                handlerFile,
                "META-INF/cxf/cxf-property-editors.xml"});
            Configurer configurer = new ConfigurerImpl(ac);

            HandlerConfigBean hcb = new HandlerConfigBean();
            configurer.configureBean(hcb);
            HandlerChainType hc = hcb.getJaxwshandler();

            if (null == hc) {
                throw new WebServiceException(BUNDLE.getString("CHAIN_NOT_SPECIFIED_EXC"));
            }

            chain = buildHandlerChain(hc, clz.getClassLoader());
        }
        assert chain != null;
        if (existingHandlers != null) {
            chain.addAll(existingHandlers);
        }
        return sortHandlers(chain);
    }

    public List<Handler> buildHandlerChainFromClass(Class<?> clz) {
        return buildHandlerChainFromClass(clz, null);
    }

    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz) {

        HandlerChain ann = clz.getAnnotation(HandlerChain.class);
        Class<?> declaringClass = clz;

        if (ann == null) {
            for (Class<?> iface : clz.getInterfaces()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("checking for HandlerChain annotation on " + iface.getName());
                }
                ann = iface.getAnnotation(HandlerChain.class);
                if (ann != null) {
                    declaringClass = iface;
                    break;
                }
            }
        }
        if (ann != null) {
            return new HandlerChainAnnotation(ann, declaringClass);
        } else {
            return null;
        }
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

        public String getChainName() {
            return ann.name();
        }

        public void validate() {
            if (null == ann.file() || "".equals(ann.file())) {
                throw new WebServiceException(BUNDLE.getString("ANNOTATION_WITHOUT_URL_EXC"));
            }
            if (null == ann.name() || "".equals(ann.name())) {
                LOG.fine("no handler name specified, defaulting to first declared");
            }
        }

        public String toString() {
            return "[" + declaringClass + "," + ann + "]";
        }
    }
}
