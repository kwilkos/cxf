package org.objectweb.celtix.bus.configuration;

final class TypeSchemaInfo {

    private String namespaceURI;
    private String location;

    public TypeSchemaInfo(String uri, String l) {
        namespaceURI = uri;
        location = l;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getLocation() {
        return location;
    }

    public int hashCode() {
        return namespaceURI.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null || !(o instanceof TypeSchemaInfo)) {
            return false;
        }
        TypeSchemaInfo other = (TypeSchemaInfo)o;

        return namespaceURI.equals(other.namespaceURI) && location.equals(other.location);
    }
}
