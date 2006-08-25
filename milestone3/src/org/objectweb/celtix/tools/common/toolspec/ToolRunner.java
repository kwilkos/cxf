package org.objectweb.celtix.tools.common.toolspec;

import java.io.*;
import java.lang.reflect.Constructor;

public final class ToolRunner {
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
                throw new ToolException(clz.getName() + " could not be constructed", ex);
            }

            try {
                container.setCommandLine(args);
                container.init();
                container.execute(exitOnFinish);
            } catch (Exception ex) {
                throw ex;
            }
        } else {
            throw new ToolException(clz.getName() + " should implement the ToolContainer interface");
        }

    }

}
