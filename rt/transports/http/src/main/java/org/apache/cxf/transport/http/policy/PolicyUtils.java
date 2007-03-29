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

package org.apache.cxf.transport.http.policy;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;
import org.apache.neethi.Assertion;

/**
 * 
 */
public final class PolicyUtils {
    
    public static final String HTTPCONF_NAMESPACE = 
        "http://cxf.apache.org/transports/http/configuration";
    public static final QName HTTPCLIENTPOLICY_ASSERTION_QNAME =
        new QName(HTTPCONF_NAMESPACE, "HTTPClientPolicy");
    public static final QName HTTPSERVERPOLICY_ASSERTION_QNAME =
        new QName(HTTPCONF_NAMESPACE, "HTTPServerPolicy");

    /**
     * Prevents instantiation.
     *
     */
    private PolicyUtils() {        
    }


    /**
     * Returns the HTTPClientPolicy for the specified message or null if no assertions of this type
     * pertain to the underlying message.
     * If on the other hand there is more than one such assertion, the first one is taken.
     * TODO:
     * This should be replaced by computing an merged assertion if this is possible, e.g. one in
     * which the connection timeout is set to be to the minimum of all connections timeouts.
     * If there are conflicting assertions, resulting e.g. from a client element that is included
     * in a policy attached to the endpoint and another client element, included in a policy 
     * attached to the underlying message, in which chunking is disallowed, a exception should be thrown. 
     * 
     * @param message the message
     * @return the HTTPClientPolicy for the message
     */
    public static HTTPClientPolicy getClient(Message message) {
        AssertionInfoMap amap =  message.get(AssertionInfoMap.class);
        if (null == amap) {
            return null;
        }
        Collection<AssertionInfo> ais = amap.get(HTTPCLIENTPOLICY_ASSERTION_QNAME);
        if (null != ais) {
            for (AssertionInfo ai : ais) {
                JaxbAssertion<HTTPClientPolicy> ja = 
                    JaxbAssertion.cast(ai.getAssertion(), HTTPClientPolicy.class);
                return ja.getData();
            }
        }
        return null;
    }
    
    /**
     * Returns the HTTPServerPolicy for the specified message or null if no assertions of this type
     * pertain to the underlying message.
     */
    public static HTTPServerPolicy getServer(Message message) {
        AssertionInfoMap amap =  message.get(AssertionInfoMap.class);
        if (null == amap) {
            return null;
        }
        Collection<AssertionInfo> ais = amap.get(HTTPSERVERPOLICY_ASSERTION_QNAME);
        if (null != ais) {
            for (AssertionInfo ai : ais) {
                JaxbAssertion<HTTPServerPolicy> ja = 
                    JaxbAssertion.cast(ai.getAssertion(), HTTPServerPolicy.class);
                return ja.getData();
            }
        }
        return null;
    }
    
    /**
     * Returns the first HTTPClientPolicy element specified in the http client policy assertions
     * or null if there are no such assertions. 
     * TODO: Return a merged value if there are multiple such assertions, or report conflict
     */
    public static HTTPClientPolicy getClient(Collection<Assertion> alternative) {
        for (Assertion a : alternative) {
            if (HTTPCLIENTPOLICY_ASSERTION_QNAME.equals(a.getName())) {
                return JaxbAssertion.cast(a, HTTPClientPolicy.class).getData();
            }
        }
        return null;
    }
    
    /**
     * Returns the first HTTPServerPolicy element specified in the http client policy assertions
     * or null if there are no such assertions. 
     * TODO: Return a merged value if there are multiple such assertions, or report conflict
     */
    public static HTTPServerPolicy getServer(Collection<Assertion> alternative) {       
        for (Assertion a : alternative) {
            if (HTTPSERVERPOLICY_ASSERTION_QNAME.equals(a.getName())) {
                return JaxbAssertion.cast(a, HTTPServerPolicy.class).getData();
            }
        }
        return null;
    }
    
    public static void assertClientPolicy(PolicyEngine engine, Message message) {
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        if (null == aim) {
            return;
        }
        Collection<AssertionInfo> ais = aim.get(HTTPCLIENTPOLICY_ASSERTION_QNAME);        
        if (null == ais || ais.size() == 0) {
            return;
        }
        
        // assert the endpoint level assertion(s) and any message specific ones that are compatible
        // with these
        
        if (MessageUtils.isOutbound(message)) {
            System.out.println("message is outbound");
            Endpoint e = message.getExchange().get(Endpoint.class);
            EndpointInfo ei = e.getEndpointInfo();
            Collection<Assertion> endpointAssertions = engine.getClientEndpointPolicy(ei, null)
                .getChosenAlternative();
            for (Assertion a : endpointAssertions) {
                if (HTTPCLIENTPOLICY_ASSERTION_QNAME.equals(a.getName())) {
                    HTTPClientPolicy p1 = (JaxbAssertion.cast(a, HTTPClientPolicy.class)).getData();
                    for (AssertionInfo ai : ais) {
                        HTTPClientPolicy p2 = (JaxbAssertion.cast(ai.getAssertion(), 
                                                                  HTTPClientPolicy.class)).getData();
                        if (compatible(p1, p2)) {
                            ai.setAsserted(true);
                        }
                    }
                }
            }
        } else {
            for (AssertionInfo ai : ais) {
                ai.setAsserted(true);
            }
        }
    }
    
