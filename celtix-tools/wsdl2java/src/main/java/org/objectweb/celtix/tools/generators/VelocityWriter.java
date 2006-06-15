package org.objectweb.celtix.tools.generators;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class VelocityWriter extends BufferedWriter {
    
    private final String newLine = System.getProperty("line.separator");

    public VelocityWriter(Writer out) {
        super(out);
    }

    public VelocityWriter(Writer out, int size) {
        super(out, size);
    }

    public void write(char[] chars) throws IOException {
        String str = new String(chars);
        if (str.indexOf("\r\n") >= 0 && newLine != null) {
            super.write(str.replaceAll("\r\n", newLine));
            return;
        } else if (str.indexOf("\n") >= 0 && newLine != null) {
            super.write(str.replaceAll("\n", newLine));
            return;
        } else {
            super.write(str);
        }
       
    }
   
    
    
    
    public void write(String str) throws IOException {
        if (str.indexOf("\r\n") >= 0  && newLine != null) {
            super.write(str.replaceAll("\r\n", newLine));
            return;
        } else if (str.indexOf("\n") >= 0  && newLine != null) {
            super.write(str.replaceAll("\r\n", newLine));
            return;
        } else {
            super.write(str);
        }
    }

}
