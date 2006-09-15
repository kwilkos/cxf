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

package org.apache.cxf.interceptor;

import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;

/**
 * A Fault that occurs during invocation processing.
 */
public class Fault extends UncheckedException {

    private Element detail;
    
    public Fault(Message message, Throwable throwable) {
        super(message, throwable);
    }
    
    public Fault(Message message) {
        super(message);
    }

    public Fault(Throwable t) {
        super(t);
    }

    
    /**
     * Returns the detail node. If no detail node has been set, an empty
     * <code>&lt;detail&gt;</code> is created.
     * 
     * @return the detail node.
     */
    public Element getDetail() {
        return detail;
    }

    /**
     * Sets a details <code>Node</code> on this fault.
     * 
     * @param details the detail node.
     */
    public void setDetail(Element details) {
        detail = details;
    }

    /**
     * Indicates whether this fault has a detail message.
     * 
     * @return <code>true</code> if this fault has a detail message;
     *         <code>false</code> otherwise.
     */
    public boolean hasDetails() {
        return detail == null ? false : true;
    }

}
