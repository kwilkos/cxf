package org.objectweb.celtix.tools.generators.java2;

import org.objectweb.celtix.tools.common.model.WSDLModel;

public class WSDLGenerator {
    private final WSDLModel wmodel;

    public WSDLGenerator(WSDLModel model) {
        wmodel = model;
    }

    public void generate() {
        writeDefinition(wmodel);
    }

    public boolean writeDefinition(WSDLModel model) {
        return true;
    }
}
