package org.objectweb.celtix.tools.common;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.objectweb.celtix.configuration.Configuration;

public abstract class DelegatingToolTestBase extends ToolTestBase {
    
    private Generator mockGenerator;
    
    protected abstract ToolBase createTool(String[] args, Generator theGenerator);

    public void setUp() {
        super.setUp();
        mockGenerator = EasyMock.createNiceMock(Generator.class);
    }
    
    public void tearDown() {
        super.tearDown();
    }
    
       
    public void testMainDelegatesToGenerator() { 
        
        mockGenerator = EasyMock.createMock(Generator.class);
        mockGenerator.setConfiguration((Configuration)EasyMock.notNull());    
        mockGenerator.generate();    
        EasyMock.replay(mockGenerator); 
        
        ToolBase tool = createTool(new String[] {wsdlLocation.toString()}, mockGenerator);                
        assertNotNull(tool);
        tool.run();
        
        EasyMock.verify(mockGenerator);
    }
    
    public void testArgumentsAvailableInConfig() {
        doArgumentsAvailableInConfigTest(new String[] {"-b", "wibble", "-keep", "-version", "foo.wsdl"});
        EasyMock.reset(mockGenerator);
        doArgumentsAvailableInConfigTest(new String[] {"foo.wsdl"});
    }
    
    private void doArgumentsAvailableInConfigTest(String[] args) {
        ToolConfig expectedConfig = new ToolConfig(args);
        assertNotNull(expectedConfig.getOriginalArgs());
        mockGenerator.setConfiguration(eqCommandLineArguments(expectedConfig));
        
        EasyMock.replay(mockGenerator);
        createTool(args, mockGenerator).run();
        EasyMock.verify(mockGenerator);             
    }
    
    static class CommandLineConfigurationMatcher implements IArgumentMatcher {
        
        private final ToolConfig config; 
        
        public CommandLineConfigurationMatcher(ToolConfig expected) { 
            config = expected;
        }
        
        
        public boolean matches(Object obj) {
            if (!(obj instanceof ToolConfig)) {
                return false;
            }     
            String[] expectedArgs = config.getOriginalArgs();
            String[] args = ((ToolConfig)obj).getOriginalArgs();
            
            if (args == null || expectedArgs.length != args.length) {
                return false;
            }
            for (int i = 0; i < expectedArgs.length; i++) {
                if (!expectedArgs[i].equals(args[i])) {
                    return false;
                }
            }
            return true;
        }
        
        public void appendTo(StringBuffer buffer) {
            buffer.append("command line arguments do not match");
        }
    }
    
    public static ToolConfig eqCommandLineArguments(ToolConfig in) {
        EasyMock.reportMatcher(new CommandLineConfigurationMatcher(in));
        return in;
    }
    
    
}
