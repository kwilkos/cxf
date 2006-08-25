package org.objectweb.celtix.common.util;

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

    public static boolean isEmpty(String str) {
        if (str != null && str.trim().length() > 0) {
            return false;
        }
        return true;
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
}
