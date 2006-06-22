package org.objectweb.celtix.js.rhino;

import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.mozilla.javascript.Scriptable;


@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)            
public class DOMMessageProvider extends AbstractDOMProvider implements Provider<DOMSource> {
    public DOMMessageProvider(Scriptable scope, Scriptable wspVar,
                              String epAddr, boolean isBaseAddr, boolean e4x) {
        super(scope, wspVar, epAddr, isBaseAddr, e4x);
    }
}
