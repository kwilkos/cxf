package org.objectweb.celtix.common.commands;

public class ForkedCommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private ForkedCommand cmd;

    public ForkedCommandException(ForkedCommand proc, Throwable t) {
        this(proc, t.getMessage());
    }

    public ForkedCommandException(ForkedCommand proc, String msg) {
        super(msg);
        this.cmd = proc;
    }

    public ForkedCommand getCommand() {
        return this.cmd;
    }

}
