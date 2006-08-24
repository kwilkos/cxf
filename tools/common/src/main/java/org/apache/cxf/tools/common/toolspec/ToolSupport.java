package org.apache.cxf.tools.common.toolspec;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
public class ToolSupport implements Tool {

    private static final Logger LOG = LogUtils.getL7dLogger(ToolSupport.class);
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
}
