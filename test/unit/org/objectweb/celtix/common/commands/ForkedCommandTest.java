package org.objectweb.celtix.common.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.objectweb.celtix.common.i18n.Message;

public class ForkedCommandTest extends TestCase {

    private static final String[] ENV_COMMAND;

    static {
        if (System.getProperty("os.name").startsWith("Windows")) {
            ENV_COMMAND = new String[] {"cmd", "/c", "set"};
        } else {
            ENV_COMMAND = new String[] {"env"};
        }
    }

    public void testBasics() {
        ForkedCommand fc1 = new ForkedCommand();
        String cmdline1 = fc1.toString();
        assertNull(cmdline1);
        try {
            fc1.execute();
        } catch (ForkedCommandException ex) {
            assertEquals("NO_ARGUMENTS_EXC", ex.getCode());
        }
        String[] args = new String[] {"a", "b", "c d e"};
        ForkedCommand fc2 = new ForkedCommand(args);
        String cmdline2 = fc2.toString();
        assertTrue(cmdline2.startsWith("a"));
        assertTrue(cmdline2.endsWith("\""));
        fc1.setArgs(args);
        cmdline1 = fc1.toString();
        assertEquals(cmdline1, cmdline2);
        
        new ForkedCommandException(new NullPointerException());
        Message msg = org.easymock.classextension.EasyMock.createMock(Message.class);
        new ForkedCommandException(msg, new NullPointerException());
    }

    public void testExecuteInDefaultEnvironment() {
        ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
        
        executeEnvCommand(null, bosOut, bosErr);
        
        String output = bosOut.toString();
        assertTrue(output.indexOf("AVAR") < 0 || output.indexOf("BVAR") < 0);      
    }
    
    public void testExecuteInNonDefaultEnvironment() {
        ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
        String[] env = new String[3];
        env[0] = "BVAR=strange";
        if (System.getProperty("os.name").startsWith("Windows")) {
            env[1] = "AVAR=something %BVAR%";
            env[2] = "AVAR=something very %BVAR%";
        } else {
            env[1] = "AVAR=something $BVAR";
            env[2] = "AVAR=something very $BVAR";
        }
        
        
        executeEnvCommand(env, bosOut, bosErr);
        
        // test variables are overwritten but not replaced
        
        boolean found = false;
        String output = bosOut.toString();
        StringTokenizer st = new StringTokenizer(output, System.getProperty("line.separator"));
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.length() > 0) {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    if ("AVAR=something very %BVAR%".equals(line)) {
                        found = true;
                        break;
                    }
                } else {
                    if ("AVAR=something very $BVAR".equals(line)
                        || "AVAR=something $BVAR".equals(line)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        assertTrue(found);
        
    }
    
    public void testTimeout() {
        String[] cmd = new String[] {
            "java" + ForkedCommand.EXE_SUFFIX,
            "org.objectweb.celtix.common.commands.TestCommand",
            "-duration",
            "60000",
        };
        ForkedCommand fc = new ForkedCommand(cmd);
        try {
            fc.execute(1);
            fail("Expected ForkedCommandException not thrown.");
        } catch (ForkedCommandException ex) {
            assertEquals("TIMEOUT_EXC", ex.getCode());
        }
    }

    private void executeEnvCommand(String[] env, ByteArrayOutputStream bosOut, ByteArrayOutputStream bosErr) {

        ForkedCommand fc = new ForkedCommand(ENV_COMMAND);
        if (null != env) {
            fc.setEnvironment(env);
        }
        fc.joinErrOut(true);

        PrintStream pso = new PrintStream(bosOut);
        PrintStream pse = new PrintStream(bosErr);
        fc.setOutputStream(pso);
        fc.setErrorStream(pse);

        int result = fc.execute();
        assertEquals(0, result);
        
    }
    
    
    
    

}
