package org.objectweb.celtix.tools.processors.wsdl2.compiler;

import java.io.File;
import java.io.IOException;

public class Compiler {
    public boolean internalCompile(String[] args) {

        Process p = null;
        try {

            for (int i = 0; i < args.length; i++) {
                if (!"/".equals(File.separator) && args[i].indexOf("package-info") == -1) {
                    args[i] = args[i].replace(File.separatorChar, '/');
                }              
            }

            p = Runtime.getRuntime().exec(args);
            if (p.getErrorStream() != null) {
                StreamPrinter errorStreamPrinter = new StreamPrinter(p.getErrorStream(), "error", System.err);
                errorStreamPrinter.run();
            }
            if (p.getInputStream() != null) {
                StreamPrinter infoStreamPrinter = new StreamPrinter(p.getInputStream(), "info", System.err);
                infoStreamPrinter.run();
            }

            if (p != null) {
                return p.waitFor() == 0 ? true : false;
            }
        } catch (SecurityException e) {
            // ignore
        } catch (InterruptedException e) {
            // ignore

        } catch (IOException e) {
            // ignore
        }

        return false;
    }
}
