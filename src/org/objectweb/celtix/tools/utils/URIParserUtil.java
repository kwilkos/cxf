package org.objectweb.celtix.tools.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public final class URIParserUtil {
    private static final Set<String> KEYWORDS = new HashSet<String>(Arrays
        .asList(new String[] {"abstract", "boolean", "break", "byte", "case", "catch", "char", "class",
                              "const", "continue", "default", "do", "double", "else", "extends", "final",
                              "finally", "float", "for", "goto", "if", "implements", "import", "instanceof",
                              "int", "interface", "long", "native", "new", "package", "private", "protected",
                              "public", "return", "short", "static", "strictfp", "super", "switch",
                              "synchronized", "this", "throw", "throws", "transient", "try", "void",
                              "volatile", "while", "true", "false", "null", "assert", "enum"}));

    private URIParserUtil() {
        // complete
    }

    public static String getPackageName(String nameSpaceURI) {
        int idx = nameSpaceURI.indexOf(':');
        String scheme = "";
        if (idx >= 0) {
            scheme = nameSpaceURI.substring(0, idx);
            if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("urn")) {
                nameSpaceURI = nameSpaceURI.substring(idx + 1);
            }
        }

        List<String> tokens = tokenize(nameSpaceURI, "/: ");
        if (tokens.size() == 0) {
            return null;
        }

        if (tokens.size() > 1) {
            String lastToken = tokens.get(tokens.size() - 1);
            idx = lastToken.lastIndexOf('.');
            if (idx > 0) {
                lastToken = lastToken.substring(0, idx);
                tokens.set(tokens.size() - 1, lastToken);
            }
        }

        String domain = tokens.get(0);
        idx = domain.indexOf(':');
        if (idx >= 0) {
            domain = domain.substring(0, idx);
        }
        List<String> r = reverse(tokenize(domain, scheme.equals("urn") ? ".-" : "."));
        if (r.get(r.size() - 1).equalsIgnoreCase("www")) {
            // remove leading www
            r.remove(r.size() - 1);
        }

        // replace the domain name with tokenized items
        tokens.addAll(1, r);
        tokens.remove(0);

        // iterate through the tokens and apply xml->java name algorithm
        for (int i = 0; i < tokens.size(); i++) {

            // get the token and remove illegal chars
            String token = tokens.get(i);
            token = removeIllegalIdentifierChars(token);

            // this will check for reserved keywords
            if (contiansReservedKeywords(token)) {
                token = '_' + token;
            }

            tokens.set(i, token.toLowerCase());
        }

        // concat all the pieces and return it
        return combine(tokens, '.');
    }

    public static String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i = tokenizer.countTokens() - 1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuffer namespace = new StringBuffer("http://");
        String dot = "";
        for (int i = 0; i < tokens.length; i++) {
            if (i == 1) {
                dot = ".";
            }
            namespace.append(dot + tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

    private static List<String> tokenize(String str, String sep) {
        StringTokenizer tokens = new StringTokenizer(str, sep);
        List<String> r = new ArrayList<String>();

        while (tokens.hasMoreTokens()) {
            r.add(tokens.nextToken());
        }
        return r;
    }

    private static String removeIllegalIdentifierChars(String token) {
        StringBuffer newToken = new StringBuffer();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);

            if (i == 0 && !Character.isJavaIdentifierStart(c)) {
                // prefix an '_' if the first char is illegal
                newToken.append("_" + c);
            } else if (!Character.isJavaIdentifierPart(c)) {
                // replace the char with an '_' if it is illegal
                newToken.append('_');
            } else {
                // add the legal char
                newToken.append(c);
            }
        }
        return newToken.toString();
    }

    private static String combine(List r, char sep) {
        StringBuilder buf = new StringBuilder(r.get(0).toString());

        for (int i = 1; i < r.size(); i++) {
            buf.append(sep);
            buf.append(r.get(i));
        }

        return buf.toString();
    }

    private static <T> List<T> reverse(List<T> a) {
        List<T> r = new ArrayList<T>();

        for (int i = a.size() - 1; i >= 0; i--) {
            r.add(a.get(i));
        }
        return r;
    }

    private static boolean contiansReservedKeywords(String token) {
        return KEYWORDS.contains(token);
    }
}
