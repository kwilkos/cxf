package org.objectweb.celtix.bus.handlers;

import java.util.Map;

public class TestHandlerWithInit extends TestHandler {


    public void init(Map<String, Object> map) {
        if (map.containsKey(STRING_PARAM_NAME)) {
            setStringParam((String)map.get(STRING_PARAM_NAME));
        }
    }    
}
