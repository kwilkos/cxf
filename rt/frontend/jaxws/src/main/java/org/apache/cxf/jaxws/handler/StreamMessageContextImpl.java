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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.jaxws.handlers.StreamMessageContext;
import org.apache.cxf.message.Message;



/**
 * Describe class StreamMessageContextImpl here.
 *
 *
 * Created: Thu Nov 17 14:52:48 2005
 *
 * @author <a href="mailto:codea@iona.com">Conrad O'Dea</a>
 * @version 1.0
 */
public class StreamMessageContextImpl extends WrappedMessageContext 
    implements StreamMessageContext {

    private static final Logger LOG = 
        LogUtils.getL7dLogger(StreamMessageContextImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    public StreamMessageContextImpl(Message m) { 
        super(m); 
    }

    public InputStream getInputStream() {   
        InputStream is = getWrappedMessage().getContent(InputStream.class);
        if (is == null) { 
            throw new IllegalStateException(BUNDLE.getString("NO_INPUT_STREAM_EXC"));
        }
        return is;
    } 

    public void setInputStream(InputStream is) { 
        getWrappedMessage().setContent(InputStream.class, is);
    } 

    public OutputStream getOutputStream() { 
        OutputStream os =  getWrappedMessage().getContent(OutputStream.class);
        if (os == null) { 
            throw new IllegalStateException(BUNDLE.getString("NO_OUTPUT_STREAM_EXC"));
        }
        return os;
    } 

    public void setOutputStream(OutputStream os) { 
        getWrappedMessage().setContent(OutputStream.class, os);
    } 
}
