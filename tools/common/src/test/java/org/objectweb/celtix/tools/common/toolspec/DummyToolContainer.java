package org.objectweb.celtix.tools.common.toolspec;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;
public class DummyToolContainer extends AbstractToolContainer {

    public DummyToolContainer(ToolSpec ts) throws BadUsageException {
        super(ts);
    }

    public void execute(boolean exitOnFinish) throws ToolException {

    }

}
