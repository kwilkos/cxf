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

package org.apache.cxf.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.common.util.Base64Utility;

public abstract class AbstractCachedOutputStream extends OutputStream {

    protected OutputStream currentStream;

    private long threshold = 64 * 1024;

    private int totalLength;

    private boolean inmem;

    private File tempFile;

    private File outputDir;

    private List<CachedOutputStreamCallback> callbacks;

    public AbstractCachedOutputStream(PipedInputStream stream) throws IOException {
        currentStream = new PipedOutputStream(stream);
        inmem = true;
    }

    public AbstractCachedOutputStream() {
        currentStream = new ByteArrayOutputStream();
        inmem = true;
    }

    public void registerCallback(CachedOutputStreamCallback cb) {
        if (null == callbacks) {
            callbacks = new ArrayList<CachedOutputStreamCallback>();
        }
        callbacks.add(cb);
    }
    
    public void deregisterCallback(CachedOutputStreamCallback cb) {
        if (null != callbacks) {
            callbacks.remove(cb);
        }
    }

    public List<CachedOutputStreamCallback> getCallbacks() {
        return callbacks == null ? null : Collections.unmodifiableList(callbacks);
    }

    /**
     * Perform any actions required on stream flush (freeze headers, reset
     * output stream ... etc.)
     */
    protected abstract void doFlush() throws IOException;

    public void flush() throws IOException {
        currentStream.flush();
        if (null != callbacks) {
            for (CachedOutputStreamCallback cb : callbacks) {
                cb.onFlush(this);
            }
        }
        doFlush();
    }

    /**
     * Perform any actions required on stream closure (handle response etc.)
     */
    protected abstract void doClose() throws IOException;

    public void close() throws IOException {
        currentStream.flush();
        currentStream.close();
        dispose();
        if (null != callbacks) {
            for (CachedOutputStreamCallback cb : callbacks) {
                cb.onClose(this);
            }
        }
        doClose();
    }

    public boolean equals(Object obj) {
        return currentStream.equals(obj);
    }

    /**
     * Replace the original stream with the new one, optionally copying the content of the old one
     * into the new one.
     * When with Attachment, needs to replace the xml writer stream with the stream used by
     * AttachmentSerializer or copy the cached output stream to the "real"
     * output stream, i.e. onto the wire.
     * 
     * @param out the new output stream
     * @param copyOldContent flag indicating if the old content should be copied
     * @throws IOException
     */
    public void resetOut(OutputStream out, boolean copyOldContent) throws IOException {
        if (currentStream instanceof AbstractCachedOutputStream) {
            AbstractCachedOutputStream ac = (AbstractCachedOutputStream) currentStream;
            InputStream in = ac.getInputStream();
            copyStream(in, out, (int) threshold);
        } else {
            if (inmem) {
                if (currentStream instanceof ByteArrayOutputStream) {
                    ByteArrayOutputStream byteOut = (ByteArrayOutputStream) currentStream;
                    if (copyOldContent && byteOut.size() > 0) {
                        byteOut.writeTo(out);
                    }
                } else if (currentStream instanceof PipedOutputStream) {
                    PipedOutputStream pipeOut = (PipedOutputStream) currentStream;
                    copyStream(new PipedInputStream(pipeOut), out, (int) threshold);
                } else {
                    throw new IOException("Unknown format of currentStream");
                }
            } else {
                // read the file
                currentStream.close();
                FileInputStream fin = new FileInputStream(tempFile);
                if (copyOldContent) {
                    copyStream(fin, out, (int) threshold);
                }
            }
        }
        currentStream = out;
    }

    public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        try {
            int n = in.read(buffer);
            while (n > 0) {
                out.write(buffer, 0, n);
                n = in.read(buffer);
            }
        } finally {
            in.close();
        }
    }

    public static void copyStreamWithBase64Encoding(InputStream in, OutputStream out, int bufferSize)
        throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(out);
        byte[] buffer = new byte[bufferSize];
        try {
            int n = in.read(buffer, 0, bufferSize);
            while (n > 0) {
                Base64Utility.encode(buffer, 0, n, osw);
                n = in.read(buffer, 0, bufferSize);
            }
        } finally {
            in.close();
        }
    }

    /**
     * @return the underlying output stream
     */
    public OutputStream getOut() {
        return currentStream;
    }

    public int hashCode() {
        return currentStream.hashCode();
    }

    public String toString() {
        return new StringBuilder().append("[")
            .append(super.toString())
            .append(" Content: ")
            .append(currentStream.toString())
            .append("]").toString();
    }

    protected abstract void onWrite() throws IOException;

    public void write(byte[] b, int off, int len) throws IOException {
        onWrite();
        this.totalLength += len;
        if (inmem && totalLength > threshold && currentStream instanceof ByteArrayOutputStream) {
            createFileOutputStream();
        }
        currentStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        onWrite();
        this.totalLength += b.length;
        if (inmem && totalLength > threshold && currentStream instanceof ByteArrayOutputStream) {
            createFileOutputStream();
        }
        currentStream.write(b);
    }

    public void write(int b) throws IOException {
        onWrite();
        this.totalLength++;
        if (inmem && totalLength > threshold && currentStream instanceof ByteArrayOutputStream) {
            createFileOutputStream();
        }
        currentStream.write(b);
    }

    private void createFileOutputStream() throws IOException {
        byte[] bytes = ((ByteArrayOutputStream) currentStream).toByteArray();
        if (outputDir == null) {
            tempFile = File.createTempFile("att", "tmp");
        } else {
            tempFile = File.createTempFile("att", "tmp", outputDir);
        }
        tempFile.deleteOnExit();
        currentStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        currentStream.write(bytes);
        inmem = false;
    }

    public File getTempFile() {
        return tempFile;
    }

    public InputStream getInputStream() throws IOException {
        if (inmem) {
            if (currentStream instanceof ByteArrayOutputStream) {
                return new ByteArrayInputStream(((ByteArrayOutputStream) currentStream).toByteArray());
            } else if (currentStream instanceof PipedOutputStream) {
                return new PipedInputStream((PipedOutputStream) currentStream);
            } else {
                return null;
            }
        } else {
            try {
                return new FileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new IOException("Cached file was deleted, " + e.toString());
            }
        }
    }

    public void dispose() {
        if (!inmem) {
            tempFile.delete();
        }
    }

    public void setOutputDir(File outputDir) throws IOException {
        this.outputDir = outputDir;
        createFileOutputStream();
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

}
