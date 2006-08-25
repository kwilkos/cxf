package org.objectweb.celtix.tools.common;




public class ToolWrapperGeneratorTest extends ToolTestBase {
    
    private ToolWrapperGenerator generator = new ToolWrapperGenerator(ToolDummy.class.getName());
    
    public void setUp() {
        super.setUp();
    }
    
    public void tearDown() { 
        super.tearDown();
    }
    

    public void testGeneratorUsingDefaultClassLoader() {     
        ToolWrapperGenerator g = new ToolWrapperGenerator(ToolDummy.class.getName());
        assertNotNull(g);
    }
    
    
    public void testGeneratorDelegatesToExternalClass() { 
    
        generator.generate();
        assertTrue("wrapper generator must delegate to tool class", ToolDummy.mainInvoked);
    }
    
    public void testGeneratorPassesAllArgumentsToExternalMain() {
        String[] expectedArgs = {"-b", "foo", "-keep"};
        
        ToolConfig config = new ToolConfig(expectedArgs);
        
        generator.setConfiguration(config);
        generator.generate();
    
        String[] actualArgs = ToolDummy.getArguments();
        
        assertEquals(expectedArgs.length, actualArgs.length);
        for (int i = 0; i < actualArgs.length; i++) {
            assertEquals(expectedArgs[i], actualArgs[i]);
        }
    }
    
    
    static final class ToolDummy {
        
        private static String[] arguments;
        private static boolean mainInvoked; 
        
        private ToolDummy() {
            //complete
        }
        
        public static void main(String[] args) {
            ToolDummy.arguments = args;
            mainInvoked = true;
        }
        
        public static boolean mainInvoked() {
            return mainInvoked;
        }
        
        public static String[] getArguments() {
            return arguments;
        }
    }
    
    
}

