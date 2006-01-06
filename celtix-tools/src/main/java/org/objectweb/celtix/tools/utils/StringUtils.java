package org.objectweb.celtix.tools.utils;

import java.io.*;
import java.net.*;

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
}
