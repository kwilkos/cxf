package org.objectweb.celtix.bus.jaxws;

import java.io.InputStream;
import javax.xml.ws.WebServiceContext;
import org.objectweb.celtix.context.WebServiceContextImpl;
import org.objectweb.celtix.resource.ResourceResolver;



public class WebContextResourceResolver implements ResourceResolver {

    // Implementation of org.objectweb.celtix.resource.ResourceResolver

    public final InputStream getAsStream(final String string) {
        return null;
    }

    public final Object resolve(final String string, final Class clz) {
        if (WebServiceContext.class.isAssignableFrom(clz)) { 
            return new WebServiceContextImpl();
        } 
        return null;
    }
}
