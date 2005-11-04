package org.objectweb.celtix.tools.common.dom;


import java.util.*;


public class SchemaLocationMap {

    private final Map<String, String>  schemaLocations = new HashMap<String, String> ();

    public void add(String publicId, String systemId) {
        schemaLocations.put(publicId, systemId);
    }

    public int size() {
        return schemaLocations.size();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (Iterator it = schemaLocations.keySet().iterator(); it.hasNext();) {
            String publicId = (String)it.next();

            sb.append(publicId).append(" ").append((String)schemaLocations.get(publicId));
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}

