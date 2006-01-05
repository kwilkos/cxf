package org.objectweb.celtix.tools.common.toolspec;

import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;

public interface ToolContainer {

    void setCommandLine(String[] args) throws BadUsageException;
    void init() throws ToolException;
    void execute(boolean exitOnFinish) throws ToolException;

}
