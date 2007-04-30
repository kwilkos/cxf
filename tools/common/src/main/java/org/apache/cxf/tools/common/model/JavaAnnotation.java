/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.common.model;

import java.util.*;

import org.apache.cxf.common.util.StringUtils;

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
        if (!StringUtils.isEmpty(value)) {
            arguments.put(key, quote + value + quote);
        }        
    }
    
    public void addArgument(String key, String value) {
        addArgument(key, value, DEFAULT_QUOTE);
    }
    
    public Map<String, String> getArguments() {
        return arguments;
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
                String value = this.arguments.get((String)keys[i]);
                if ("null".equals(value)) {
                    continue;
                }
                sb.append(" = ");
                sb.append(value);
                if (i != (keys.length - 1)) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
