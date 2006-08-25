package org.objectweb.celtix.bus;

import org.objectweb.celtix.configuration.AbstractCommandLineConfiguration;
import org.objectweb.celtix.configuration.CommandLineOption;

public class BusArguments extends AbstractCommandLineConfiguration {
    
    private static final CommandLineOption BUS_ID_OPT;
    
    
    static {
        BUS_ID_OPT = new CommandLineOption("-BUSid");
    }
    
    BusArguments(String[] args) {    
        addOption(BUS_ID_OPT);
        parseCommandLine(args, false);    
    }
    
    String getBusId() {
        return null;
    }
    
}
