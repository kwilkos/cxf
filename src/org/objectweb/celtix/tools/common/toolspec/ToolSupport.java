package org.objectweb.celtix.tools.common.toolspec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.pump.Pumper;

public class ToolSupport implements Tool {

    private static final Logger LOG = LogUtils.getL7dLogger(ToolSupport.class);
    protected Pumper pumper = Pumper.createPumper(4096);
    private ToolContext ctx;

    public void init() throws ToolException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Initializing " + this);
        }
    }

    public void setContext(ToolContext c) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Setting context to " + c);
        }
        this.ctx = c;
    }

    public ToolContext getContext() {
        return ctx;
    }

    public void performFunction() throws ToolException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Performing function");
        }
    }

    public void destroy() throws ToolException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Destroying " + this);
        }
    }

    protected void pump(InputStream in, OutputStream out) throws ToolException {
        try {
            pumper.pump(in, out);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "FAIL_TO_SERIALIZE_MSG", ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                throw new ToolException("Failed to close output stream", ex);
            }
        }
    }

    protected void pump(File in, OutputStream out) throws ToolException {
        try {
            pumper.pumpFromFile(in, out);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "FAIL_TO_SERIALIZE_MSG", ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                throw new ToolException("Failed to close output stream", ex);
            }
        }
    }

}
