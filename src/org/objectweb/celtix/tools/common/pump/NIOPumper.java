package org.objectweb.celtix.tools.common.pump;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class NIOPumper extends Pumper {
    private static final int MAX_NON_MAPPED = 128 * 1024;

    // files less than 16K should just be closed.  No benifit to
    // putting on the background thread
    private static final int MIN_NONTHREADED_CLOSE = 16 * 1024;

    private static final int MAX_FILES = 32;

    private static List<Object> filesToClose = new LinkedList<Object>();
    private static boolean threadStarted;

    ByteBuffer bbuf;
    public NIOPumper() {
        super();
        bbuf = ByteBuffer.wrap(buf);
    }

    public NIOPumper(int size) {
        super(size);
        bbuf = ByteBuffer.wrap(buf);
    }

    private static void closeChannelThread() {
        while (threadStarted) {
            Object out = null;
            synchronized (filesToClose) {
                if (filesToClose.isEmpty()) {
                    try {
                        filesToClose.wait();
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                }
                if (!filesToClose.isEmpty()) {
                    out = filesToClose.remove(0);
                }
            }
            try {
                if (out != null) {
                    if (out instanceof OutputStream) {
                        ((OutputStream)out).close();
                    } else if (out instanceof Channel) {
                        ((Channel)out).close();
                    } else if (out instanceof RandomAccessFile) {
                        ((RandomAccessFile)out).close();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private static synchronized void startChannelThread() {
        if (threadStarted) {
            return;
        }

        threadStarted = true;
        Thread th = new Thread() {
                public void run() {
                    closeChannelThread();
                }
            };
        th.setDaemon(true);
        th.start();



        th = new Thread() {
                public void run() {
                    threadStarted = false;
                    closeAll();
                    synchronized (filesToClose) {
                        filesToClose.notifyAll();
                    }
                }
            };
        Runtime.getRuntime().addShutdownHook(th);
    }

    protected void closeStream(OutputStream out, long size) throws IOException {
        if (size < MIN_NONTHREADED_CLOSE) {
            out.close();
            return;
        }

        startChannelThread();

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
    protected void closeChannel(Channel out, long size) throws IOException {
        if (size < MIN_NONTHREADED_CLOSE) {
            out.close();
            return;
        }

        startChannelThread();
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
    protected void closeFile(RandomAccessFile out, long size) throws IOException {
        if (size < MIN_NONTHREADED_CLOSE) {
            out.close();
            return;
        }

        startChannelThread();
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

    protected static void closeAll() {
        synchronized (filesToClose) {
            while (!filesToClose.isEmpty()) {
                Object out = filesToClose.remove(0);
                try {
                    if (out instanceof OutputStream) {
                        ((OutputStream)out).close();
                    } else if (out instanceof Channel) {
                        ((Channel)out).close();
                    } else if (out instanceof RandomAccessFile) {
                        ((RandomAccessFile)out).close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void closeAllStreams() {
        closeAll();
    }


    public long pumpFromChannel(ReadableByteChannel channel, OutputStream out) throws IOException {
        long total = 0;
        while (true) {
            bbuf.clear();
            int r = channel.read(bbuf);

            if (r == -1) {
                return total;
            }
            out.write(buf, 0, r);
            total += r;
            if (listener != null) {
                listener.blockCopied(r);
            }
        }
    }

    public long pumpFromFileChannel(FileChannel channel, OutputStream out) throws IOException {
        if (channel.size() > MAX_NON_MAPPED) {
            // big file, we'll mmap it
            ByteBuffer buffer = null;

            try {
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            } catch (IOException ex) {
                // could not map the file, use normal channel pumping
                return pumpFromChannel(channel, out);
            }
            long total = 0;
            int r = buffer.remaining();
            while (r > 0) {
                if (r > buf.length) {
                    r = buf.length;
                }
                buffer.get(buf, 0, r);
                out.write(buf, 0, r);
                total += r;
                if (listener != null) {
                    listener.blockCopied(r);
                }
                r = buffer.remaining();
            }
            return total;
        }
        return pumpFromChannel(channel, out);
    }

    public long pumpFileChannelToFileChannel(FileChannel in, FileChannel out) throws IOException {
        return out.transferFrom(in, in.position(), in.size() - in.position());
    }

    public void pumpToFile(InputStream ins, File out) throws IOException {
        pumpToFile(ins, out, -1);
    }

    public void pumpToFile(InputStream ins, File out, int size) throws IOException {
        if (!out.exists()) {
            out.createNewFile();
        }
        // use random access file so we can mmap it
        RandomAccessFile fout = new RandomAccessFile(out, "rw");
        FileChannel channel = fout.getChannel();
        long total = 0;

        try {
            if (ins instanceof FileInputStream) {
                FileChannel inschannel = ((FileInputStream)ins).getChannel();
                try {
                    total = pumpFileChannelToFileChannel(inschannel, channel);
                } finally {
                    closeChannel(inschannel, total);
                }
            } else {
                MappedByteBuffer mbb = null;

                if (size > MAX_NON_MAPPED) {
                    try {
                        mbb = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
                    } catch (IOException ex) {
                        // couldn't mmap the area, just do normal blocks
                        mbb = null;
                    }
                }

                while (true) {
                    int max = buf.length;

                    if (size != -1 && max > size) {
                        max = size;
                    }
                    int cnt = ins.read(buf, 0, max);

                    if (cnt == -1) {
                        return;
                    }

                    if (mbb == null) {
                        ByteBuffer bb = bbuf;

                        if (cnt != buf.length) {
                            bb = ByteBuffer.wrap(buf, 0, cnt);
                        }
                        bb.position(0);
                        while (bb.hasRemaining()) {
                            channel.write(bb);
                        }
                    } else {
                        mbb.put(buf, 0, cnt);
                    }
                    total += cnt;

                    if (listener != null) {
                        listener.blockCopied(cnt);
                    }
                    if (size != -1) {
                        size -= cnt;
                        if (size == 0) {
                            return;
                        }
                    }
                }
            }
        } finally {
            closeChannel(channel, total);
            closeFile(fout, total);
        }
    }

    public void pumpFromFile(File ins, OutputStream out) throws IOException {
        FileInputStream fin = new FileInputStream(ins);
        FileChannel channel = fin.getChannel();

        long total = 0;
        try {
            total = pumpFromFileChannel(channel, out);
        } finally {
            closeChannel(channel, total);
            fin.close();
        }
    }

    public void pump(InputStream ins, OutputStream out) throws IOException {
        if (ins instanceof FileInputStream) {
            FileInputStream fin = (FileInputStream)ins;
            FileChannel channel = fin.getChannel();

            long total = 0;
            try {
                total = pumpFromFileChannel(channel, out);
            } finally {
                closeChannel(channel, total);
            }
        } else {
            // System.out.println("generic pump "+ins.getClass().getName()+" -> "+out.getClass().getName());
            super.pump(ins, out);
        }
    }

}
