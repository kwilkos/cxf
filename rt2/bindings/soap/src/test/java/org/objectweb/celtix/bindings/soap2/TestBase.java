package org.objectweb.celtix.bindings.soap2;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.utils.StaxUtils;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.servicemodel.JAXWSClassServiceBuilder;
import org.objectweb.celtix.servicemodel.Service;

public class TestBase extends TestCase {

    protected PhaseInterceptorChain chain;
    protected SoapMessage soapMessage;
    
    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        phases.add(phase1);
        phases.add(phase2);
        chain = new PhaseInterceptorChain(phases);

        soapMessage = TestUtil.createEmptySoapMessage(new Soap11(), chain);
    }

    public void tearDown() throws Exception {
    }

    public InputStream getTestStream(Class clz, String file) {
        return clz.getResourceAsStream(file);
    }

    public XMLStreamReader getXMLStreamReader(InputStream is) {
        return StaxUtils.createXMLStreamReader(is);
    }

    public XMLStreamWriter getXMLStreamWriter(OutputStream os) {
        return StaxUtils.createXMLStreamWriter(os);
    }

    public Service getTestService(Class clz) {
        return JAXWSClassServiceBuilder.buildService(clz);
    }

    protected JAXBDataBindingCallback getTestCallback(Class clz, String methodName) throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(clz);
        Method m = org.objectweb.celtix.testutil.common.TestUtil.getMethod(clz,
                                                                           methodName);
        return new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
    }
}
