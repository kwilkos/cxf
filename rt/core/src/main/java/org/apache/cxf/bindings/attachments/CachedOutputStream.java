package org.apache.cxf.bindings.attachments;

import java.io.IOException;

import org.apache.cxf.messaging.AbstractCachedOutputStream;

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
