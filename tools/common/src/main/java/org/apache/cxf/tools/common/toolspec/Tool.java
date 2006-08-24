package org.apache.cxf.tools.common.toolspec;
import org.apache.cxf.tools.common.ToolException;
public interface Tool {

    String TOOL_SPEC_PUBLIC_ID = "http://www.xsume.com/Xutil/ToolSpecification";

    void init() throws ToolException;

    void setContext(ToolContext ctx);

    /**
     * A tool has to be prepared to perform it's duty any number of times.
     */
    void performFunction() throws ToolException;

    void destroy() throws ToolException;

}
