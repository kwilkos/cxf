package org.objectweb.celtix.tools.common;

import org.objectweb.celtix.configuration.Configuration;

/**
 * Interface for code generators used by the tools
 * 
 * @author codea
 */
public interface Generator {

    void setConfiguration(Configuration config);
    void generate();
}

