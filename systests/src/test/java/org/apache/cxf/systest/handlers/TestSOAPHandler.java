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
package org.apache.cxf.systest.handlers;


import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
//import org.apache.handler_test.PingException;

/**
 * Describe class TestSOAPHandler here.
 *
 *
 * Created: Fri Oct 21 13:24:05 2005
 *
 * @author <a href="mailto:codea@iona.com">codea</a>
 * @version 1.0
 */
public class  TestSOAPHandler<T extends SOAPMessageContext> extends TestHandlerBase 
    implements SOAPHandler<T> {

    public TestSOAPHandler() {
        this(true); 
    } 

    public TestSOAPHandler(boolean serverSide) {
        super(serverSide);
    }

    // Implementation of javax.xml.ws.handler.soap.SOAPHandler

    public final Set<QName> getHeaders() {
        return null;
    }
  
    public String getHandlerId() { 
        return "soapHandler" + getId();
    }
    
    public boolean handleMessage(T ctx) {

        boolean continueProcessing = true; 

        try {
            methodCalled("handleMessage"); 
            printHandlerInfo("handleMessage", isOutbound(ctx));
            Object b  = ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            boolean outbound = (Boolean)b;
            SOAPMessage msg = ctx.getMessage();
            
            if (isServerSideHandler()) {
                if (outbound) {
                    continueProcessing = getReturnValue(outbound, ctx); 
                } else {
                    continueProcessing = getReturnValue(outbound, ctx); 
                    if (!continueProcessing) {
                        outbound = true;
                    }
                }
                
                if (outbound) {
                    try {
                        // append handler id to SOAP response message 
                        SOAPBody body = msg.getSOAPBody(); 
                        Node resp = body.getFirstChild();
                        
                        if (resp.getNodeName().contains("pingResponse")) { 
                            Node child = resp.getFirstChild();
                            Document doc = resp.getOwnerDocument();
                            Node info = doc.createElementNS(child.getNamespaceURI(), child.getLocalName());
                            info.setPrefix("ns4");
                            info.appendChild(doc.createTextNode(getHandlerId()));
                            resp.appendChild(info); 
                            msg.saveChanges();
                        } 
                    } catch (DOMException e) {
                        e.printStackTrace();
                    }
                } else {
                    getHandlerInfoList(ctx).add(getHandlerId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return continueProcessing;
    }

    public final boolean handleFault(T ctx) {
        methodCalled("handleFault"); 
        printHandlerInfo("handleFault", isOutbound(ctx));
        return true;
    }

    public final void init(final Map map) {
        methodCalled("init"); 

    }

    public final void destroy() {
        methodCalled("destroy"); 
    }

    public final void close(MessageContext messageContext) {
        methodCalled("close"); 
    }

    private boolean getReturnValue(boolean outbound, T ctx) { 
        boolean ret = true;
        try {
            SOAPMessage msg  = ctx.getMessage(); 
            SOAPBody body = msg.getSOAPBody();

            if (body.getFirstChild().getFirstChild() == null) {
                return true;
            }

            Node commandNode = body.getFirstChild().getFirstChild().getFirstChild();
            String arg = commandNode.getNodeValue(); 
            String namespace = body.getFirstChild().getFirstChild().getNamespaceURI(); 
            
            StringTokenizer strtok = new StringTokenizer(arg, " ");
            String hid = "";
            String direction = "";
            String command = "";
            if (strtok.countTokens() == 3) {
                hid = strtok.nextToken();
                direction = strtok.nextToken();
                command = strtok.nextToken();
            }
            
            if (getHandlerId().equals(hid) && "stop".equals(command)) {
                if (!outbound && "inbound".equals(direction)) {
                     // remove the incoming request body.
                    Document doc = body.getOwnerDocument(); 
                    // build the SOAP response for this message 
                    //
                    Node wrapper = doc.createElementNS(namespace, "pingResponse");
                    wrapper.setPrefix("ns4");
                    body.removeChild(body.getFirstChild());
                    body.appendChild(wrapper); 

                    for (String info : getHandlerInfoList(ctx)) {
                        // copy the the previously invoked handler list into the response.  
                        // Ignore this handlers information as it will be added again later.
                        //
                        if (!info.contains(getHandlerId())) {
                            Node newEl = doc.createElementNS(namespace, "HandlersInfo");
                            newEl.setPrefix("ns4");
                            newEl.appendChild(doc.createTextNode(info));
                            wrapper.appendChild(newEl); 
                        }
                    }
                    ret = false;
                } else if (outbound && "outbound".equals(direction)) {
                    ret = false;
                }
            } 

        } catch (Exception e) {
            e.printStackTrace();
        }
            
        return ret;
    } 


    public String toString() { 
        return getHandlerId();
    } 
}
