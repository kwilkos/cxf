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

package org.apache.cxf.service.model;

import javax.xml.namespace.QName;

public final class MessagePartInfo extends AbstractPropertiesHolder {
    private QName pname;
    private AbstractMessageContainer mInfo;
    
    private boolean isElement;
    private QName typeName;
    private QName elementName;
    private boolean isInSoapHeader;

    MessagePartInfo(QName n, AbstractMessageContainer info) {
        mInfo = info;
        pname = n;
    }

    /**
     * @return Returns the name.
     */
    public QName getName() {
        return pname;
    }
    /**
     * @param name The name to set.
     */
    public void setName(QName n) {
        pname = n;
    }
    
    public QName getConcreteName() {
        if (isElement) {
            return elementName;
        } else {
            return pname;
        }
    }
    
    public boolean isElement() { 
        return isElement;
    }
    public void setIsElement(boolean b) {
        isElement = b;
    }
    
    public QName getElementQName() {
        return elementName;
    }
    public QName getTypeQName() {
        return typeName;
    }
    public void setTypeQName(QName qn) {
        isElement = false;
        typeName = qn;
    }
    public void setElementQName(QName qn) {
        isElement = true;
        elementName = qn;
    }
    
    public AbstractMessageContainer getMessageInfo() {
        return mInfo;
    }

    public boolean isInSoapHeader() {
        return isInSoapHeader;
    }

    public void setInSoapHeader(boolean inSoapHeader) {
        this.isInSoapHeader = inSoapHeader;
    }
}
