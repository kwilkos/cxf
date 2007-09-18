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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * java.util.logging.Logger implementation delegating to Log4j.
 * All methods can be used except:
 *   setLevel
 *   addHandler / getHandlers
 *   setParent / getParent
 *   setUseParentHandlers / getUseParentHandlers
 *
 * @author gnodet
 */
public class Log4jLogger extends AbstractDelegatingLogger {

    private static final Map<Level, org.apache.log4j.Level> TO_LOG4J = 
                                                new HashMap<Level, org.apache.log4j.Level>();
    private static final Map<org.apache.log4j.Level, Level> FROM_LOG4J = 
                                                new HashMap<org.apache.log4j.Level, Level>();

    private org.apache.log4j.Logger log;


    static {
        TO_LOG4J.put(Level.ALL,     org.apache.log4j.Level.ALL);
        TO_LOG4J.put(Level.SEVERE,  org.apache.log4j.Level.ERROR);
        TO_LOG4J.put(Level.WARNING, org.apache.log4j.Level.WARN);
        TO_LOG4J.put(Level.INFO,    org.apache.log4j.Level.INFO);
        TO_LOG4J.put(Level.CONFIG,  org.apache.log4j.Level.DEBUG);
        TO_LOG4J.put(Level.FINE,    org.apache.log4j.Level.DEBUG);
        TO_LOG4J.put(Level.FINER,   org.apache.log4j.Level.TRACE);
        TO_LOG4J.put(Level.FINEST,  org.apache.log4j.Level.TRACE);
        TO_LOG4J.put(Level.OFF,     org.apache.log4j.Level.OFF);
        FROM_LOG4J.put(org.apache.log4j.Level.ALL,   Level.ALL);
        FROM_LOG4J.put(org.apache.log4j.Level.ERROR, Level.SEVERE);
        FROM_LOG4J.put(org.apache.log4j.Level.WARN,  Level.WARNING);
        FROM_LOG4J.put(org.apache.log4j.Level.INFO,  Level.INFO);
        FROM_LOG4J.put(org.apache.log4j.Level.DEBUG, Level.FINE);
        FROM_LOG4J.put(org.apache.log4j.Level.TRACE, Level.FINEST);
        FROM_LOG4J.put(org.apache.log4j.Level.OFF,   Level.OFF);
    }

    public Log4jLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
        log = org.apache.log4j.LogManager.getLogger(name);
    }

    public Level getLevel() {
        for (org.apache.log4j.Category c = log; c != null; c = c.getParent()) {
            org.apache.log4j.Level l = c.getLevel();
            if (l != null) {
                return FROM_LOG4J.get(l);
            }
        }
        return null;
    }

    protected void internalLogFormatted(String msg, LogRecord record) {
        log.log(AbstractDelegatingLogger.class.getName(),
                TO_LOG4J.get(record.getLevel()),
                msg,
                record.getThrown());
    }

}
