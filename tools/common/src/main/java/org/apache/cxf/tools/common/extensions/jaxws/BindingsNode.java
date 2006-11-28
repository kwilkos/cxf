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

package org.apache.cxf.tools.common.extensions.jaxws;

import org.w3c.dom.Element;

public class BindingsNode {

    private String xpathExpression;
    private Class parentType;
    private String nodeName;
    private Element element;

    public Element getElement() {
        return this.element;
    }

    public void setElement(Element elem) {
        this.element = elem;
    }
    
    public Class getParentType() {
        return this.parentType;
    }

    public void setParentType(Class clz) {
        parentType = clz;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nn) {
        this.nodeName = nn;
    }

    public String getXPathExpression() {
        return this.xpathExpression;
    }

    public void setXPathExpression(String xe) {
        this.xpathExpression = xe;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nBindingNode");
        sb.append("[");
        sb.append(xpathExpression);
        sb.append("]");
        sb.append("[");
        sb.append(parentType);
        sb.append("]");
        sb.append("[");
        sb.append(nodeName);
        sb.append("]\n");
        return sb.toString();
    }
}