    /**
     * Checks if two HTTPClientPolicy objects are compatible.
     * @param p1 one policy
     * @param p2 another policy
     * @return true iff policies are compatible
     */
    public static boolean compatible(HTTPClientPolicy p1, HTTPClientPolicy p2) {
        
        if (p1 == p2 || p1.equals(p2)) {
            return true;
        }
        
        boolean compatible = true;
        
        if (compatible) {
            compatible &= compatible(p1.getAccept(), p2.getAccept());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getAcceptEncoding(), p2.getAcceptEncoding());
        }
           
        if (compatible) {
            compatible &= compatible(p1.getAcceptLanguage(), p2.getAcceptLanguage());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getBrowserType(), p2.getBrowserType());
        }
        
        if (compatible) {
            compatible &= !p1.isSetCacheControl() || !p2.isSetCacheControl()
                || p1.getCacheControl().value().equals(p2.getCacheControl().value());
        }
        
        if (compatible) {            
            compatible = !p1.isSetConnection() || !p2.isSetConnection()
                || p1.getConnection().value().equals(p2.getConnection().value());
        }
        
        if (compatible) {
            compatible &= p1.getContentType().equals(p2.getContentType());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getCookie(), p2.getCookie());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getDecoupledEndpoint(), p2.getDecoupledEndpoint());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getHost(), p2.getHost());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getProxyServer(), p2.getProxyServer());
        }
       
        if (compatible) {
            compatible &= !p1.isSetProxyServerPort() || !p2.isSetProxyServerPort()
                || p1.getProxyServerPort() == p2.getProxyServerPort();
        }
        
        if (compatible) {
            compatible &= !p1.isSetProxyServerType() || !p2.isSetProxyServerType()
                || p1.getProxyServerType().equals(p2.getProxyServerType());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getReferer(), p2.getReferer());
        }
        
        if (compatible) {
            compatible &= p1.isAllowChunking() == p2.isAllowChunking();
        }
        
        if (compatible) {
            compatible &= p1.isAutoRedirect() == p2.isAutoRedirect();
        }
        
        return compatible;
    }
 
    
    /**
     * Returns a new HTTPClientPolicy which is compatible with the two specified policies or
     * null if no compatible policy can be determined.
     * @param p1 one policy
     * @param p2 another policy
     * @return the compatible policy
     */
    public static HTTPClientPolicy intersect(HTTPClientPolicy p1, HTTPClientPolicy p2) {
        
        // incompatibilities
        
        if (!compatible(p1, p2)) {
            return null;
        }
        
        // ok - compute compatible policy
        
        HTTPClientPolicy p = new HTTPClientPolicy();
        p.setAccept(combine(p1.getAccept(), p2.getAccept()));
        p.setAcceptEncoding(combine(p1.getAcceptEncoding(), p2.getAcceptEncoding()));
        p.setAcceptLanguage(combine(p1.getAcceptLanguage(), p2.getAcceptLanguage()));
        if (p1.isSetAllowChunking()) {
            p.setAllowChunking(p1.isAllowChunking());
        } else if (p2.isSetAllowChunking()) {
            p.setAllowChunking(p2.isAllowChunking());
        } 
        if (p1.isSetAutoRedirect()) {
            p.setAutoRedirect(p1.isAutoRedirect());
        } else if (p2.isSetAutoRedirect()) {
            p.setAutoRedirect(p2.isAutoRedirect());
        } 
        p.setAutoRedirect(p1.isAutoRedirect());
        p.setBrowserType(combine(p1.getBrowserType(), p2.getBrowserType()));
        if (p1.isSetCacheControl()) {
            p.setCacheControl(p1.getCacheControl());
        } else if (p2.isSetCacheControl()) {
            p.setCacheControl(p2.getCacheControl());
        }
        if (p1.isSetConnection()) {
            p.setConnection(p1.getConnection());
        } else if (p2.isSetConnection()) {
            p.setConnection(p2.getConnection());
        }        
        p.setContentType(p1.getContentType());
        p.setCookie(combine(p1.getCookie(), p2.getCookie()));
        p.setDecoupledEndpoint(combine(p1.getDecoupledEndpoint(), p2.getDecoupledEndpoint()));
        p.setHost(combine(p1.getHost(), p2.getHost()));
        p.setProxyServer(combine(p1.getProxyServer(), p2.getProxyServer()));
        if (p1.isSetProxyServerPort()) {
            p.setProxyServerPort(p1.getProxyServerPort());
        } else if (p2.isSetProxyServerPort()) {
            p.setProxyServerPort(p2.getProxyServerPort());
        }
        if (p1.isSetProxyServerType()) {
            p.setProxyServerType(p1.getProxyServerType());
        } else if (p2.isSetProxyServerType()) {
            p.setProxyServerType(p2.getProxyServerType());
        }
        p.setReferer(combine(p1.getReferer(), p2.getReferer()));
        if (p1.isSetConnectionTimeout() || p2.isSetConnectionTimeout()) {
            p.setConnectionTimeout(Math.min(p1.getConnectionTimeout(), p2.getConnectionTimeout()));
        }
        if (p1.isSetReceiveTimeout() || p2.isSetReceiveTimeout()) {
            p.setReceiveTimeout(Math.min(p1.getReceiveTimeout(), p2.getReceiveTimeout()));
        }
         
        return p;      
    }
    
    /**
     * Checks if two HTTPServerPolicy objects are compatible.
     * @param p1 one policy
     * @param p2 another policy
     * @return true iff policies are compatible
     */
    public static boolean compatible(HTTPServerPolicy p1, HTTPServerPolicy p2) {
        
        if (p1 == p2 || p1.equals(p2)) {
            return true;
        }
        
        boolean compatible = true;
        
        if (compatible) {
            compatible &= !p1.isSetCacheControl() || !p2.isSetCacheControl()
                || p1.getCacheControl().value().equals(p2.getCacheControl().value());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getContentEncoding(), p2.getContentEncoding());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getContentLocation(), p2.getContentLocation());
        }
        
        if (compatible) {
            compatible &= p1.getContentType().equals(p2.getContentType());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getRedirectURL(), p2.getRedirectURL());
        }
        
        if (compatible) {
            compatible &= compatible(p1.getServerType(), p2.getServerType());
        }
        
        if (compatible) {
            compatible &= p1.isHonorKeepAlive() == p2.isHonorKeepAlive();
        }
        
        if (compatible) {
            compatible &= p1.isSuppressClientReceiveErrors() == p2.isSuppressClientReceiveErrors();
        }
        
        if (compatible) {
            compatible &= p1.isSuppressClientSendErrors() == p2.isSuppressClientSendErrors();
        }
        
        return compatible;
    }
    
    /**
     * Returns a new HTTPServerPolicy which is compatible with the two specified policies or
     * null if no compatible policy can be determined.
     * @param p1 one policy
     * @param p2 another policy
     * @return the compatible policy
     */
    public static HTTPServerPolicy intersect(HTTPServerPolicy p1, HTTPServerPolicy p2) {
                
        if (!compatible(p1, p2)) {
            return null;
        }
        
        HTTPServerPolicy p = new HTTPServerPolicy();
        if (p1.isSetCacheControl()) {
            p.setCacheControl(p1.getCacheControl());
        } else if (p2.isSetCacheControl()) {
            p.setCacheControl(p2.getCacheControl());
        }
        p.setContentEncoding(combine(p1.getContentEncoding(), p2.getContentEncoding()));
        p.setContentLocation(combine(p1.getContentLocation(), p2.getContentLocation()));
        if (p1.isSetContentType()) {
            p.setContentType(p1.getContentType());
        } else if (p2.isSetContentType()) {
            p.setContentType(p2.getContentType());
        }     
        if (p1.isSetHonorKeepAlive()) {
            p.setHonorKeepAlive(p1.isHonorKeepAlive());
        } else if (p2.isSetHonorKeepAlive()) {
            p.setHonorKeepAlive(p2.isHonorKeepAlive());
        } 
        if (p1.isSetReceiveTimeout() || p2.isSetReceiveTimeout()) {
            p.setReceiveTimeout(Math.min(p1.getReceiveTimeout(), p2.getReceiveTimeout()));
        }
        p.setRedirectURL(combine(p1.getRedirectURL(), p2.getRedirectURL()));
        p.setServerType(combine(p1.getServerType(), p2.getServerType()));
        if (p1.isSetSuppressClientReceiveErrors()) {
            p.setSuppressClientReceiveErrors(p1.isSuppressClientReceiveErrors());
        } else if (p2.isSetSuppressClientReceiveErrors()) {
            p.setSuppressClientReceiveErrors(p2.isSuppressClientReceiveErrors());
        }
        if (p1.isSetSuppressClientSendErrors()) {
            p.setSuppressClientSendErrors(p1.isSuppressClientSendErrors());
        } else if (p2.isSetSuppressClientSendErrors()) {
            p.setSuppressClientSendErrors(p2.isSuppressClientSendErrors());
        } 
        
        return p;
    }
    
    private static String combine(String s1, String s2) {
        return s1 == null ? s2 : s1;
    }
    
    private static boolean compatible(String s1, String s2) {
        return s1 == null || s2 == null || s1.equals(s2);
    }
}
