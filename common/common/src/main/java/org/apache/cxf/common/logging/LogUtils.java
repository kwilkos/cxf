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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.BundleUtils;


/**
 * A container for static utility methods related to logging.
 */
public final class LogUtils {
    
    private static final Object[] NO_PARAMETERS = new Object[0];

    private static Class<?> loggerClass;
    
    /**
     * Prevents instantiation.
     */
    private LogUtils() {
    }

    /**
     * Enable users to use their own logger implementation.
     */
    public static void setLoggerClass(Class<?> cls) {
        loggerClass = cls;
    }

    /**
     * Get a Logger with the associated default resource bundle for the class.
     *
     * @param cls the Class to contain the Logger
     * @return an appropriate Logger 
     */
    public static Logger getL7dLogger(Class<?> cls) {
        return createLogger(cls, null);
    }
    
    /**
     * Get a Logger with an associated resource bundle.
     *
     * @param cls the Class to contain the Logger
     * @param name the resource name
     * @return an appropriate Logger 
     */
    public static Logger getL7dLogger(Class<?> cls, String name) {
        return createLogger(cls, name);
    }

    /**
     * Create a logger
     */
    protected static Logger createLogger(Class<?> cls, String name) {
        if (loggerClass != null) {
            try {
                Constructor cns = loggerClass.getConstructor(String.class, String.class);
                if (name == null) {
                    try {
                        return (Logger) cns.newInstance(cls.getName(), BundleUtils.getBundleName(cls));
                    } catch (InvocationTargetException ite) {
                        if (ite.getTargetException() instanceof MissingResourceException) {
                            return (Logger) cns.newInstance(cls.getName(), null);
                        } else {
                            throw ite;
                        }
                    } 
                } else {
                    return (Logger) cns.newInstance(cls.getName(), BundleUtils.getBundleName(cls, name));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (name == null) {
            try {
                return Logger.getLogger(cls.getName(), BundleUtils.getBundleName(cls));
            } catch (MissingResourceException rex) {
                return Logger.getLogger(cls.getName(), null);
            }
        } else {
            return Logger.getLogger(cls.getName(), BundleUtils.getBundleName(cls, name));
        }
    }

    /**
     * Allows both parameter substitution and a typed Throwable to be logged.
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     * @param throwable the Throwable to log
     * @param parameter the parameter to substitute into message
     */
    public static void log(Logger logger, 
                           Level level, 
                           String message, 
                           Throwable throwable,
                           Object parameter) {
        if (logger.isLoggable(level)) {
            final String formattedMessage = 
                MessageFormat.format(localize(logger, message), parameter);
            doLog(logger, level, formattedMessage, throwable);
        }
    }

    /**
     * Allows both parameter substitution and a typed Throwable to be logged.
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     * @param throwable the Throwable to log
     * @param parameters the parameters to substitute into message
     */
    public static void log(Logger logger, 
                           Level level, 
                           String message, 
                           Throwable throwable,
                           Object... parameters) {
        if (logger.isLoggable(level)) {
            final String formattedMessage = 
                MessageFormat.format(localize(logger, message), parameters);
            doLog(logger, level, formattedMessage, throwable);
        }
    }
 
    /**
     * Checks log level and logs
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     */    
    public static void log(Logger logger, 
                           Level level, 
                           String message) {
        if (logger.isLoggable(level)) {
            final String formattedMessage = 
                MessageFormat.format(localize(logger, message), NO_PARAMETERS);
            doLog(logger, level, formattedMessage, null);
        }
        
    }  
    
    /**
     * Checks log level and logs
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     * @param throwable the Throwable to log
     */      
    public static void log(Logger logger, 
                           Level level, 
                           String message, 
                           Throwable throwable) {
        log(logger, level, message, throwable, NO_PARAMETERS);
    }
  
    /**
     * Checks log level and logs
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     * @param parameter the parameter to substitute into message
     */      
    public static void log(Logger logger, 
                           Level level, 
                           String message, 
                           Object parameter) {
        log(logger, level, message, new Object[] {parameter});
    }
    
    /**
     * Checks log level and logs
     *
     * @param logger the Logger the log to
     * @param level the severity level
     * @param message the log message
     * @param parameters the parameters to substitute into message
     */      
    public static void log(Logger logger, 
                           Level level, 
                           String message, 
                           Object[] parameters) {
        if (logger.isLoggable(level)) {
            final String formattedMessage = 
                MessageFormat.format(localize(logger, message), parameters);
            doLog(logger, level, formattedMessage, null);
        }
        
    }

    private static void doLog(Logger log, Level level, String msg, Throwable t) {
        LogRecord record = new LogRecord(level, msg);
    
        record.setLoggerName(log.getName());
        record.setResourceBundleName(log.getResourceBundleName());
        record.setResourceBundle(log.getResourceBundle());
            
        if (t != null) {
            record.setThrown(t);
        }
        
        //try to get the right class name/method name - just trace
        //back the stack till we get out of this class
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        String cname = LogUtils.class.getName();
        for (int x = 0; x < stack.length; x++) {
            StackTraceElement frame = stack[x];
            if (!frame.getClassName().equals(cname)) {
                record.setSourceClassName(frame.getClassName());
                record.setSourceMethodName(frame.getMethodName());
                break;
            }
        }
        log.log(record);
    }

    /**
     * Retreive localized message retreived from a logger's resource
     * bundle.
     *
     * @param logger the Logger
     * @param message the message to be localized
     */
    private static String localize(Logger logger, String message) {
        ResourceBundle bundle = logger.getResourceBundle();
        return bundle != null ? bundle.getString(message) : message;
    }

}
