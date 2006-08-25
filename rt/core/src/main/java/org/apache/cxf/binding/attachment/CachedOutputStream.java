package org.apache.cxf.binding.attachment;

import java.io.IOException;

import org.apache.cxf.transport.AbstractCachedOutputStream;

public class CachedOutputStream extends AbstractCachedOutputStream {

    public CachedOutputStream() throws IOException {
        super();
    }

    public void onWrite() throws IOException {
    }

    public void doClose() {
    }

    public void doFlush() {
    }

}
