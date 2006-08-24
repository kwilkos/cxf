package org.apache.cxf.tools.common.toolspec;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
public final class ToolRunner {
    private static final Logger LOG = LogUtils.getL7dLogger(ToolRunner.class);
    private ToolRunner() {
        // utility class - never constructed
    }

    public static void runTool(Class clz, InputStream toolspecStream,
                               boolean validate, String[] args) throws Exception {
        runTool(clz, toolspecStream, validate, args, true);
    }

    public static void runTool(Class clz,
                               InputStream toolspecStream,
                               boolean validate,
                               String[] args,
                               boolean exitOnFinish) throws Exception {

        if (ToolContainer.class.isAssignableFrom(clz)) {

            ToolContainer container = null;

            try {
                Constructor cons = clz.getConstructor(
                                                      new Class[] {
                                                          ToolSpec.class
                                                      });
                container = (ToolContainer)cons.newInstance(
                                                            new Object[] {
                                                                new ToolSpec(toolspecStream, validate)
                                                            });
            } catch (Exception ex) {
                Message message = new Message("CLZ_CANNOT_BE_CONSTRUCTED", LOG, clz.getName());
                LOG.log(Level.SEVERE, message.toString());
                throw new ToolException(message, ex);
            }

            try {
                container.setCommandLine(args);
                container.init();
                container.execute(exitOnFinish);
            } catch (Exception ex) {
                throw ex;
            }
        } else {
            Message message = new Message("CLZ_SHOULD_IMPLEMENT_INTERFACE", LOG, clz.getName());
            LOG.log(Level.SEVERE, message.toString());
            throw new ToolException(message);
        }

    }

}
