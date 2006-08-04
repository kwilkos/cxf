package org.objectweb.celtix.common.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.BundleUtils;


/**
 * A container for static utility methods related to logging.
 */
public final class LogUtils {
    
    private static final Object[] NO_PARAMETERS = new Object[0];
    
    /**
     * Prevents instantiation.
     */
    private LogUtils() {
    }

    /**
     * Get a Logger with the associated default resource bundle for the class.
     *
     * @param cls the Class to contain the Logger
     * @return an appropriate Logger 
     */
    public static Logger getL7dLogger(Class<?> cls) {
        return Logger.getLogger(cls.getName(), BundleUtils.getBundleName(cls));
    }
    
    /**
     * Get a Logger with an associated resource bundle.
     *
     * @param cls the Class to contain the Logger
     * @param name the resource name
     * @return an appropriate Logger 
     */
    public static Logger getL7dLogger(Class<?> cls, String name) {
        return Logger.getLogger(cls.getName(), BundleUtils.getBundleName(cls, name));
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
            logger.log(level, formattedMessage, throwable);
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
            logger.log(level, formattedMessage, throwable);
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
            logger.log(level, formattedMessage);
        }
        
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
            logger.log(level, formattedMessage);
        }
        
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
