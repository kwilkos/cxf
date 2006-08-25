package org.objectweb.celtix.jca.core.logging;

import java.io.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class LoggerHelper {
    public static final Level DEFAULT_LOG_LEVEL = Level.WARNING;    
    public static final String CONSOLE_HANDLER = "ConsoleHandler";
    public static final String WRITER_HANDLER = "WriterHandler";
    private static String rootLoggerName = "org.objectweb.celtix";
    private static boolean initComplete;
    private static Level currentLogLevel = Level.WARNING;;

    private LoggerHelper() {
        //do nothing here
    }

    public static void initializeLoggingOnWriter(final Writer writer) {
        if (writer != null) {
            if (writer.getClass().getName().startsWith("org.jboss")) {
                // jboss writer will redirect to log4j which will cause an
                // infinite loop if we install an appender over this writer.
                // Continue logging via log4j and ignore this writer.
                //
                return;
            }
            Logger celtixLogger = getRootCeltixLogger();

            // test if the stream handler were setted
            if (getHandler(celtixLogger, WRITER_HANDLER) == null) {
                final WriterHandler handler = new WriterHandler(writer);
                celtixLogger.addHandler(handler);
            }
            // log just to the print writer
            disableConsoleLogging();
        }
    }
    
    public static void deleteLoggingOnWriter() {
        Logger celtixLogger = getRootCeltixLogger();
        Handler handler = getHandler(celtixLogger, WRITER_HANDLER);
        
        if (handler != null) {
            celtixLogger.removeHandler(handler);
        }
        enableConsoleLogging();
    }

    // true if log output is already going somewhere
    public static boolean loggerInitialisedOutsideConnector() {       
        final Handler[] handlers = Logger.getLogger("").getHandlers();        
        return handlers.length > 0;
    }

    static Handler getHandler(Logger log, String handlerName) {
        Handler[] handlers = log.getHandlers();
        Handler result = null;
        for (int i = 0; i < handlers.length; i++) {
            if (handlers[i].getClass().getName().endsWith(handlerName)) {
                result = handlers[i];
            }
        }
        return result;
    }

    public static void disableConsoleLogging() {        
        final Handler handler = getHandler(Logger.getLogger(""), CONSOLE_HANDLER);
        Logger.getLogger("").removeHandler(handler);
    }

    public static void enableConsoleLogging() {        
        if (getHandler(Logger.getLogger(""), CONSOLE_HANDLER) == null) {
            final ConsoleHandler console = new ConsoleHandler();
            Logger.getLogger("").addHandler(console);
        }
    }

    public static void setLogLevel(String logLevel) {
        init();
        try {
            currentLogLevel = Level.parse(logLevel);
        } catch (IllegalArgumentException ex) {
            currentLogLevel = DEFAULT_LOG_LEVEL;
        }
        getRootCeltixLogger().setLevel(currentLogLevel);
    }

    public static String getLogLevel() {
        return currentLogLevel.toString();
    }

    public static Logger getRootCeltixLogger() {
        Logger rootCeltixLogger = null;
        rootCeltixLogger = LogManager.getLogManager().getLogger(getRootLoggerName());
        if (rootCeltixLogger == null) {
            rootCeltixLogger = Logger.getLogger(getRootLoggerName());
        }

        return rootCeltixLogger;
    }

    public static void init() {
        if (!initComplete) {
            initComplete = true;
            if (!loggerInitialisedOutsideConnector()) {
                enableConsoleLogging();
            }
        }
    }

    public static String getRootLoggerName() {
        return rootLoggerName;
    }

    public static void setRootLoggerName(String loggerName) {
        LoggerHelper.rootLoggerName = loggerName;
    }
}
