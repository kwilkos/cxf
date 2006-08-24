package org.apache.cxf.common.commands;

import java.io.*;

public class ResultBufferedCommand extends ForkedCommand {

    private ByteArrayOutputStream bosOut;
    private ByteArrayOutputStream bosError;

    public ResultBufferedCommand() {
        init();
    }

    public ResultBufferedCommand(String[] args) {
        super(args);
        init();
    }

    public InputStream getOutput() {
        return new ByteArrayInputStream(this.bosOut.toByteArray());
    }

    public BufferedReader getBufferedOutputReader() {
        return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(this.bosOut.toByteArray())));
    }

    public InputStream getError() {
        return new ByteArrayInputStream(bosError.toByteArray());
    }

    public BufferedReader getBufferedErrorReader() {
        return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(bosError.toByteArray())));
    }
    
    private void init() {
        bosOut = new ByteArrayOutputStream();
        bosError = new ByteArrayOutputStream();

        setOutputStream(new PrintStream(bosOut));
        setErrorStream(new PrintStream(bosError));
    }


}
