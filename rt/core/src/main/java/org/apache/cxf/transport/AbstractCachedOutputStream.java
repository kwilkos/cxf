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

package org.apache.cxf.transport;

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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


public abstract class AbstractCachedOutputStream extends OutputStream {

    protected OutputStream currentStream;    
    private long threshold = 8 * 1024;
    private int totalLength;
    private boolean inmem;
    private File tempFile;
    private File outputDir;
    

    public AbstractCachedOutputStream(PipedInputStream stream) throws IOException {
        currentStream = new PipedOutputStream(stream);        
        inmem = true;       
    }

    public AbstractCachedOutputStream() {
        currentStream = new ByteArrayOutputStream();        
        inmem = true;       
    }

    /**
     * Perform any actions required on stream flush (freeze headers, reset
     * output stream ... etc.)
     */
    protected abstract void doFlush() throws IOException;

    public void flush() throws IOException {
        currentStream.flush();
        doFlush();
    }

    /**
     * Perform any actions required on stream closure (handle response etc.)
     */
    protected abstract void doClose() throws IOException;

    public void close() throws IOException {        
        currentStream.close();
        doClose();
    }

    public boolean equals(Object obj) {
        return currentStream.equals(obj);
    }

    /**
     * Replace the original stream with the new one, when with Attachment, needs
     * to replace the xml writer stream with the stream used by
     * AttachmentSerializer Or Copy the cached output stream to the "real"
     * output stream, i.e. onto the wire.
     * 
     * @param realOS the real output stream
     * @throws IOException
     */
    public void resetOut(OutputStream out, boolean copyOldContent) throws IOException {
        ByteArrayOutputStream bout = (ByteArrayOutputStream)currentStream;
        if (copyOldContent && bout.size() > 0) {
            bout.writeTo(out);
        }
        currentStream = out;
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
        return currentStream.toString();
    }    
    
    protected abstract void onWrite() throws IOException;
        
    
    public void write(byte[] b, int off, int len) throws IOException {
        onWrite();
        this.totalLength += len;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        onWrite();
        this.totalLength += b.length;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b);
    }

    public void write(int b) throws IOException {
        onWrite();
        this.totalLength++;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b);
    }

    
    private void createFileOutputStream() throws IOException {
        byte[] bytes = ((ByteArrayOutputStream)currentStream).toByteArray();
        if (outputDir == null) {
            tempFile = File.createTempFile("att", "tmp");
        } else {
            tempFile = File.createTempFile("att", "tmp", outputDir);
        }
        currentStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        currentStream.write(bytes);
        inmem = false;
    }

    public File getTempFile() {
        return tempFile;
    }

    public InputStream getInputStream() throws IOException {
        if (inmem) {
            return new ByteArrayInputStream(((ByteArrayOutputStream)currentStream).toByteArray());
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
