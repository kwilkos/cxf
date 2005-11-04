package org.objectweb.celtix.tools.common.pump;

import java.io.*;

public class PipedInputStream extends InputStream {
    byte singleByte[] = new byte[1];

    Block first;
    Block last;

    int maxBlock = 4096;
    int multiplier = 1;
    int blockCount;

    boolean inputClosed;
    boolean outputClosed;

    Thread inputThread;
    Thread outputThread;

    OutputStream redirect;

    public PipedInputStream() {
    }

    public int read() throws java.io.IOException {
        read(singleByte, 0, 1);
        return singleByte[0];
    }

    public synchronized int available() throws IOException {
        int av = 0;
        Block blck = first;

        while (blck != null) {
            av += blck.max - blck.idx;
            blck = blck.next;
        }
        return av;
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        if (len > maxBlock) {
            maxBlock = len;
        }

        inputThread = Thread.currentThread();

        int count = 0;

        while (first == null) {
            count++;
            if (count == 20) {
                return 0;
            }
            if (outputClosed) {
                return -1;
            } else if (outputThread != null && !outputThread.isAlive()) {
                throw new IOException("Write end dead");
            } else {
                // wait for some data
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    // ignorable
                }
            }
        }

        int total = 0;

        while (first != null) {
            int l = len - total;

            if (l > (first.max - first.idx)) {
                l = first.max - first.idx;
            }
            System.arraycopy(first.bytes, first.idx, b, off, l);
            first.idx += l;
            total += l;
            off += l;
            if (total == len) {
                return total;
            }

            if (first.idx == first.max) {
                first = first.next;
                if (first == null) {
                    last = null;
                }
                if ((blockCount % 4) == 0) {
                    multiplier >>= 2;
                }
                blockCount--;
            }
        }

        return total;
    }

    public synchronized void close() throws IOException {
        inputClosed = true;
    }

    public synchronized void setRedirectStream(OutputStream out) throws IOException {
        inputThread = null;
        redirect = out;
        // redirect all the buffers
        while (first != null) {
            out.write(first.bytes, first.idx, first.max - first.idx);
            first = first.next;
        }
        last = null;
        while (!outputClosed) {
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                // ignorable
            }
        }
    }

    synchronized void outputClosed() throws IOException {
        outputClosed = true;
        notifyAll();
    }

    synchronized void receive(byte b[], int off, int len) throws IOException {
        if (inputClosed) {
            throw new IOException("Pipe closed");
        }

        if (inputThread != null && !inputThread.isAlive()) {
            throw new IOException("Read end dead");
        }
        outputThread = Thread.currentThread();

        if (redirect != null) {
            redirect.write(b, off, len);
            notifyAll();
            return;
        }

        if (len > maxBlock) {
            maxBlock = len;
        }

        if (last != null && len <= (last.bytes.length - last.max)) {
            System.arraycopy(b, off, last.bytes, last.max, len);
            last.max += len;
            notifyAll();
            return;
        }
        blockCount++;

        if ((blockCount % 4) == 0) {
            // severely overpowering the receiver, start increasing the block sizes
            multiplier <<= 2;
        }

        Block blck = new Block(b, off, len);

        if (first == null) {
            first = blck;
            last = blck;
        } else {
            last.next = blck;
            last = blck;
        }
        notifyAll();
    }

    private class Block {
        byte bytes[];
        Block next;
        int idx;
        int max;

        Block(byte b[], int off, int len) {
            bytes = new byte[maxBlock * multiplier];
            System.arraycopy(b, off, bytes, 0, len);
            idx = 0;
            max = len;
            next = null;
        }
    }

}

