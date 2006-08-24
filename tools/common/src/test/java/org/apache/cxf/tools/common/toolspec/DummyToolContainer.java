package org.apache.cxf.tools.common.toolspec;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;
public class DummyToolContainer extends AbstractToolContainer {

    public DummyToolContainer(ToolSpec ts) throws BadUsageException {
        super(ts);
    }

    public void execute(boolean exitOnFinish) throws ToolException {

    }

}
