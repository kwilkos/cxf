package org.objectweb.celtix.tools.wsdl2java.processor.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.objectweb.celtix.tools.common.ToolException;

public class StreamPrinter extends Thread {
    InputStream is;
    String msg;
    OutputStream os;

    StreamPrinter(InputStream stream, String type) {
        this(stream, type, null);
    }

    StreamPrinter(InputStream stream, String type, OutputStream redirect) {
        is = stream;
        msg = type;
        os = redirect;
    }

    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null) {
                pw = new PrintWriter(os);
            }
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                if (pw != null) {
                    pw.println(msg + ">" + line);
                }
                line = br.readLine();
            }
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException ioe) {
            throw new ToolException(ioe);
        }
    }
}
