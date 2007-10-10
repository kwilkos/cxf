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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;

public class CachedOutputStream extends OutputStream {

    protected OutputStream currentStream;

    private long threshold = 64 * 1024;

    private int totalLength;

    private boolean inmem;

    private File tempFile;

    private File outputDir;

    private List<CachedOutputStreamCallback> callbacks;

    public CachedOutputStream(PipedInputStream stream) throws IOException {
        currentStream = new PipedOutputStream(stream);
        inmem = true;
    }

    public CachedOutputStream() {
        currentStream = new ByteArrayOutputStream(2048);
        inmem = true;
    }

    public CachedOutputStream(long threshold) {
        this.threshold = threshold; 
        currentStream = new ByteArrayOutputStream(2048);
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
    protected void doFlush() throws IOException {
        
    }

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
    protected void doClose() throws IOException {
        
    }
    
    /**
     * Perform any actions required after stream closure (close the other related stream etc.)
     */
    protected void postClose() throws IOException {
        
    }

    public void close() throws IOException {
        currentStream.flush();
        if (null != callbacks) {
            for (CachedOutputStreamCallback cb : callbacks) {
                cb.onClose(this);
            }
        }
        
        doClose();
        currentStream.close();
        dispose();
        postClose();
        
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
        if (currentStream instanceof CachedOutputStream) {
            CachedOutputStream ac = (CachedOutputStream) currentStream;
            InputStream in = ac.getInputStream();
            IOUtils.copyAndCloseInput(in, out);
        } else {
            if (inmem) {
                if (currentStream instanceof ByteArrayOutputStream) {
                    ByteArrayOutputStream byteOut = (ByteArrayOutputStream) currentStream;
                    if (copyOldContent && byteOut.size() > 0) {
                        byteOut.writeTo(out);
                    }
                } else if (currentStream instanceof PipedOutputStream) {
                    PipedOutputStream pipeOut = (PipedOutputStream) currentStream;
                    IOUtils.copyAndCloseInput(new PipedInputStream(pipeOut), out);
                } else {
                    throw new IOException("Unknown format of currentStream");
                }
            } else {
                // read the file
                currentStream.close();
                FileInputStream fin = new FileInputStream(tempFile);
                if (copyOldContent) {
                    IOUtils.copyAndCloseInput(fin, out);
                }
            }
        }
        currentStream = out;
    }

    public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        IOUtils.copyAndCloseInput(in, out, bufferSize);
    }

    
    public byte[] getBytes() throws IOException {
        flush();
        if (inmem) {
            if (currentStream instanceof ByteArrayOutputStream) {
                return ((ByteArrayOutputStream)currentStream).toByteArray();
            } else {
                throw new IOException("Unknown format of currentStream");
            }
        } else {
            // read the file
            FileInputStream fin = new FileInputStream(tempFile);
            return IOUtils.readBytesFromStream(fin);
        }
    }
    
    public void writeCacheTo(OutputStream out) throws IOException {
        flush();
        if (inmem) {
            if (currentStream instanceof ByteArrayOutputStream) {
                ((ByteArrayOutputStream)currentStream).writeTo(out);
            } else {
                throw new IOException("Unknown format of currentStream");
            }
        } else {
            // read the file
            FileInputStream fin = new FileInputStream(tempFile);
            IOUtils.copyAndCloseInput(fin, out);
        }
    }
    public void writeCacheTo(StringBuilder out) throws IOException {
        flush();
        if (inmem) {
            if (currentStream instanceof ByteArrayOutputStream) {
                out.append(((ByteArrayOutputStream)currentStream).toString());
            } else {
                throw new IOException("Unknown format of currentStream");
            }
        } else {
            // read the file
            FileInputStream fin = new FileInputStream(tempFile);
            byte bytes[] = new byte[1024];
            int x = fin.read(bytes);
            while (x != -1) {
                out.append(new String(bytes, 0, x));
                x = fin.read(bytes);
            }
            fin.close();
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
        StringBuilder builder = new StringBuilder().append("[")
            .append(super.toString())
            .append(" Content: ");
        
        if (inmem) {
            builder.append(currentStream.toString());
        } else {
            try {
                Reader fin = new FileReader(tempFile);
                char buf[] = new char[1024];
                int x = fin.read(buf);
                while (x > -1) {
                    builder.append(buf, 0, x);
                    x = fin.read(buf);
                }
                fin.close();
            } catch (IOException e) {
                //ignore
            }
        }
        return builder.append("]").toString();
    }

    protected void onWrite() throws IOException {
        
    }

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
        return tempFile != null && tempFile.exists() ? tempFile : null;
    }

    public InputStream getInputStream() throws IOException {
        flush();
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
                return new FileInputStream(tempFile) {
                    public void close() throws IOException {
                        super.close();
                        if (tempFile != null) {
                            tempFile.delete();
                            //tempFile = null;
                        }
                        currentStream = new ByteArrayOutputStream();
                        inmem = true;
                    }
                };
            } catch (FileNotFoundException e) {
                throw new IOException("Cached file was deleted, " + e.toString());
            }
        }
    }

    public void dispose() {
        if (!inmem && tempFile != null) {
            tempFile.delete();
            //tempFile = null;
        }
    }

    public void setOutputDir(File outputDir) throws IOException {
        this.outputDir = outputDir;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
}
