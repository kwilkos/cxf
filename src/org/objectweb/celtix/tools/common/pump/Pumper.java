package org.objectweb.celtix.tools.common.pump;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.*;


public class Pumper {
    private static final int MAX_FILES = 32;
    private static List<Object> filesToClose = new LinkedList<Object>();
    private static boolean threadStarted;

    private static Pumper defaultPumper;
    private static Class pumperClass;
    static {
        if (System.getProperty("os.name").toLowerCase().startsWith("irix")) {
            pumperClass = Pumper.class;
        } else {
            try {
                pumperClass = Class.forName("org.objectweb.celtix.tools.common.pump.NIOPumper");
            } catch (Throwable ex) {
                // probably couldn't find the nio stuff, just use a normal pumper
                pumperClass = Pumper.class;
            }
        }
    }

    protected final byte[] buf;
    protected PumperListener listener;

    public Pumper() {
        buf = new byte[4096];
    }

    public Pumper(int size) {
        buf = new byte[size];
    }

    private static void closeStreamThread() {
        while (true) {
            OutputStream out = null;
            synchronized (filesToClose) {
                if (filesToClose.isEmpty()) {
                    try {
                        filesToClose.wait();
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                }
                if (!filesToClose.isEmpty()) {
                    out = (OutputStream)filesToClose.remove(0);
                }
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private static synchronized void startStreamThread() {
        if (threadStarted) {
            return;
        }
        Thread th = new Thread() {
                public void run() {
                    closeStreamThread();
                }
            };
        th.setDaemon(true);
        th.start();
        threadStarted = true;
    }

    protected void closeStream(OutputStream out) throws IOException {
        startStreamThread();
        synchronized (filesToClose) {
            if (filesToClose.size() < MAX_FILES) {
                filesToClose.add(out);
                filesToClose.notifyAll();
                out = null;
            }
        }
        if (out != null) {
            out.close();
        }
    }

    public void closeAllStreams() {
        synchronized (filesToClose) {
            while (!filesToClose.isEmpty()) {
                OutputStream out = (OutputStream)filesToClose.remove(0);
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    public static synchronized Pumper getDefaultPumper() {
        if (defaultPumper == null) {
            defaultPumper = createPumper();
        }
        return defaultPumper;
    }

    public static Pumper createPumper() {
        try {
            return (Pumper)pumperClass.newInstance();
        } catch (Throwable ex) {
            // ignore, use default pumper
        }
        return new Pumper();
    }

    public static Pumper createPumper(int bufferSize) {
        try {
            Constructor cons = pumperClass.getConstructor(
                                                          new Class[] {
                                                              Integer.TYPE
                                                          });

            return (Pumper)cons.newInstance(
                                            new Object[] {
                                                new Integer(bufferSize)
                                            });
        } catch (Throwable ex) {
            // ignore, use default pumper
        }
        return new Pumper(bufferSize);
    }


    public void setPumperListener(PumperListener l) {
        listener = l;
    }

    public PumperListener getPumperListener() {
        return listener;
    }

    public void pumpToFile(InputStream ins, File out) throws IOException {
        pumpToFile(ins, out, -1);
    }

    public void pumpToFile(InputStream ins, File out, int size) throws IOException {
        FileOutputStream fout = new FileOutputStream(out);

        pump(ins, fout);
        closeStream(fout);
    }

    public void pumpFromFile(File ins, OutputStream out) throws IOException {
        FileInputStream fin = new FileInputStream(ins);

        pump(fin, out);
        fin.close();
    }

    public void pump(InputStream ins, OutputStream out) throws IOException {
        if (ins instanceof PipedInputStream) {
            // let the pipe itself redirect to the right place, no block copies
            ((PipedInputStream)ins).setRedirectStream(out);
        } else {
            while (true) {
                int r = ins.read(buf, 0, buf.length);

                if (r == -1) {
                    break;
                }
                out.write(buf, 0, r);
                if (listener != null) {
                    listener.blockCopied(r);
                }
            }
        }
    }
}

