package org.objectweb.celtix.tools.common.model;

import java.util.*;

public class JavaAnnotation {
    private String tagName;
    private final Map<String, String>  arguments = new HashMap<String, String>();

    public JavaAnnotation() {
    }

    public JavaAnnotation(String tn) {
        this.tagName = tn;
    }

    public void addArgument(String key, String value) {
        arguments.put(key, value);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("@");
        sb.append(this.tagName);
        Object[] keys = arguments.keySet().toArray();
        if (keys.length > 1) {
            sb.append("(");
            for (int i = 0; i < keys.length; i++) {
                sb.append((String) keys[i]);
                sb.append(" = \"");
                sb.append(this.arguments.get((String)keys[i]));
                sb.append("\"");
                if (i != (keys.length - 1)) {
                    sb.append(",");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
