package org.objectweb.celtix.messaging;

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


public abstract class AbstractCachedOutputStream extends OutputStream {

    private OutputStream currentStream;
    private long threshold = 8 * 1024;
    private int totalLength;
    private boolean inmem;
    private File tempFile;
    private File outputDir;
    

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
        flush();
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
    protected OutputStream getOut() {
        return currentStream;
    }

    public int hashCode() {
        return currentStream.hashCode();
    }

    public String toString() {
        return currentStream.toString();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.totalLength += len;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b, off, len);
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

    public void write(byte[] b) throws IOException {
        this.totalLength += b.length;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b);
    }

    public void write(int b) throws IOException {
        this.totalLength++;
        if (inmem && totalLength > threshold) {
            createFileOutputStream();
        }
        currentStream.write(b);
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
