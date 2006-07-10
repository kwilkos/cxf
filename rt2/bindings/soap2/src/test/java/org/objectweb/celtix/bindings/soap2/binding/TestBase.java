package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;
import org.objectweb.celtix.bindings.soap2.utils.StaxUtils;
import org.objectweb.celtix.rio.phase.Phase;
import org.objectweb.celtix.rio.phase.PhaseInterceptorChain;
import org.objectweb.celtix.rio.soap.SoapMessage;

public class TestBase extends TestCase {

    protected PhaseInterceptorChain chain;
    protected SoapMessage soapMessage;
    
    public void setUp() throws Exception {
        List<Phase> phases = new ArrayList<Phase>();
        Phase phase1 = new Phase("phase1", 1);
        phases.add(phase1);
        chain = new PhaseInterceptorChain(phases);
    }

    public void tearDown() {
    }

    public InputStream getTestStream(Class clz, String file) {
        return clz.getResourceAsStream(file);
    }

    public XMLStreamReader getXMLStreamReader(InputStream is) {
        return StaxUtils.createXMLStreamReader(is);
    }
}
