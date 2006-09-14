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
package org.apache.cxf.binding.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.interceptor.Fault;

public class XMLFault extends Fault {

    static final long serialVersionUID = 100000;

    private Node faultRoot;

    // private QNname faultCode;
    private String faultString;

    private Node faultDetail;

    private Node detailRoot;

    

    public XMLFault(Message message, Throwable throwable) {
        super(message, throwable);
    }

    public XMLFault(Message message) {
        super(message);
    }

    public XMLFault(Throwable t) {
        super(t);
    }

    public void setFaultString(String str) {
        this.faultString = str;
    }

    public void addFaultString(String str) {
        assert faultRoot != null;

        Text text = XMLUtils.createTextNode(this.faultRoot, str);
        Node faultStringNode = XMLUtils.createElementNS(this.faultRoot, XMLConstants.XML_FAULT_STRING);
        faultStringNode.appendChild(text);
        this.faultRoot.appendChild(faultStringNode);

        this.faultString = str;
    }

    public void setFaultDetail(Node detail) {
        this.detailRoot = detail;

        NodeList list = detail.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node entry = list.item(i);
            if (entry.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            this.faultDetail = detail;
        }
    }

    public void appendFaultDetail(Node detail) {
        assert faultRoot != null;
        assert detailRoot != null;

        this.detailRoot.appendChild(detail);
        this.faultDetail = detail;
    }

    public Node addFaultDetail() {
        assert faultRoot != null;

        this.detailRoot = XMLUtils.createElementNS(this.faultRoot, XMLConstants.XML_FAULT_DETAIL);
        this.faultRoot.appendChild(this.detailRoot);
        return this.detailRoot;
    }

    public String getFaultString() {
        return this.faultString;
    }

    public Node getFaultDetail() {
        return this.faultDetail;
    }

    public Node getFaultDetailRoot() {
        return this.detailRoot;
    }

    public Node getFaultRoot() {
        return this.faultRoot;
    }

    protected void setFaultRoot(Node root) {
        this.faultRoot = root;
    }

    public void removeChild(Node node) {
        this.faultRoot.removeChild(node);
    }

    public boolean hasChildNodes() {
        return this.faultRoot.hasChildNodes();
    }

}
