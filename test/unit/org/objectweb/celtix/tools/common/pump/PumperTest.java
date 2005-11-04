package org.objectweb.celtix.tools.common.pump;

import java.io.*;
import junit.framework.TestCase;

public class PumperTest extends TestCase {
    private void checkBytes(byte in[], byte out[]) {
        assertEquals("In and out byte[] lengths do not match", in.length, out.length);
        for (int x = 0; x < in.length; x++) {
            assertEquals("Bytes at pos " + x + " do not match", in[x], out[x]);
        }
    }

    public void testNIOPumperInMemory() throws Exception {
        Pumper pumper = Pumper.createPumper();
        byte byts[] = new byte[1024 * 16];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);
        ByteArrayInputStream in = new ByteArrayInputStream(byts);

        pumper.pump(in, out);
        checkBytes(byts, out.toByteArray());
    }

    public void testPumperInMemory() throws Exception {
        Pumper pumper = new Pumper();
        byte byts[] = new byte[1024 * 16];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);
        ByteArrayInputStream in = new ByteArrayInputStream(byts);

        pumper.pump(in, out);
        checkBytes(byts, out.toByteArray());
    }

    public void testPumperToFromFile() throws Exception {
        Pumper pumper = new Pumper();
        byte byts[] = new byte[1024 * 16];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
        }
    }

    public void testNIOPumperToFromFile() throws Exception {
        Pumper pumper = Pumper.createPumper();
        byte byts[] = new byte[1024 * 16];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
        }
    }

    public void testPumperToFromHugeFile() throws Exception {
        Pumper pumper = new Pumper();
        byte byts[] = new byte[1024 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
        }
    }

    public void testNIOPumperToFromHugeFile() throws Exception {
        Pumper pumper = Pumper.createPumper();
        byte byts[] = new byte[1024 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
        }
    }

    public void testFileToFile() throws Exception {
        Pumper pumper = new Pumper();
        byte byts[] = new byte[1024 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");
        File file2 = File.createTempFile("temp", "tst");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);

            pumper.pumpToFile(new FileInputStream(file), file2);

            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file2, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
            file2.delete();
        }
    }

    public void testNIOFileToFile() throws Exception {
        Pumper pumper = Pumper.createPumper();
        byte byts[] = new byte[1024 * 1024];

        for (int x = 0; x < byts.length; x++) {
            byts[x] = (byte)(x & 0xff);
        }

        File file = File.createTempFile("temp", "tst");
        File file2 = File.createTempFile("temp", "tst");

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(byts);

            pumper.pumpToFile(in, file);

            pumper.pumpToFile(new FileInputStream(file), file2);

            ByteArrayOutputStream out = new ByteArrayOutputStream(byts.length);

            pumper.pumpFromFile(file2, out);
            checkBytes(byts, out.toByteArray());
        } finally {
            file.delete();
            file2.delete();
        }
    }

}

