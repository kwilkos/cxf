package org.objectweb.celtix.bindings.soap2.attachments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class ByteDataSource implements DataSource {
    private String contentType;
    private String name;
    private byte[] data;
    private int offset;
    private int length;

    public ByteDataSource(byte[] dataParam) {
        this(dataParam, 0, dataParam.length);
    }

    public ByteDataSource(byte[] dataParam, int offsetParam, int lengthParam) {
        this.data = dataParam;
        this.offset = offsetParam;
        this.length = lengthParam;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] dataParam) {
        this.data = dataParam;
    }

    public void setContentType(String contentTypeParam) {
        this.contentType = contentTypeParam;
    }

    public void setName(String nameParam) {
        this.name = nameParam;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data, offset, length);
    }

    public String getName() {
        return name;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

}
