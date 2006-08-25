package org.objectweb.celtix.js.rhino;

import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.mozilla.javascript.Scriptable;


@WebServiceProvider
public class DOMPayloadProvider extends AbstractDOMProvider implements Provider<DOMSource> {
    public DOMPayloadProvider(Scriptable scope, Scriptable wspVar,
                              String epAddr, boolean isBaseAddr, boolean e4x) {
        super(scope, wspVar, epAddr, isBaseAddr, e4x);
    }
}
