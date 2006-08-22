package org.objectweb.celtix.bindings.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class AttachmentDataSource implements DataSource {

    private final String ct;
    private final InputStream in;

    public AttachmentDataSource(String ctParam, CachedOutputStream cosParam) throws IOException {
        this.ct = ctParam;
        this.in = cosParam.getInputStream();
    }

    public AttachmentDataSource(String ctParam, InputStream inParam) {
        this.ct = ctParam;
        this.in = inParam;
    }

    public String getContentType() {
        return ct;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }


}
