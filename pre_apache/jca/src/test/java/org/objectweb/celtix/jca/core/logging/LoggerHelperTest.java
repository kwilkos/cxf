package org.objectweb.celtix.jca.core.logging;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class LoggerHelperTest extends TestCase {
    public static final String TEST_LOGGER_NAME = "test.logger";

    public LoggerHelperTest(String name) {
        super(name);
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testEnableDisableConsoleLogging() {
        Logger rootLogger = Logger.getLogger("");
        Handler handler;
        
        /*Handler handler = LoggerHelper.getHandler(rootLogger, LoggerHelper.CONSOLE_HANDLER);
        assertNotNull("default console appender is there", handler);*/

        LoggerHelper.enableConsoleLogging();

        handler = LoggerHelper.getHandler(rootLogger, LoggerHelper.CONSOLE_HANDLER);
        assertNotNull("default console appender is there", handler);

        LoggerHelper.disableConsoleLogging();

        handler = LoggerHelper.getHandler(rootLogger, LoggerHelper.CONSOLE_HANDLER);
        assertNull("Unexpected appender after disable", handler);
    }

    public void testSettingLogLevel() {
        LoggerHelper.setRootLoggerName(TEST_LOGGER_NAME);
        LoggerHelper.setLogLevel("INFO");
        assertEquals("incorrect log level", "INFO", LoggerHelper.getLogLevel());
        assertEquals("log level not set on IONA logger", "INFO", Logger.getLogger(TEST_LOGGER_NAME)
            .getLevel().toString());
    }

    public void testSetWriter() {
        // setup an dummy writer
        DummyWriter writer = new DummyWriter();
        assertTrue("The DummyWriter init error", !writer.writed);
        LoggerHelper.initializeLoggingOnWriter(writer);
        LoggerHelper.setLogLevel("INFO");
        LoggerHelper.getRootCeltixLogger().severe("Test String");
        assertTrue("The DummyWriter didn't be setup", writer.writed);
    }

    public static Test suite() {
        return new TestSuite(LoggerHelperTest.class);
    }

    class DummyWriter extends Writer {
        boolean writed;
        boolean flushed;
        boolean closed;

        public void write(char[] cbuf, int off, int len) throws IOException {

            writed = true;
        }

        @Override
        public void flush() throws IOException {
            flushed = true;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {LoggerHelperTest.class.getName()});
    }
}
