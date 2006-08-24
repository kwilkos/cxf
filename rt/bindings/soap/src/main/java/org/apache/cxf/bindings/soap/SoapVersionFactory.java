package org.apache.cxf.bindings.soap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class SoapVersionFactory {
    private static SoapVersionFactory factory = new SoapVersionFactory();

    static {
        getInstance().register(Soap11.getInstance());
        getInstance().register(Soap12.getInstance());
    }
    
    private Map<String, SoapVersion> versions = new HashMap<String, SoapVersion>();
    
    public static SoapVersionFactory getInstance() {
        return factory;
    }
    
    public SoapVersion getSoapVersion(String namespace) {
        return versions.get(namespace);
    }
    
    public void register(SoapVersion version) {
        versions.put(version.getNamespace(), version);
    }

    public Iterator<SoapVersion> getVersions() {
        return versions.values().iterator();
    }
}
