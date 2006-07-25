package org.objectweb.celtix.bindings.attachments;

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

public class CachedOutputStream extends OutputStream {
    private OutputStream currentStream;
    private long threshold;
    private int totalLength;
    private boolean inmem;
    private File tempFile;
    private File outputDir;

    public CachedOutputStream(long thresholdParam, File outputDirParam) throws IOException {
        this.threshold = thresholdParam;
        this.outputDir = outputDirParam;

        if (threshold <= 0) {
            createFileOutputStream();
        } else {
            currentStream = new ByteArrayOutputStream();
            inmem = true;
        }
    }

    public void close() throws IOException {
        currentStream.close();
    }

    public boolean equals(Object obj) {
        return currentStream.equals(obj);
    }

    public void flush() throws IOException {
        currentStream.flush();
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
            switchToFile();
        }
        currentStream.write(b, off, len);
    }

    private void switchToFile() throws IOException {
        byte[] bytes = ((ByteArrayOutputStream)currentStream).toByteArray();

        createFileOutputStream();

        currentStream.write(bytes);
        inmem = false;
    }

    private void createFileOutputStream() throws IOException {
        if (outputDir == null) {
            tempFile = File.createTempFile("att", "tmp");
        } else {
            tempFile = File.createTempFile("att", "tmp", outputDir);
        }
        currentStream = new BufferedOutputStream(new FileOutputStream(tempFile));
    }

    public void write(byte[] b) throws IOException {
        this.totalLength += b.length;
        if (inmem && totalLength > threshold) {
            switchToFile();
        }
        currentStream.write(b);
    }

    public void write(int b) throws IOException {
        this.totalLength++;
        if (inmem && totalLength > threshold) {
            switchToFile();
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
}
