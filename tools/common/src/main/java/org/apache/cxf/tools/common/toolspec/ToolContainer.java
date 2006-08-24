package org.apache.cxf.tools.common.toolspec;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;

public interface ToolContainer {

    void setCommandLine(String[] args) throws BadUsageException;
    void init() throws ToolException;
    void execute(boolean exitOnFinish) throws ToolException;

}
