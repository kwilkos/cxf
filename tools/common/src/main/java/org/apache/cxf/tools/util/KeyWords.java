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
package org.apache.cxf.tools.util;

import java.util.ArrayList;
import java.util.List;

public final class KeyWords {
    
    private static String[] keywords = {"abstract", "boolean", "byte", "case", "catch", "char", "class",
                                        "continue", "default", "do", "double", "else", "extends", "final",
                                        "finally", "float", "for", "if", "implements", "import",
                                        "instanceof", "int", "interface", "long", "native", "new", "package",
                                        "private", "protected", "public", "return", "short", "static",
                                        "super", "switch", "synchronized", "this", "throw", "throws",
                                        "transient", "try", "void", "volatile", "while", "false", "true",
                                        "null"};

    private static List<String> keywordsList = new ArrayList<String>();

    static {
        for (int i = 0; i < keywords.length; i++) {
            keywordsList.add(keywords[i]);
        }
    }

    private KeyWords() {
        
    }
    public static boolean isKeywords(String word) {
        return keywordsList.contains(word);
    }
}
