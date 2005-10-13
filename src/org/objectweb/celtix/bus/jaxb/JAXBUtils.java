package org.objectweb.celtix.bus.jaxb;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.StringTokenizer;

public final class JAXBUtils {
    
    private static final String[] KEYWORDS = new String[] {       
        "abstract",    "continue",    "for",           "new",          "switch",
        "assert",      "default",     "if",            "package",      "synchronized",
        "boolean",     "do",          "goto",          "private",      "this",
        "break",       "double",      "implements",    "protected",    "throw",
        "byte",        "else",        "import",        "public",       "throws",
        "case",        "enum",        "instanceof",    "return",       "transient",
        "catch",       "extends",     "int",           "short",        "try",
        "char",        "final",       "interface",     "static",       "void", 
        "class",       "finally",     "long",          "strictfp",     "volatile",
        "const",       "float",       "native",        "super",        "while",
    };
    
    /**
     * prevents instantiation
     *
     */
    private JAXBUtils() {
    }
    
    /** 
     * Checks if the specified word is a Java keyword (as of 1.5).
     * 
     * @param word the word to check.
     * @return true if the word is a keyword.
     */
    public static boolean isJavaKeyword(String word) {
        for (String w : KEYWORDS) {
            if (w.equals(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a Java package name from a URI according to the
     * algorithm outlined in JAXB 2.0.
     * 
     * @param namespaceURI the namespace URI.
     * @return the package name.
     */
    public static String namespaceURIToPackage(String namespaceURI) {
        try {
            return nameSpaceURIToPackage(new URI(namespaceURI));
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    /**
     * Generates a Java package name from a URI according to the
     * algorithm outlined in JAXB 2.0.
     * 
     * @param namespaceURI the namespace URI.
     * @return the package name.
     */
    public static String nameSpaceURIToPackage(URI uri) {
        
        StringBuffer packageName = new StringBuffer();   
        
        String authority = uri.getAuthority();
        
        if ("urn".equals(uri.getScheme())) {
            packageName.append(authority);
            for (int i = 0; i < packageName.length(); i++) {
                if (packageName.charAt(i) == '-') {
                    packageName.setCharAt(i, '.');
                }
            }
            authority = packageName.toString();
            packageName.setLength(0);
        }
        
        StringTokenizer st = new StringTokenizer(authority, ".");
        if (st.hasMoreTokens()) {
            String token = null;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                if (packageName.length() == 0) {
                    if ("www".equals(token)) {
                        continue;
                    }
                } else {
                    packageName.insert(0, ".");
                }
                packageName.insert(0, token);
            }
            
            if (!("com".equals(token) || "gov".equals(token) || "net".equals(token) || "org".equals(token)
                || "edu".equals(token))) {
                packageName.setLength(0);

            }
        }

        String path = uri.getPath();
        int index = path.lastIndexOf('.');
        if (index < 0) {
            index = path.length();
        }
        st = new StringTokenizer(path.substring(0, index), "/");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (packageName.length() > 0) {
                packageName.append('.');
            }
            packageName.append(nameToIdentifier(token));
        }
        return packageName.toString();
    }
    
    private static String nameToIdentifier(String name) {
        StringBuffer sname = new StringBuffer(name.toLowerCase());

        for (int i = 0; i < sname.length(); i++) {
            sname.setCharAt(i, Character.toLowerCase(sname.charAt(i)));
        }
        
        for (int i = 0; i < sname.length(); i++) {
            if (!Character.isJavaIdentifierPart(sname.charAt(i))) {
                sname.setCharAt(i, '_');
            }
        }
        
        if (isJavaKeyword(sname.toString())) {
            sname.insert(0, '_');
        }
        
        if (!Character.isJavaIdentifierStart(sname.charAt(0))) {
            sname.insert(0, '_');  
        }
                
        return sname.toString();
    }

}
