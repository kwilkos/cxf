package org.objectweb.celtix.common.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import org.objectweb.celtix.common.i18n.BundleUtils;


public class LogUtilsTest extends TestCase {
    private Logger logger;

    public void setUp() {
        logger = LogUtils.getL7dLogger(LogUtilsTest.class);
    }

    public void testGetL7dLog() throws Exception {
        assertNotNull("expected non-null logger", logger);
        assertEquals("unexpected resource bundle name",
                     BundleUtils.getBundleName(LogUtilsTest.class),
                     logger.getResourceBundleName());
    }

    public void testHandleL7dMessage() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        logger.addHandler(handler);
        // handler called *before* localization of message
        LogRecord record = new LogRecord(Level.WARNING, "FOOBAR_MSG");
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        logger.log(Level.WARNING, "FOOBAR_MSG");
        EasyMock.verify(handler);
    }

    public void testLogParamSubstitutionWithThrowable() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        logger.addHandler(handler);
        // handler called *after* localization of message
        Exception ex = new Exception();
        LogRecord record = new LogRecord(Level.SEVERE, "subbed in 1 only");
        record.setThrown(ex);
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        LogUtils.log(logger, Level.SEVERE, "SUB1_MSG", ex, 1);
        EasyMock.verify(handler);
    }

    public void testLogParamsSubstitutionWithThrowable() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        logger.addHandler(handler);
        // handler called *after* localization of message
        Exception ex = new Exception();
        LogRecord record = new LogRecord(Level.SEVERE, "subbed in 4 & 3");
        record.setThrown(ex);
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        LogUtils.log(logger, Level.SEVERE, "SUB2_MSG", ex, new Object[] {3, 4});
        EasyMock.verify(handler);
    }

    private static final class LogRecordMatcher implements IArgumentMatcher {
        private LogRecord record;

        private LogRecordMatcher(LogRecord r) {
            this.record = r;
        }

        public boolean matches(Object obj) {
            if (obj instanceof LogRecord) {
                LogRecord other = (LogRecord)obj;
                return record.getMessage().equals(other.getMessage())
                       && record.getLevel().equals(other.getLevel())
                       && record.getThrown() == other.getThrown();
            }
            return false;
        }    

        public void appendTo(StringBuffer buffer) {
            buffer.append("log records did not match");
        }
    } 
}
