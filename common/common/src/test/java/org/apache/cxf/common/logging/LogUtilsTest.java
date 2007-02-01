/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.common.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.cxf.common.i18n.BundleUtils;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;



public class LogUtilsTest extends TestCase {
    private static final Logger LOG = LogUtils.getL7dLogger(LogUtilsTest.class);


    public void testGetL7dLog() throws Exception {
        assertNotNull("expected non-null logger", LOG);
        assertEquals("unexpected resource bundle name",
                     BundleUtils.getBundleName(LogUtilsTest.class),
                     LOG.getResourceBundleName());
        Logger otherLogger = LogUtils.getL7dLogger(LogUtilsTest.class, "Messages");
        assertEquals("unexpected resource bundle name",
                     BundleUtils.getBundleName(LogUtilsTest.class, "Messages"),
                     otherLogger.getResourceBundleName());
    }

    public void testHandleL7dMessage() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        LOG.addHandler(handler);
        // handler called *before* localization of message
        LogRecord record = new LogRecord(Level.WARNING, "FOOBAR_MSG");
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        LOG.log(Level.WARNING, "FOOBAR_MSG");
        EasyMock.verify(handler);
        LOG.removeHandler(handler);
    }

    public void testLogParamSubstitutionWithThrowable() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        LOG.addHandler(handler);
        // handler called *after* localization of message
        Exception ex = new Exception();
        LogRecord record = new LogRecord(Level.SEVERE, "subbed in 1 only");
        record.setThrown(ex);
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        LogUtils.log(LOG, Level.SEVERE, "SUB1_MSG", ex, 1);
        EasyMock.verify(handler);
        LOG.removeHandler(handler);
    }

    public void testLogParamsSubstitutionWithThrowable() throws Exception {
        Handler handler = EasyMock.createNiceMock(Handler.class);
        LOG.addHandler(handler);
        // handler called *after* localization of message
        Exception ex = new Exception();
        LogRecord record = new LogRecord(Level.SEVERE, "subbed in 4 & 3");
        record.setThrown(ex);
        EasyMock.reportMatcher(new LogRecordMatcher(record));
        handler.publish(record);
        EasyMock.replay(handler);
        LogUtils.log(LOG, Level.SEVERE, "SUB2_MSG", ex, new Object[] {3, 4});
        EasyMock.verify(handler);
        LOG.removeHandler(handler);
    }

    public void testClassMethodNames() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        LOG.addHandler(handler);

        // logger called directly
        LOG.warning("hello");
        
        String cname = handler.cname;
        String mname = handler.mname;
        
        // logger called through LogUtils
        LogUtils.log(LOG, Level.WARNING,  "FOOBAR_MSG");
        
        assertEquals(cname, handler.cname);
        assertEquals(mname, handler.mname);
    }
    
    private static final class TestLogHandler extends Handler {
        String cname;
        String mname;

        public void close() throws SecurityException {
        }
        public void flush() {
        }

        public void publish(LogRecord record) {
            cname = record.getSourceClassName();
            mname = record.getSourceMethodName();
        }       
    }
    
    private static final class LogRecordMatcher implements IArgumentMatcher {
        private final LogRecord record;

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
