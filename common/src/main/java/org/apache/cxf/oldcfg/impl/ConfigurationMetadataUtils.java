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

package org.apache.cxf.oldcfg.impl;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.oldcfg.ConfigurationException;

public final class ConfigurationMetadataUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataUtils.class);
    
    /**
     * prevents instantiation
     *
     */
    private ConfigurationMetadataUtils() {
    }
    
    public static String getElementValue(Node node) {
        for (Node nd = node.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.TEXT_NODE == nd.getNodeType()) {
                return nd.getNodeValue();
            }
        }
        return null;
    }
    
    public static QName elementAttributeToQName(Document document, Element element, String attrName) {
        return stringToQName(document, element, element.getAttribute(attrName));
    }
    
    public static QName elementValueToQName(Document document, Element element) {
        return stringToQName(document, element, getElementValue(element));
    }
    
    private static QName stringToQName(Document document, Element element, String s) {
        
        int index = s.indexOf(":");
        if (index < 0) {
            return new QName(s);
        } else if (index == 0) {
            throw new ConfigurationException(new Message("ILLEGAL_QNAME_EXC", LOG, s));
        }
        String prefix = s.substring(0, index);
        String nsAttr = "xmlns:" + prefix;
        String uri = null;
        Element el = element;
        while (null == uri || "".equals(uri)) {
            uri = el.getAttribute(nsAttr);
            if (null != uri && !"".equals(uri)) {
                break;
            }
            if (el == document.getDocumentElement()) {
                break;
            }
            el = (Element)el.getParentNode();
        }
        if (null == uri || "".equals(uri)) {
            throw new ConfigurationException(new Message("ILLEGAL_PREFIX_EXC", LOG, s));
        }
        if (index >= (s.length() - 1)) {
            throw new ConfigurationException(new Message("ILLEGAL_QNAME_EXC", LOG, s));
        }
        
        String localPart = s.substring(index + 1);
        return new QName(uri, localPart);
    }
}
