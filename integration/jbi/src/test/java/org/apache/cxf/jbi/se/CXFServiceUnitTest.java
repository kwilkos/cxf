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

package org.apache.cxf.jbi.se;

import java.net.URL;
import java.util.logging.Logger;
 
import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
 
import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.jbi.transport.JBITransportFactory;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.easymock.classextension.EasyMock;


public class CXFServiceUnitTest extends TestCase {

    private static final Logger LOG = LogUtils.getL7dLogger(CXFServiceUnitTest.class);
    private static final String ROOT_PATH =
        "/service-assemblies/cxf-demo-service-assembly/version_1/sus/"
            + "CXFServiceEngine/JBIDemoSE_AProvider";
    private static final String CXF_CONFIG =
        "/components/CXFServiceEngine/version_1/META-INF/cxf-config.xml";
    private CXFServiceUnit csu;
    private CXFServiceUnitManager csuManager;
    private ComponentContext ctx = EasyMock.createMock(ComponentContext.class);
    private DeliveryChannel channel = EasyMock.createMock(DeliveryChannel.class);
    private String absCsuPath;
    private Bus bus;
    private JBITransportFactory jbiTransportFactory;
     
    public void setUp() throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        absCsuPath = getClass().getResource(ROOT_PATH).getFile();
        
        System.setProperty(Configurer.USER_CFG_FILE_PROPERTY_NAME,
            getClass().getResource(CXF_CONFIG).toString());
        bus = new SpringBusFactory().createBus();
              
        ComponentClassLoader componentClassLoader =
            new ComponentClassLoader(new URL[0], getClass().getClassLoader());
          
        csuManager = new CXFServiceUnitManager(bus, ctx, componentClassLoader);
         
        csu = new CXFServiceUnit(bus, absCsuPath, componentClassLoader);
        registerJBITransport(bus, csuManager);
        channel.accept();
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.expectLastCall().anyTimes();
         
        EasyMock.replay(channel);
    }
     
    public void tearDown() throws Exception {
        bus.shutdown(false);
    }
     
    public void testPrepare() throws Exception {
        csu.prepare(ctx);
        assertTrue(csu.isServiceProvider());
        assertEquals(csu.getServiceName().getNamespaceURI(), "http://apache.org/hello_world");
        assertEquals(csu.getServiceName().getLocalPart(), "HelloWorldService");
    }
     
    public void testEndpintName() throws Exception {
        assertEquals(csu.getEndpointName(), "SE_Endpoint");
    }
     
    public void testServiceName() throws Exception {
        csu.prepare(ctx);
        assertEquals(csu.getServiceName().getNamespaceURI(), "http://apache.org/hello_world");
        assertEquals(csu.getServiceName().getLocalPart(), "HelloWorldService");
    }
     
    public void testStart() throws Exception {
         
    }
     
    public void testStop() throws Exception {
         
    }
     
    void registerJBITransport(Bus argBus, CXFServiceUnitManager mgr) throws JBIException { 
        try { 
            getTransportFactory().setBus(argBus);
            getTransportFactory().setServiceUnitManager(mgr);
        } catch (Exception ex) {
            LOG.severe(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString());
            throw new JBIException(new Message("SE.FAILED.REGISTER.TRANSPORT.FACTORY", 
                                               LOG).toString(), ex);
        }
    }
    protected JBITransportFactory getTransportFactory() throws JBIException, BusException {
        assert bus != null;
        
        if (jbiTransportFactory  == null) {
            jbiTransportFactory = (JBITransportFactory)bus.getExtension(ConduitInitiatorManager.class).
                getConduitInitiator(CXFServiceEngine.JBI_TRANSPORT_ID);
            jbiTransportFactory.setBus(bus);
            jbiTransportFactory.setDeliveryChannel(ctx.getDeliveryChannel());
            
        }
        return jbiTransportFactory;
    }

    
}
