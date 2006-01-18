package org.objectweb.celtix.bus.transports.jms;


import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.transports.jms.AddressType;


public final class JMSUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(JMSUtils.class);
    private static final char URL_PKG_PREFIXES_SEPARATOR = ':';


    private JMSUtils() {

    }

    public static Context getInitialContext(AddressType addrType) throws NamingException {
        Properties env = new Properties();
        populateContextEnvironment(addrType, env);

        // the initialContextFactory attribute can either specify the
        // JNDI initial context factory classname or a colon-separated
        // list of URL context factory package prefixes - we assume the
        // latter if the value includes a colon or does not identify a
        // loadable class
        //
        final String contextFactory = addrType.getInitialContextFactory();
        String contextProperty = Context.URL_PKG_PREFIXES;

        if (contextFactory.indexOf(URL_PKG_PREFIXES_SEPARATOR) == -1) {
            try {
                Class.forName(contextFactory);
                contextProperty = Context.INITIAL_CONTEXT_FACTORY;
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.FINE, "Context Factory class not found: ", cnfe);
            }
        }

        env.put(contextProperty, contextFactory);
        env.put(Context.PROVIDER_URL, addrType.getJndiProviderURL());

        Enumeration props = env.propertyNames();

        while (props.hasMoreElements()) {
            String name = (String) props.nextElement();
            String value = env.getProperty(name);
            LOG.log(Level.FINE, "Context property: " + name + " | " + value);
        }

        Context context = new InitialContext(env);

        return context;
    }


    protected static void populateContextEnvironment(AddressType addrType, Properties env) {
        String val = addrType.getJavaNamingApplet();

        if (val != null) {
            env.setProperty(Context.APPLET, val);
        }

        val = addrType.getJavaNamingAuthoritative();
        if (val != null) {
            env.setProperty(Context.AUTHORITATIVE, val);
        }

        val = addrType.getJavaNamingBatchsize();

        if (val != null) {
            env.setProperty(Context.BATCHSIZE, val);
        }

        val = addrType.getJavaNamingDnsUrl();

        if (val != null) {
            env.setProperty(Context.DNS_URL, val);
        }

        val = addrType.getJavaNamingLanguage();

        if (val != null) {
            env.setProperty(Context.LANGUAGE, val);
        }

        val = addrType.getJavaNamingFactoryObject();

        if (val != null) {
            env.setProperty(Context.OBJECT_FACTORIES, val);
        }

        val = addrType.getJavaNamingReferral();

        if (val != null) {
            env.setProperty(Context.REFERRAL, val);
        }

        val = addrType.getJavaNamingSecurityAuthentication();

        if (val != null) {
            env.setProperty(Context.SECURITY_AUTHENTICATION, val);
        }

        val = addrType.getJavaNamingSecurityCredentials();

        if (val != null) {
            env.setProperty(Context.SECURITY_CREDENTIALS, val);
        }

        val = addrType.getJavaNamingSecurityPrincipal();

        if (val != null) {
            env.setProperty(Context.SECURITY_PRINCIPAL, val);
        }

        val = addrType.getJavaNamingSecurityProtocol();

        if (val != null) {
            env.setProperty(Context.SECURITY_PROTOCOL, val);
        }

        val = addrType.getJavaNamingFactoryState();

        if (val != null) {
            env.setProperty(Context.STATE_FACTORIES, val);
        }

        val = addrType.getJavaNamingFactoryUrlPkgs();

        if (val != null) {
            env.setProperty(Context.URL_PKG_PREFIXES, val);
        }
    }
}
