package org.objectweb.celtix.tools.common.pump;

import java.io.*;
import junit.framework.TestCase;

public class PipedTest extends TestCase {
    private void checkBytes(byte in[], byte out[]) {
        assertEquals("In and out byte[] lengths do not match", in.length, out.length);
        for (int x = 0; x < in.length; x++) {
            assertEquals("Bytes at pos " + x + " do not match", in[x], out[x]);
        }
    }

    public void testPipeFirstWrite() throws Exception {
        byte byts[] = new byte[256 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        for (int x = 0; x < byts.length; x += 1024) {
            out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
        }

        byte bytes2[] = new byte[ins.available()];

        ins.read(bytes2);
        checkBytes(byts, bytes2);
    }

    public void testPipeInterSperse() throws Exception {
        byte byts[] = new byte[256 * 1024];
        byte byts2[] = new byte[256 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        int pos = 0;

        for (int x = 0; x < byts.length; x += 1024) {
            out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
            pos += ins.read(byts2, pos, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
        }

        assertEquals("Did not read all the bytes", byts.length, pos);
        checkBytes(byts, byts2);
    }

    public void testPipeSemiInterSperse() throws Exception {
        byte byts[] = new byte[256 * 1024];
        byte byts2[] = new byte[256 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        int pos = 0;

        for (int x = 0; x < byts.length; x += 1024) {
            out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
            pos += ins.read(byts2, pos, ((byts.length - x) > 1024 ? 1024 : (byts.length - x)) / 2);
        }

        assertEquals("Should have read half", byts.length / 2, ins.available());
        pos += ins.read(byts2, pos, ins.available());
        assertEquals("Did not read all the bytes", byts.length, pos);
        checkBytes(byts, byts2);
    }

    public void testThreaded() throws Exception {
        byte byts[] = new byte[64 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        TestRunnable tr = new TestRunnable(ins);

        new Thread(tr).start();

        for (int x = 0; x < byts.length; x += 1024) {
            out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
            Thread.currentThread().sleep(100);
        }
        out.close();

        while (!tr.finished) {
            Thread.currentThread().sleep(100);
        }
        if (tr.ex != null) {
            throw new Exception(tr.ex);
        }

        checkBytes(byts, tr.bout.toByteArray());
    }

    public void testRedirect() throws Exception {
        byte byts[] = new byte[64 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        TestRunnable tr = new TestRunnable(ins, true, false, false);

        new Thread(tr).start();

        for (int x = 0; x < byts.length; x += 1024) {
            out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
            Thread.currentThread().sleep(10);
        }
        out.close();

        while (!tr.finished) {
            Thread.currentThread().sleep(100);
        }
        if (tr.ex != null) {
            throw new Exception(tr.ex);
        }

        checkBytes(byts, tr.bout.toByteArray());
    }

    public void testDeadReader() throws Exception {
        byte byts[] = new byte[64 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        TestRunnable tr = new TestRunnable(ins, false, true, false);

        new Thread(tr).start();

        try {
            for (int x = 0; x < byts.length; x += 1024) {
                out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
                Thread.currentThread().sleep(10);
            }
            fail("Should have gotten a pipe closed exception");
        } catch (IOException ex) {
            //ignore, expected
        }
    }

    public void testReaderClose() throws Exception {
        byte byts[] = new byte[64 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        PipedInputStream ins = new org.objectweb.celtix.tools.common.pump.PipedInputStream();
        PipedOutputStream out = new org.objectweb.celtix.tools.common.pump.PipedOutputStream(ins);

        TestRunnable tr = new TestRunnable(ins, false, false, true);

        new Thread(tr).start();

        try {
            for (int x = 0; x < byts.length; x += 1024) {
                out.write(byts, x, (byts.length - x) > 1024 ? 1024 : (byts.length - x));
                Thread.currentThread().sleep(10);
            }
            fail("Should have gotten a pipe closed exception");
        } catch (IOException ex) {
            //ignore, expected
        }
    }

    class TestRunnable implements Runnable {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
        boolean finished;
        boolean redirect;
        boolean death;
        boolean close;
        PipedInputStream ins;
        Throwable ex;

        public TestRunnable(PipedInputStream in) {
            ins = in;
        }

        public TestRunnable(PipedInputStream in, boolean r, boolean d, boolean c) {
            ins = in;
            redirect = r;
            death = d;
            close = c;
        }

        public void run() {
            try {
                byte buf[] = new byte[1024];
                int pos = 0;
                int r = ins.read(buf, 0, buf.length);
                while (r != -1) {
                    bout.write(buf, 0, r);
                    pos += r;
                    if (pos > 16000) {
                        if (redirect) {
                            ins.setRedirectStream(bout);
                            return;
                        } else if (death) {
                            return;
                        } else if (close) {
                            ins.close();
                            return;
                        }
                    }
                    r = ins.read(buf, 0, buf.length);
                }
            } catch (Throwable ex2) {
                ex2.printStackTrace();
                ex2.fillInStackTrace();
                ex = ex2;
            } finally {
                finished = true;
            }
        }
    }
}

