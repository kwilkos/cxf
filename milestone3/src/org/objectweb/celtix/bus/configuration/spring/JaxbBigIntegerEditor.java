package org.objectweb.celtix.bus.configuration.spring;

import java.math.BigInteger;

public class JaxbBigIntegerEditor extends JaxbPropertyEditor {

    public void setAsText(String text) {
        setValue(new BigInteger(text));      
    }
}
