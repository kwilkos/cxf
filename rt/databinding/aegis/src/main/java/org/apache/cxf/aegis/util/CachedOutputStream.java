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
package org.apache.cxf.aegis.util;

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

import org.apache.cxf.aegis.DatabindingException;

public class CachedOutputStream extends OutputStream {
    private OutputStream currentStream;
    private int threshold;
    private int totalLength = 0;
    private boolean inmem = false;
    private File tempFile = null;
    private File outputDir;

    public CachedOutputStream(int threshold, File outputDir) throws IOException {
        this.threshold = threshold;
        this.outputDir = outputDir;

        if (threshold <= 0) {
            createFileOutputStream();
        } else {
            currentStream = new ByteArrayOutputStream();
            inmem = true;
        }
    }

    @Override
    public void close() throws IOException {
        currentStream.close();
    }

    @Override
    public boolean equals(Object obj) {
        return currentStream.equals(obj);
    }

    @Override
    public void flush() throws IOException {
        currentStream.flush();
    }

    @Override
    public int hashCode() {
        return currentStream.hashCode();
    }

    @Override
    public String toString() {
        return currentStream.toString();
    }

    @Override
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

    @Override
    public void write(byte[] b) throws IOException {
        this.totalLength += b.length;
        if (inmem && totalLength > threshold) {
            switchToFile();
        }

        currentStream.write(b);
    }

    @Override
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

    public InputStream getInputStream() {
        if (inmem) {
            return new ByteArrayInputStream(((ByteArrayOutputStream)currentStream).toByteArray());
        } else {
            try {
                return new FileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new DatabindingException("Cached file was deleted!!!", e);
            }
        }
    }

    public void dispose() {
        if (!inmem) {
            tempFile.delete();
        }
    }
}
