package org.apache.cxf.binding.attachment;

import java.io.IOException;
import java.io.PipedInputStream;

import org.apache.cxf.transport.AbstractCachedOutputStream;

public class CachedOutputStream extends AbstractCachedOutputStream {

    public CachedOutputStream() throws IOException {
        super();
    }

    public CachedOutputStream(PipedInputStream stream) throws IOException {
        super(stream);
    }

    public void onWrite() throws IOException {
    }

    public void doClose() {
    }

    public void doFlush() {
    }

}
