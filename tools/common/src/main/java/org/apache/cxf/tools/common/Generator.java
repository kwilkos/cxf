package org.apache.cxf.tools.common;

import org.apache.cxf.configuration.CommandlineConfiguration;

/**
 * Interface for code generators used by the tools
 * 
 * @author codea
 */
public interface Generator {

    void setConfiguration(CommandlineConfiguration config);
    void generate();
}

