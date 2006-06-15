package org.objectweb.celtix.tools.common;

import org.objectweb.celtix.configuration.CommandlineConfiguration;

/**
 * Interface for code generators used by the tools
 * 
 * @author codea
 */
public interface Generator {

    void setConfiguration(CommandlineConfiguration config);
    void generate();
}

