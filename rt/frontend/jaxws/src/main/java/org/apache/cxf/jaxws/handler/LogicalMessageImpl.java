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

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.ws.LogicalMessage;


public class LogicalMessageImpl implements LogicalMessage {

    private final LogicalMessageContextImpl msgContext;
    
    public LogicalMessageImpl(LogicalMessageContextImpl lmctx) {
        msgContext = lmctx;
    }

    public Source getPayload() {
        return msgContext.getWrappedMessage().getContent(Source.class);
    }

    public void setPayload(Source s) {
        msgContext.getWrappedMessage().setContent(Source.class, s);
    }

    public Object getPayload(JAXBContext arg0) {
        // TODO - what to do with JAXB context?
        return msgContext.getWrappedMessage().getContent(List.class).get(0);
    }

    public void setPayload(Object arg0, JAXBContext arg1) {
        // TODO - what to do with JAXB context?
        List<Object> l = new ArrayList<Object>();
        l.add(arg0);        
        msgContext.getWrappedMessage().setContent(List.class, l);
    }

   
}
