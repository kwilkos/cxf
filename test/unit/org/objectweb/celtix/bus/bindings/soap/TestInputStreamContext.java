/*
 * TestInputStreamContext.java
 *
 * Created on 26 September 2005, 12:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private byte[] byteArray;
    public TestInputStreamContext(byte[] bArray) throws IOException {
        byteArray = bArray;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(byteArray);
    }

    public void setInputStream(InputStream ins) { }

}
