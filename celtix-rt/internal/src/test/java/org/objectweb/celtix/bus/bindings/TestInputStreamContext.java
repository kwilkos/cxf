/*
 * TestInputStreamContext.java
 *
 * Created on 26 September 2005, 12:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.objectweb.celtix.bus.bindings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;


/**
 *
 * @author apaibir
 */
public class TestInputStreamContext
    extends GenericMessageContext
    implements InputStreamMessageContext {

    private static final long serialVersionUID = 1L;
    private final byte[] byteArray;
    private InputStream inputStream;
    
    public TestInputStreamContext() {
        this(null);
    }
    
    public TestInputStreamContext(byte[] bArray) {
        byteArray = bArray;
        // put(ObjectMessageContext.MESSAGE_INPUT, false);
    }

    public InputStream getInputStream() {
        if (null != inputStream) {
            return inputStream;
        }
        if (null != byteArray) {
            return new ByteArrayInputStream(byteArray);
        }
        return null;
    }

    public void setInputStream(InputStream ins) {
        inputStream = ins;
    }

    public boolean isFault() { 
        return false; 
    } 

    public void setFault(boolean b) {
    } 
}
