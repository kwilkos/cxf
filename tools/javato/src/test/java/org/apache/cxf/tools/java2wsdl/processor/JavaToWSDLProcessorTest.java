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

package org.apache.cxf.tools.java2wsdl.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

import org.apache.cxf.helpers.WSDLHelper;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.helpers.XPathUtils;
import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;
import org.apache.cxf.tools.common.extensions.soap.SoapBinding;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;
import org.apache.cxf.tools.wsdlto.core.FrontEndProfile;
import org.apache.cxf.tools.wsdlto.core.PluginLoader;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.JAXWSContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JavaToWSDLProcessorTest extends ProcessorTestBase {

    private JavaToWSDLProcessor j2wProcessor;
    private JAXWSContainer wj2Processor;
    private String tns = "org.apache.asyn_lit";
    private String serviceName = "cxfService";
    private WSDLHelper wsdlHelper = new WSDLHelper();
    private File classFile;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        j2wProcessor = new JavaToWSDLProcessor();
        classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        wj2Processor = new JAXWSContainer(null);
        env.put(FrontEndProfile.class, PluginLoader.getInstance().getFrontEndProfile("jaxws"));
        env.put(DataBindingProfile.class, PluginLoader.getInstance().getDataBindingProfile("jaxb"));        
    }

    @After
    public void tearDown() {
        super.tearDown();
        j2wProcessor = null;
        wj2Processor = null;
    }

    @Test
    public void testAsync() throws Exception {

        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/java2wsdl_wsdl/hello_world_async.wsdl"));
        wj2Processor.setContext(env);
        wj2Processor.execute();

        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/asyn.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_async_soap_http.GreeterAsync");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

        File wsdlFile = new File(output, "asyn.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Fail to generate schema file", schemaFile.exists());

    }

    @Test
    public void testDocWrapparBare() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/java2wsdl_wsdl/hello_world_doc_wrapped_bare.wsdl"));
        wj2Processor.setContext(env);
        wj2Processor.execute();

        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped_bare.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_doc_wrapped_bare.Greeter");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

        File wsdlFile = new File(output, "doc_wrapped_bare.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Fail to generate schema file", schemaFile.exists());

    }

    @Test
    public void testDocLitUseClassPathFlag() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/java2wsdl_wsdl/hello_world_doc_lit.wsdl"));
        wj2Processor.setContext(env);
        wj2Processor.execute();

        System.setProperty("java.class.path", "");
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_doc_lit.Greeter");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_CLASSPATH, classFile.getCanonicalPath());
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "doc_lit.wsdl");
        assertTrue("Generate Wsdl Fail", wsdlFile.exists());

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue("Generate schema file Fail", schemaFile.exists());

    }

    @Test
    public void testRPCLit() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/java2wsdl_wsdl/hello_world_rpc_lit.wsdl"));
        wj2Processor.setContext(env);
        wj2Processor.execute();

        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/rpc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_rpclit.GreeterRPCLit");
        env.put(ToolConstants.CFG_TNS, tns);
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "rpc_lit.wsdl");
        assertTrue(wsdlFile.exists());

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(tns, serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue(schemaFile.exists());
        File schemaFile2 = new File(output, "schema2.xsd");
        assertTrue(schemaFile2.exists());

        Binding binding = def.getBinding(new QName(def.getTargetNamespace(), "GreeterRPCLitBinding"));
        assertNotNull(binding);
        Iterator it = binding.getExtensibilityElements().iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(SOAPBindingUtil.isSOAPBinding(obj));
            assertTrue(obj instanceof SOAPBinding);
            SoapBinding soapBinding = SOAPBindingUtil.getSoapBinding(obj);
            assertNotNull(soapBinding);
            assertTrue("rpc".equalsIgnoreCase(soapBinding.getStyle()));
            assertTrue(WSDLConstants.SOAP_HTTP_TRANSPORT.equalsIgnoreCase(soapBinding.getTransportURI()));
        }
        Port port = wsdlService.getPort("GreeterRPCLitPort");
        assertNotNull(port);

        it = port.getExtensibilityElements().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(SOAPBindingUtil.isSOAPAddress(obj));
            assertTrue(obj instanceof SOAPAddress);
            assertEquals("http://localhost:9000/cxfService", ((SOAPAddress)obj).getLocationURI());
        }
    }

    @Test
    public void testSOAP12() throws Exception {
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/java2wsdl_wsdl/hello_world_soap12.wsdl"));
        wj2Processor.setContext(env);
        wj2Processor.execute();

        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/soap12.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.hello_world_soap12_http.Greeter");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        env.put(ToolConstants.CFG_SOAP12, "soap12");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "soap12.wsdl");
        assertTrue(wsdlFile.exists());
        assertTrue("WSDL file: " + wsdlFile.toString() + " is empty", wsdlFile.length() > 0);

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(def.getTargetNamespace(), serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);

        File schemaFile = new File(output, "schema1.xsd");
        assertTrue(schemaFile.exists());
        Binding binding = def.getBinding(new QName(def.getTargetNamespace(), "GreeterBinding"));
        assertNotNull(binding);

        Iterator it = binding.getExtensibilityElements().iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(SOAPBindingUtil.isSOAPBinding(obj));
            assertTrue(obj instanceof SOAP12Binding);
            SoapBinding soapBinding = SOAPBindingUtil.getSoapBinding(obj);
            assertNotNull(soapBinding);
            assertTrue("document".equalsIgnoreCase(soapBinding.getStyle()));
            assertTrue(WSDLConstants.SOAP12_HTTP_TRANSPORT.equalsIgnoreCase(soapBinding.getTransportURI()));
        }
        Port port = wsdlService.getPort("GreeterPort");
        assertNotNull(port);

        it = port.getExtensibilityElements().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(SOAPBindingUtil.isSOAPAddress(obj));
            assertTrue(obj instanceof SOAP12Address);
            assertEquals("http://localhost:9000/cxfService", ((SOAP12Address)obj).getLocationURI());
        }
    }
    
    @Test
    public void testRPCWithoutParentBindingAnnotation() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/rpc_lit_service_no_anno.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.withannotation.rpc.Hello");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
        File wsdlFile = new File(output, "rpc_lit_service_no_anno.wsdl");
        assertTrue(wsdlFile.exists());
        assertTrue("WSDL file: " + wsdlFile.toString() + " is empty", wsdlFile.length() > 0);

        Definition def = wsdlHelper.getDefinition(wsdlFile);
        Service wsdlService = def.getService(new QName(def.getTargetNamespace(), serviceName));
        assertNotNull("Generate WSDL Service Error", wsdlService);
        Binding binding = def.getBinding(new QName(def.getTargetNamespace(), "HelloBinding"));
        assertNotNull(binding);

        Iterator it = binding.getExtensibilityElements().iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(SOAPBindingUtil.isSOAPBinding(obj));
            assertTrue(obj instanceof SOAPBinding);
            SoapBinding soapBinding = SOAPBindingUtil.getSoapBinding(obj);
            assertNotNull(soapBinding);
            assertTrue("rpc".equalsIgnoreCase(soapBinding.getStyle()));
            assertTrue(WSDLConstants.SOAP_HTTP_TRANSPORT.equalsIgnoreCase(soapBinding.getTransportURI()));
        }
    }
    
    @Test
    public void testDocWrappedWithoutWrapperClass() {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit_wrapped_no_anno.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.withannotation.doc.HelloWrapped");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {        
            j2wProcessor.process();
        } catch (ToolException e) {
            String expected = "org.apache.cxf.tools.fortest.withannotation.doc.jaxws.SayHi";
            assertTrue(e.getMessage().contains(expected));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }    
    
    @Test
    public void testDocWrappedWithWrapperClass() {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit_wrapped_no_anno_nowrapper11.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.withannotation.doc.Hello");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {
            j2wProcessor.process();
            File wsdlFile = new File(output, "doc_lit_wrapped_no_anno_nowrapper11.wsdl");
            assertTrue(wsdlFile.exists());
            assertTrue("WSDL file: " + wsdlFile.toString() + " is empty", wsdlFile.length() > 0);
        } catch (ToolException e) {
            String expected = "org.apache.cxf.tools.fortest.withannotation.doc.jaxws.SayHi";
            assertTrue(e.getMessage().contains(expected));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }
    
    @Test
    public void testSOAPBindingRPCOnMethod() {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/rpc_on_method.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, 
                "org.apache.cxf.tools.fortest.withannotation.rpc.HelloWrongAnnotation");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {        
            j2wProcessor.process();
        } catch (ToolException e) {
            String expected = "Method [sayHi] processing error : SOAPBinding annotation " 
                + "can not be placed on method with RPC style";
            assertTrue(e.getMessage().contains(expected));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }

    @Test
    public void testDocWrappedWithLocalName() {
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("xsd", "http://www.w3.org/2001/XMLSchema");
        
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit_wrapped_localName.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.withannotation.doc.Stock");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {        
            j2wProcessor.process();
            File file = new File(output, "schema1.xsd");
            assertTrue(file.exists());
            Document root = XMLUtils.parse(new BufferedInputStream(new FileInputStream(file)));
            XPathUtils xpather = new XPathUtils(ns);
            assertNull(xpather.getValue("/xsd:schema/xsd:element[@name='xXx']",
                                        root,
                                        XPathConstants.NODE));
            assertNotNull(xpather.getValue("/xsd:schema/xsd:element[@name='getPrice']",
                                           root,
                                           XPathConstants.NODE));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }

    @Test
    public void testDocWrappedNoWebParam() {
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("xsd", "http://www.w3.org/2001/XMLSchema");
        
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit_wrapped_webparam.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME,
                "org.apache.cxf.tools.fortest.withannotation.doc.HelloWithNoWebParam");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {        
            j2wProcessor.process();
            File file = new File(output, "schema1.xsd");
            assertTrue(file.exists());
            Document root = XMLUtils.parse(new BufferedInputStream(new FileInputStream(file)));
            XPathUtils xpather = new XPathUtils(ns);
            assertNotNull(xpather.getValue("//xsd:complexType[@name='sayHi']//xsd:element[@name='arg0']",
                                        root,
                                        XPathConstants.NODE));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }

    @Test
    public void testSoapHeader() throws Exception {
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        ns.put("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
        ns.put("tns", "http://apache.org/samples/headers");
        
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/soap_header.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME,
                "org.apache.samples.headers.HeaderTester");
        env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        j2wProcessor.setEnvironment(env);
        try {        
            j2wProcessor.process();
            File file = new File(output, "soap_header.wsdl");
            assertTrue(file.exists());
            Document root = XMLUtils.parse(new BufferedInputStream(new FileInputStream(file)));
            XPathUtils xpather = new XPathUtils(ns);
            assertNotNull(xpather.getValue("//wsdl:input[@name='inHeader']//soap:header[@message='inHeader']",
                                        root,
                                        XPathConstants.NODE));

            assertNotNull(xpather.getValue("//wsdl:input[@name='inoutHeader']"
                                           + "//soap:header[@message='inoutHeader']",
                                        root,
                                        XPathConstants.NODE));

            // The generated namespace prefix of the message attribute
            // can differ between the ibm and sun jdks, so only check
            // the local name part.
            Object obj = xpather.getValue("//wsdl:output[@name='inoutHeaderResponse']"
                                           + "//soap:header[@message]",
                                        root,
                                        XPathConstants.NODE);
            assertNotNull(obj);
            if (obj instanceof org.w3c.dom.Element) {
                org.w3c.dom.Attr attr = ((org.w3c.dom.Element)obj).getAttributeNode("message");
                assertNotNull(attr);
                assertTrue("Failed to find output message", attr.getValue().endsWith(":inoutHeaderResponse"));
            } else {
                fail("Expected Element type");
            }
            obj = xpather.getValue("//wsdl:output[@name='outHeaderResponse']"
                                           + "//soap:header[@message]",
                                        root,
                                        XPathConstants.NODE);
            assertNotNull(obj);
            if (obj instanceof org.w3c.dom.Element) {
                org.w3c.dom.Attr attr = ((org.w3c.dom.Element)obj).getAttributeNode("message");
                assertNotNull(attr);
                assertTrue("Failed to find output message", attr.getValue().endsWith(":outHeaderResponse"));
            } else {
                fail("Expected Element type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not happen other exception " + e.getMessage());
        }
    }
    
    @Test
    public void testCXF188() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/cxf188.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.cxf188.Demo");
        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

        File wsdlFile = new File(output, "cxf188.wsdl");
        assertTrue("Fail to generate wsdl file", wsdlFile.exists());

        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
        env.put(ToolConstants.CFG_WSDLURL, output.getCanonicalPath() + "/cxf188.wsdl");
        wj2Processor.setContext(env);
        wj2Processor.execute();
    }

    @Test
    @Ignore
    public void testHolder() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/holder.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.holder.HolderService");
        j2wProcessor.setEnvironment(env);
        try {
            j2wProcessor.process();
            File wsdlFile = new File(output, "holder.wsdl");
            assertTrue(wsdlFile.exists());
            assertTrue("WSDL file: " + wsdlFile.toString() + " is empty", wsdlFile.length() > 0);
        } catch (ToolException e) {
            String expected = "org.apache.cxf.tools.fortest.holder.HolderService";
            assertTrue(e.getMessage().contains(expected));
        } catch (Exception e) {
            fail("Should not happen other exception " + e.getMessage());
        }
    }
}
