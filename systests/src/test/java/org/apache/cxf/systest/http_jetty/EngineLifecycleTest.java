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
package org.apache.cxf.systest.http_jetty;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;


import org.apache.cxf.helpers.IOUtils;

import org.apache.cxf.io.CachedOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;



/**
 * This class tests starting up and shutting down the embedded server when there
 * is extra jetty configuration.
 */
public class EngineLifecycleTest extends Assert {
    private GenericApplicationContext applicationContext;
    
    private void readBeans(Resource beanResource) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        reader.loadBeanDefinitions(beanResource);
    }
    
    public void setUpBus() throws Exception {
        applicationContext = new GenericApplicationContext();
        readBeans(new ClassPathResource("META-INF/cxf/cxf.xml"));
        readBeans(new ClassPathResource("META-INF/cxf/cxf-extension-soap.xml"));
        readBeans(new ClassPathResource("META-INF/cxf/cxf-extension-http.xml"));
        readBeans(new ClassPathResource("META-INF/cxf/cxf-extension-http-jetty.xml"));
        readBeans(new ClassPathResource("jetty-engine.xml", getClass()));
        
        
        // bring in some property values from a Properties file
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("staticResourceURL", getStaticResourceURL());
        cfg.setProperties(properties);
        // now actually do the replacement
        cfg.postProcessBeanFactory(applicationContext.getBeanFactory());        
        applicationContext.refresh();
    }
    
    public void launchService() throws Exception {
        applicationContext = new GenericApplicationContext();
        readBeans(new ClassPathResource("server-lifecycle-beans.xml", getClass()));
        applicationContext.refresh();
    }
    
    private void invokeService() {        
        DummyInterface client = (DummyInterface) applicationContext.getBean("dummy-client");
        assertEquals("We should get out put from this client", "hello world", client.echo("hello world"));
    }
    
    private HttpURLConnection getHttpConnection(String target) throws Exception {
        URL url = new URL(target);       
        
        URLConnection connection = url.openConnection();            
        
        assertTrue(connection instanceof HttpURLConnection);
        return (HttpURLConnection)connection;        
    }
    
    private void getTestHtml() throws Exception {
        HttpURLConnection httpConnection = 
            getHttpConnection("http://localhost:8808/test.html");    
        httpConnection.connect();
        InputStream in = httpConnection.getInputStream();        
        assertNotNull(in);
        CachedOutputStream response = new CachedOutputStream();
        IOUtils.copy(in, response);
        in.close();
        response.close();
              
        FileInputStream htmlFile = 
            new FileInputStream("target/test-classes/org/apache/cxf/systest/http_jetty/test.html");    
        CachedOutputStream html = new CachedOutputStream();
        IOUtils.copy(htmlFile, html);
        htmlFile.close();
        html.close();
        
        assertEquals("Can't get the right test html", html.toString(), response.toString());
        
        
        
    }
    
    public String getStaticResourceURL() throws Exception {
        File staticFile = new File(this.getClass().getResource("test.html").toURI());
        staticFile = staticFile.getParentFile();
        staticFile = staticFile.getAbsoluteFile();
        URL furl = staticFile.toURL();
        return furl.toString();
    }

    public void shutdownService() throws Exception {        
        applicationContext.destroy();
        applicationContext.close();        
    }

    @Test
    public void testServerUpDownUp() throws Exception {        
        setUpBus();
        launchService();
        shutdownService();

        launchService();
        invokeService();            
        getTestHtml();
        
        shutdownService();
    }

}
