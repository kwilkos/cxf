package org.apache.cxf.tools.common.toolspec.parser;


import java.util.*;


public class BadUsageException extends Exception {
    public static final long serialVersionUID = 1L;
    private final String usage;
    private final ErrorVisitor errors;

    public BadUsageException() {
        this("(unknown)", null);
    }

    public BadUsageException(ErrorVisitor err) {
        this("(unknown)", err);
    }

    public BadUsageException(String use, ErrorVisitor err) {
        super("Usage: " + use);
        this.usage = use;
        this.errors = err;
    }

    public Collection getErrors() {
        return errors.getErrors();
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();

        if (errors != null) {
            sb.append(errors.toString());
        }
        return sb.toString();
    }

    public String getUsage() {
        return usage;
    }

}
