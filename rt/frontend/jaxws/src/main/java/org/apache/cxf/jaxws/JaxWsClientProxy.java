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

package org.apache.cxf.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
//TODO JAX-WS 2.1
//import javax.xml.ws.EndpointReference;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Node;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.service.model.BindingOperationInfo;

public class JaxWsClientProxy extends org.apache.cxf.frontend.ClientProxy implements
    InvocationHandler, BindingProvider {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxWsClientProxy.class);

    protected ThreadLocal <Map<String, Object>> requestContext = 
            new ThreadLocal<Map<String, Object>>();
    protected ThreadLocal <Map<String, Object>> responseContext =
            new ThreadLocal<Map<String, Object>>();

    private final Binding binding;

    public JaxWsClientProxy(Client c, Binding b) {
        super(c);
        this.binding = b;
        setupEndpointAddressContext(getClient().getEndpoint());
    }

    private void setupEndpointAddressContext(Endpoint endpoint) {
        // NOTE for jms transport the address would be null
        if (null != endpoint && null != endpoint.getEndpointInfo().getAddress()) {
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                                    endpoint.getEndpointInfo().getAddress());
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Endpoint endpoint = getClient().getEndpoint();
        MethodDispatcher dispatcher = (MethodDispatcher)endpoint.getService().get(
                                                                                  MethodDispatcher.class
                                                                                      .getName());
        BindingOperationInfo oi = dispatcher.getBindingOperation(method, endpoint);
        if (oi == null) {
            // check for method on BindingProvider and Object
            if (method.getDeclaringClass().equals(BindingProvider.class)
                || method.getDeclaringClass().equals(BindingProviderImpl.class)
                || method.getDeclaringClass().equals(Object.class)) {
                return method.invoke(this);
            }

            Message msg = new Message("NO_OPERATION_INFO", LOG, method.getName());
            throw new WebServiceException(msg.toString());
        }

        Object[] params = args;
        if (null == params) {
            params = new Object[0];
        }
        Map<String, Object> reqContext = new HashMap<String, Object>(this.getRequestContext());        
        Map<String, Object> respContext = this.getResponseContext();
        
        // Clear the response context's hold information
        // Not call the clear Context is to avoid the error 
        // that getResponseContext() could be called by Client code first
        respContext.clear();
        
        Map<String, Object> context = new HashMap<String, Object>();

        // need to do context mapping from jax-ws to cxf message
        ContextPropertiesMapping.mapRequestfromJaxws2Cxf(reqContext);

        context.put(Client.REQUEST_CONTEXT, reqContext);
        context.put(Client.RESPONSE_CONTEXT, respContext);
       
        reqContext.put(Method.class.getName(), method);
        reqContext.put(Client.REQUEST_METHOD, method);
        boolean isAsync = method.getName().endsWith("Async");

        Object result = null;
        try {
            if (isAsync) {
                result = invokeAsync(method, oi, params, context);
            } else {
                result = invokeSync(method, oi, params, context);
            }
        } catch (WebServiceException wex) {
            throw wex.fillInStackTrace();
        } catch (Exception ex) {
            for (Class<?> excls : method.getExceptionTypes()) {
                if (excls.isInstance(ex)) {
                    throw ex.fillInStackTrace();
                }
            }
            
            if (getBinding() instanceof HTTPBinding) {
                HTTPException exception = new HTTPException(HttpURLConnection.HTTP_INTERNAL_ERROR);
                exception.initCause(ex);
                throw exception;
            } else if (getBinding() instanceof SOAPBinding) {
                SOAPFault soapFault = ((SOAPBinding)getBinding()).getSOAPFactory().createFault();
                
                if (ex instanceof SoapFault) {
                    soapFault.setFaultString(((SoapFault)ex).getReason());
                    soapFault.setFaultCode(((SoapFault)ex).getFaultCode());

                    Node nd = soapFault.getOwnerDocument().importNode(((SoapFault)ex).getOrCreateDetail(),
                                                                      true);
                    soapFault.addDetail().appendChild(nd);
 
                } else {
                    soapFault.setFaultCode(new QName("http://cxf.apache.org/faultcode", "HandlerFault"));
                    soapFault.setFaultString(ex.getMessage());
                }      
  
                SOAPFaultException  exception = new SOAPFaultException(soapFault);
                exception.initCause(ex);
                throw exception;                
            } else {
                throw new WebServiceException(ex);
            }
        } finally {
            if (endpoint != getClient().getEndpoint()) {
                setupEndpointAddressContext(getClient().getEndpoint());
            }
        }
        
        // need to do context mapping from cxf message to jax-ws
        ContextPropertiesMapping.mapResponsefromCxf2Jaxws(respContext);
        return result;

    }

    private Object invokeAsync(Method method, BindingOperationInfo oi, Object[] params,
                               Map<String, Object> context) {

        FutureTask<Object> f = new FutureTask<Object>(new JAXWSAsyncCallable(this, method, oi, params,
                                                                             context));

        Endpoint endpoint = getClient().getEndpoint();
        endpoint.getExecutor().execute(f);
        Response<?> r = new AsyncResponse<Object>(f, Object.class);
        if (params.length > 0 && params[params.length - 1] instanceof AsyncHandler) {
            // callback style
            AsyncCallbackFuture callback = 
                new AsyncCallbackFuture(r, (AsyncHandler)params[params.length - 1]);
            endpoint.getExecutor().execute(callback);
            return callback;
        } else {
            return r;
        }
    }


    public Map<String, Object> getRequestContext() {
        if (null == requestContext.get()) {
            requestContext.set(new HashMap<String, Object>());
        }
        return requestContext.get();
    }

    public Map<String, Object> getResponseContext() {
        if (null == responseContext.get()) {
            responseContext.set(new HashMap<String, Object>());
        }        
        return responseContext.get();
    }

    public Binding getBinding() {
        return binding;
    }

    protected void populateResponseContext(MessageContext ctx) {
        Iterator<String> iter = ctx.keySet().iterator();
        Map<String, Object> respCtx = getResponseContext();
        while (iter.hasNext()) {
            String obj = iter.next();
            if (MessageContext.Scope.APPLICATION.compareTo(ctx.getScope(obj)) == 0) {
                respCtx.put(obj, ctx.get(obj));
            }
        }
    }

    
    /*
    //  TODO JAX-WS 2.1
    public EndpointReference getEndpointReference() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
        // TODO
        throw new UnsupportedOperationException();
    }    
    */
}
