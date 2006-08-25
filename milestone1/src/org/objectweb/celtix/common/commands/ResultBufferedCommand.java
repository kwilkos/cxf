package org.objectweb.celtix.common.commands;

import java.io.*;

public class ResultBufferedCommand extends ForkedCommand {

    private ByteArrayOutputStream bosOut;
    private ByteArrayOutputStream bosError;

    public ResultBufferedCommand() {

    }

    public ResultBufferedCommand(String[] args) {
        super(args);
        this.bosOut = new ByteArrayOutputStream();
        this.bosError = new ByteArrayOutputStream();

        setOutputStream(new PrintStream(this.bosOut));
        setErrorStream(new PrintStream(this.bosError));
    }

    public InputStream getOutput() {
        return new ByteArrayInputStream(this.bosOut.toByteArray());
    }

    public BufferedReader getBufferedOutputReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.bosOut.toByteArray())));
    }

    public InputStream getError() {
        return new ByteArrayInputStream(this.bosError.toByteArray());
    }

    public BufferedReader getBufferedErrorReader() {
        return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(this.bosError.toByteArray())));
    }


}
