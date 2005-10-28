package org.objectweb.celtix.bus.jaxb;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class JAXBUtils {
    
    public enum IdentifierType {
        CLASS,
        INTERFACE,
        GETTER,
        SETTER,
        VARIABLE,
        CONSTANT
    };
    
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
    
    private static final char[] XML_NAME_PUNCTUATION_CHARS = new char[] {
        /* hyphen                       */ '\u002D', 
        /* period                       */ '\u002E',
        /* colon                        */'\u003A',
        /* dot                          */ '\u00B7',
        /* greek ano teleia             */ '\u0387',
        /* arabic end of ayah           */ '\u06DD',
        /* arabic start of rub el hizb  */'\u06DE',
        /* underscore                   */ '\u005F',
    };
    
    private static final String XML_NAME_PUNCTUATION_STRING = new String(XML_NAME_PUNCTUATION_CHARS);
    
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
            packageName.append(normalizePackageNamePart(token));
        }
        return packageName.toString();
    }
    
    private static String normalizePackageNamePart(String name) {
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
    
    
    /**
     * Converts an XML name to a Java identifier according to the mapping
     * algorithm outlines in the JAXB specification
     * 
     * @param name the XML name
     * @return the Java identifier
     */
    public static String nameToIdentifier(String name, IdentifierType type) {

        if (null == name || name.length() == 0) {
            return name;
        }

        // algorithm will not change an XML name that is already a legal and
        // conventional (!) Java class, method, or constant identifier

        boolean legalIdentifier = false;
        StringBuffer buf = new StringBuffer(name);
        legalIdentifier = Character.isJavaIdentifierStart(buf.charAt(0));

        for (int i = 1; i < name.length() && legalIdentifier; i++) {
            legalIdentifier = legalIdentifier && Character.isJavaIdentifierPart(buf.charAt(i));
        }
        
        boolean conventionalIdentifier = isConventionalIdentifier(buf, type); 
        if (legalIdentifier && conventionalIdentifier) {
            if (JAXBUtils.isJavaKeyword(name) && type == IdentifierType.VARIABLE) {
                name = normalizePackageNamePart(name.toString());
            }
            return name;
        }
        
        // split into words 

        List<String> words = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(name, XML_NAME_PUNCTUATION_STRING);
        while (st.hasMoreTokens()) {
            words.add(st.nextToken());
        }        

        for (int i = 0; i < words.size(); i++) {
            splitWord(words, i);
        }
        
        return makeConventionalIdentifier(words, type);
    }
    
    private static void splitWord(List<String> words, int listIndex) {
        String word = words.get(listIndex);
        if (word.length() <= 1) {
            return;
        }
        int index = listIndex + 1;
        StringBuffer sword = new StringBuffer(word);
        int first = 0;
        char firstChar = sword.charAt(first);
        if (Character.isLowerCase(firstChar)) {
            sword.setCharAt(first, Character.toUpperCase(firstChar));
        }
        int i = 1;
        
        while (i < sword.length()) {  
            if (Character.isDigit(firstChar)) {
                while (i < sword.length() && Character.isDigit(sword.charAt(i))) {
                    i++;
                }
            } else if (isCasedLetter(firstChar)) {
                boolean previousIsLower = Character.isLowerCase(firstChar); 
                while (i < sword.length() && isCasedLetter(sword.charAt(i))) {
                    if (Character.isUpperCase(sword.charAt(i)) && previousIsLower) {
                        break;
                    } 
                    previousIsLower = Character.isLowerCase(sword.charAt(i));
                    i++;
                }             
            } else { 
                // first must be a mark or an uncased letter
                while (i < sword.length() && (isMark(sword.charAt(i)) || !isCasedLetter(sword.charAt(i)))) {
                    i++;
                }       
            }
            
            // characters from first to i are all either
            // * digits
            // * upper or lower case letters, with only the first one an upper
            // * uncased letters or marks
            
            
            String newWord = sword.substring(first, i);
            words.add(index, newWord);
            index++;
            if (i >= sword.length()) {
                break;
            } else {
                first = i;
                firstChar = sword.charAt(first);
            }
        }
        
        if (index > (listIndex + 1)) {
            words.remove(listIndex);
        }
    }
    
    private static boolean isMark(char c) {
        return Character.isJavaIdentifierPart(c) && !Character.isLetter(c) && !Character.isDigit(c);
    }
    
    private static boolean isCasedLetter(char c) {
        return Character.isUpperCase(c) || Character.isLowerCase(c);
    }
    
    private static boolean isConventionalIdentifier(StringBuffer buf, IdentifierType type) {
        if (null == buf || buf.length() == 0) {
            return false;
        }
        boolean result = false;
        if (IdentifierType.CONSTANT == type) {
            for (int i = 0; i < buf.length(); i++) {
                if (Character.isLowerCase(buf.charAt(i))) {
                    return false;                  
                }
            }
            result = true;
        } else if (IdentifierType.VARIABLE == type) {
            result = Character.isLowerCase(buf.charAt(0));
        } else {
            int pos = 3;
            if (IdentifierType.GETTER == type 
                && !(buf.length() >= pos 
                    && "get".equals(buf.subSequence(0, 3)))) {
                return false;
            } else if (IdentifierType.SETTER == type 
                && !(buf.length() >= pos && "set".equals(buf.subSequence(0, 3)))) {
                return false;
            } else {
                pos = 0;
            }
            result = Character.isUpperCase(buf.charAt(pos));
        }
        return result;
    }
    
    private static String makeConventionalIdentifier(List<String> words, IdentifierType type) {
        StringBuffer buf = new StringBuffer();
        boolean firstWord = true;
        if (IdentifierType.GETTER == type) {
            buf.append("get");
        } else if (IdentifierType.SETTER == type) {
            buf.append("set");
        }
        for (String w : words) {
            int l = buf.length();
            if (l > 0 && IdentifierType.CONSTANT == type) {
                buf.append('_');
                l++;
            }
            buf.append(w);
            if (IdentifierType.CONSTANT == type) {
                for (int i = l; i < buf.length(); i++) {
                    if (Character.isLowerCase(buf.charAt(i))) {
                        buf.setCharAt(i, Character.toUpperCase(buf.charAt(i)));                  
                    }
                }
            } else if (IdentifierType.VARIABLE == type) {
                if (firstWord && Character.isUpperCase(buf.charAt(l))) {
                    buf.setCharAt(l, Character.toLowerCase(buf.charAt(l)));
                }
            } else {
                if (firstWord && Character.isLowerCase(buf.charAt(l))) {
                    buf.setCharAt(l, Character.toUpperCase(buf.charAt(l)));
                }
            }
            firstWord = false;
        }
        return buf.toString();
    }

}
