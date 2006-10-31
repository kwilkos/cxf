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

package org.apache.cxf.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class StringUtils {

    private StringUtils() {
    }

    public static String extract(String string, String startToken, String endToken) {
        int start = string.indexOf(startToken) + startToken.length();
        int end = string.lastIndexOf(endToken);

        if (start == -1 || end == -1) {
            return null;
        }

        return string.substring(start, end);
    }

    public static String wrapper(String string, String startToken, String endToken) {
        StringBuffer sb = new StringBuffer();
        sb.append(startToken);
        sb.append(string);
        sb.append(endToken);
        return sb.toString();
    }

    public static boolean isFileExist(String file) {
        return new File(file).exists() && new File(file).isFile();
    }

    public static boolean isFileAbsolute(String file) {
        return isFileExist(file) && new File(file).isAbsolute();
    }

    public static URL getURL(String spec) throws MalformedURLException {
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            return new File(spec).toURL();
        }
    }

    public static boolean isEmpty(String str) {
        if (str != null && str.trim().length() > 0) {
            return false;
        }
        return true;
    }
    
    public static String trim(String target, String token) {
        int tokenLength = token.length();
        int targetLength = target.length();
        
        if (target.startsWith(token)) {            
            return trim(target.substring(tokenLength), token);
        }
        if (target.endsWith(token)) {            
            return trim(target.substring(0, targetLength - tokenLength), token);
        }
        return target;
    }

    public static boolean isEqualUri(String uri1, String uri2) {

        if (uri1.substring(uri1.length() - 1).equals("/") && !uri2.substring(uri2.length() - 1).equals("/")) {
            return uri1.substring(0, uri1.length() - 1).equals(uri2);
        } else if (uri2.substring(uri2.length() - 1).equals("/")
                   && !uri1.substring(uri1.length() - 1).equals("/")) {
            return uri2.substring(0, uri2.length() - 1).equals(uri1);
        } else {
            return uri1.equals(uri2);
        }
    }
    
    public static String diff(String str1, String str2) {
        int index = str1.lastIndexOf(str2);
        if (index > -1) {
            return str1.substring(str2.length());
        }
        return str1;
    }
    
    public static List<String> getParts(String str, String sperator) {
        List<String> ret = new ArrayList<String>();
        List<String> parts = Arrays.asList(str.split("/"));
        for (String part : parts) {
            if (!isEmpty(part)) {
                ret.add(part);
            }
        }
        return ret;
    }
    
    public static String getFirstNotEmpty(String str, String sperator) {
        List<String> parts = Arrays.asList(str.split("/"));
        for (String part : parts) {
            if (!isEmpty(part)) {
                return part;
            }
        }
        return str;
    }
}
