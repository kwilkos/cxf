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


import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.jaxws.handlers.StreamHandler;
import org.apache.cxf.jaxws.handlers.StreamMessageContext;
import org.apache.handler_test.PingException;

public class  TestStreamHandler extends TestHandlerBase 
    implements StreamHandler {

    private static final Logger LOG = Logger.getLogger(TestStreamHandler.class.getName()); 

    public TestStreamHandler() {
        this(true); 
    } 

    public TestStreamHandler(boolean serverSide) {
        super(serverSide);
    }

    public String getHandlerId() { 
        return "streamHandler" + getId();
    }
    
    public boolean handleMessage(StreamMessageContext ctx) {

        methodCalled("handleMessage"); 
        printHandlerInfo("handleMessage", isOutbound(ctx));

        if (isServerSideHandler()) { 
            try {
                Object wsdlDescription = ctx.get(MessageContext.WSDL_DESCRIPTION);
                if (wsdlDescription == null 
                    || (!((wsdlDescription instanceof java.net.URI)
                        || (wsdlDescription instanceof java.net.URL)))) {
                    throw new PingException("WSDLDescription not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isOutbound(ctx)) { 
                getHandlerInfoList(ctx).add(getHandlerId());
            } else { 
                LOG.info("compressing message stream");
                // compress outbound on server side
                setupCompressionOutputStream(ctx);
            } 
        } else {  
            if (!isOutbound(ctx)) { 
                LOG.info("decompressing message stream");
                // decompress inbound on client side
                setupDecompressionInputStream(ctx); 
            } 
        } 
        return true;
    }


    public final boolean handleFault(StreamMessageContext ctx) {
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


    public String toString() { 
        return getHandlerId();
    } 

    private void setupDecompressionInputStream(StreamMessageContext ctx) { 
        try { 
            
            GZIPInputStream zipIn = new GZIPInputStream(ctx.getInputStream());
            ctx.setInputStream(zipIn); 
        } catch (IOException ex) { 
            throw new ProtocolException(ex);
        }
    } 

    private void setupCompressionOutputStream(StreamMessageContext ctx) { 

        try { 
            GZIPOutputStream zipOut = new GZIPOutputStream(ctx.getOutputStream());
            ctx.setOutputStream(zipOut); 
        } catch (IOException ex) { 
            throw new ProtocolException(ex);
        }
    } 
}
