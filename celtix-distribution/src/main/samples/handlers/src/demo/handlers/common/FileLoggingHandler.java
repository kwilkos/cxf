/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package demo.handlers.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/*
 * This simple SOAPHandler will output the contents of incoming
 * and outgoing messages into a file.
 */
public class FileLoggingHandler extends LoggingHandler {

    public FileLoggingHandler() {
        try {
            setLogStream(new PrintStream(new FileOutputStream("demo.log")));
        } catch (IOException ex) {
            System.err.println("Could not open log file demo.log");
        }
    }

    public void init(Map c) {
        System.out.println("FileLoggingHandler : init() Called....");
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("FileLoggingHandler : handleMessage Called....");
        logToSystemOut(smc);
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("FileLoggingHandler : handleFault Called....");
        logToSystemOut(smc);
        return true;
    }
}
