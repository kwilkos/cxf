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

package org.apache.cxf.javascript;

import java.util.Stack;

/**
 * 
 */
public class JavascriptUtils {
    private static final String NL = "\n";
    private StringBuffer code;
    private Stack<String> prefixStack;
    private String xmlStringAccumulatorVariable;
    
    public JavascriptUtils(StringBuffer code) {
        this.code = code;
        prefixStack = new Stack<String>();
        prefixStack.push("");
    }
    
    public void startXmlStringAccumulator(String variableName) {
        xmlStringAccumulatorVariable = variableName;
        code.append(prefix());
        code.append("var ");
        code.append(variableName);
        code.append(";" + NL);
    }
    
    public static String protectSingleQuotes(String value) {
        return value.replaceAll("'", "\\'");
    }
    
    /**
     * emit javascript to append a value to the accumulator. 
     * @param value
     */
    public void appendAppend(String value) {
        code.append(prefix());
        code.append(xmlStringAccumulatorVariable + " = " + xmlStringAccumulatorVariable + " + ");
        code.append(value);
        code.append(";" + NL);
    }
    
    private String prefix() {
        return prefixStack.peek();
    }
    
    public void appendLine(String line) {
        code.append(prefix());
        code.append(line);
        code.append(NL);
    }
    
    public void startIf(String test) {
        code.append(prefix());
        code.append("if (" + test + ") {" + NL);
        prefixStack.push(prefix() + " ");
    }
    
    public void appendElse() {
        prefixStack.pop();
        code.append(prefix());
        code.append("} else {" + NL);
        prefixStack.push(prefix() + " ");
    }
    
    public void endBlock() {
        prefixStack.pop();
        code.append(prefix());
        code.append("}" + NL);
    }
    
    public void startFor(String start, String test, String increment) {
        code.append(prefix());
        code.append("for (" + start + ";" + test + ";" + increment + ") {" + NL);
        prefixStack.push(prefix() + " ");
    }
}
