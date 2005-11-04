package org.objectweb.celtix.tools.common.pump;

import java.io.IOException;
import java.io.OutputStream;


public class PipedOutputStream extends OutputStream {

    final PipedInputStream insStream;
    final byte singleByte[] = new byte[1];

    public PipedOutputStream(PipedInputStream ins) {
        this.insStream = ins;
    }

    public void write(int b) throws java.io.IOException {
        singleByte[0] = (byte)b;
        write(singleByte, 0, 1);
    }

    public void flush() throws IOException {
        synchronized (insStream) {
            insStream.notifyAll();
        }
    }

    public void close() throws IOException {
        insStream.outputClosed();
    }

    public void write(byte b[], int off, int len) throws IOException {
        insStream.receive(b, off, len);
    }

}

