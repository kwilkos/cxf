package org.objectweb.celtix.tools.common.model;

import java.util.*;

public class JavaAnnotation {
    private static final String DEFAULT_QUOTE  = "\"";

    private String tagName;
    private final Map<String, String>  arguments = new HashMap<String, String>();

    public JavaAnnotation() {
    }

    public JavaAnnotation(String tn) {
        this.tagName = tn;
    }

    public void addArgument(String key, String value, String quote) {
        arguments.put(key, quote + value + quote);
    }
    
    public void addArgument(String key, String value) {
        addArgument(key, value, DEFAULT_QUOTE);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("@");
        sb.append(this.tagName);
        Object[] keys = arguments.keySet().toArray();
        if (keys.length > 0) {
            sb.append("(");
            for (int i = 0; i < keys.length; i++) {
                sb.append((String)keys[i]);
                sb.append(" = ");
                sb.append(this.arguments.get((String)keys[i]));
                if (i != (keys.length - 1)) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
