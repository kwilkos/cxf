package  org.apache.cxf.helpers;

public final class NSDecl {
    private final String prefix;
    private final String uri;
    private final int hashCode;

    public NSDecl(String pfx, String ur) {
        if (pfx == null) {
            this.prefix = "".intern();
        } else {
            this.prefix = pfx.intern();
        }
        this.uri = ur.intern();
        this.hashCode = (toString()).hashCode();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    public String toString() {
        return prefix + ":" + uri;
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object obj) {
        return obj != null
            && uri == ((NSDecl)obj).uri
            && prefix == ((NSDecl)obj).prefix;
    }

}
