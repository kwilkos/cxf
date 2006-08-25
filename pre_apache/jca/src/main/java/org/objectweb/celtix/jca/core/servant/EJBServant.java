package org.objectweb.celtix.jca.core.servant;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

import org.objectweb.celtix.common.i18n.Message;

/**
 * This servant is used to dispatch invocations to an EJB.
 */
public class EJBServant extends AbstractServant {


    
    /**
     * Name of the default Initial Context properties file.
     */
    public static final String INITIAL_CONTEXT_PROPS_FILE = "initial_context.properties";
    private static final Logger LOG = Logger.getLogger(EJBServant.class.getName());
    /**
     * Properties for Initial Context.
     */
    protected Properties props;
    private EJBObject ejb;
    private String jndiLookup;
//     private ClassLoader ejbHomeClassLoader = null;

    /**
     * Constructor for creating an EJBServant.
     * @param wsdlLoc wsdl location.
     * @param bus Bus
     * @param jndiName JNDI name
     * @param initialContextProps initial context properties.
     */
    public EJBServant(String wsdlLoc, Bus bus, String jndiName, Properties initialContextProps) {
        super(wsdlLoc, bus);
        props = initialContextProps;
        jndiLookup = jndiName;
    }

    /**
     * Constructor for creating an EJBServant.
     * If you use this constructor, initial context properties can be provided
     * via properties file available in classpath. @see EJBServant#INITIAL_CONTEXT_PROPS_FILE
     * @param wsdlLoc wsdl location.
     * @param bus Bus.
     * @param jndiName JNDI Name.
     * @throws Exception if there is an error creating servant.
     */
    public EJBServant(String wsdlLoc, Bus bus, String jndiName) throws Exception {
        super(wsdlLoc, bus);
        props = getContextProperties(jndiName, INITIAL_CONTEXT_PROPS_FILE);
        jndiLookup = jndiName;
    }

    /**
     * Invokes on the operation of the target object with the arguments provided.
     * @param target Object to be invoked upon.
     * @param method Method to be invoked.
     * @param args Object[] arguments.
     * @return Object the result of dispatching the method. Void.TYPE for void methods.
     * @throws BusException if there is an errror invoking operation.
     */
    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
//         Object retval = null;

//         ClassLoader original = Thread.currentThread().getContextClassLoader();

//         try {
//             Thread.currentThread().setContextClassLoader(target.getClass().getClassLoader());

//             try {
//                 retval = method.invoke(target, args);

//                 if ((retval == null) && method.getReturnType().equals(Void.TYPE)) {
//                     retval = Void.TYPE;
//                 }
//             } catch (InvocationTargetException ite) {
//                 resetCacheTarget();
//                 throw new BusException(ite.getCause());
//             } catch (Exception ex) {
//                 resetCacheTarget();
//                 throw new BusException(ex);
//             }
//         } finally {
//             Thread.currentThread().setContextClassLoader(original);
//         }

//         return retval;
//     }
        Object result;
        Class[] argsClass;
        try {
            if (args != null) {
                argsClass = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argsClass[i] = args[i].getClass();
                }
            } else {
                argsClass = null;
            }
            Method tMethod = ejb.getClass().getMethod(method.getName(), argsClass);  

            //            result = tMethod.invoke(target, args);
            result = tMethod.invoke(ejb, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " 
                                       + e.getMessage());
        }
        return result;
    }


    synchronized void resetCacheTarget() {
        ejb = null;
    }

    public void setProperties(Properties p) {
        this.props = p;
    }

    /**
     * Based on the information from properties.xml, locate the remote object reference
     * of the target EJB.
     * @return Object EJBObject created by calling create function on EJBHome.
     * @throws BusException if there is an error getting Target EJB Object.
     */
    public synchronized Object getTargetObject() throws BusException {
        if (ejb == null) {
            try {

                Context ejbContext = getInitialContext(props);

                if (ejbContext == null) {
                    Message mg = new Message("Can't get InitialContext", LOG);
                    throw new BusException(mg);
                }

                EJBHome home = getEJBHome(ejbContext, jndiLookup);

//                 ejbHomeClassLoader = home.getClass().getClassLoader();

                Method createMethod = home.getClass().getMethod("create", new Class[0]);

                ejb = (EJBObject) createMethod.invoke(home, new Object[0]);
                
            } catch (NamingException e) {
                throw new BusException(e);
            } catch (NoSuchMethodException e) {
                throw new BusException(e);
            } catch (IllegalAccessException e) {
                throw new BusException(e);
            } catch (InvocationTargetException itex) {
                Throwable thrownException = itex.getTargetException();
                throw new BusException(thrownException);
            }
        }

        return ejb;
    }

    /**
     * Get properties for InitialContext. Looks for properties file in the
     * classpath with either <code>INITIAL_CONTEXT_PROPS_FILE</code> or
     * <code>jndi name + INITIAL_CONTEXT_PROPS_FILE</code> names.
     * @param jndiName JNDI Name
     * @param defaultPropsName name of the default properties file.
     * @return Properties properties for initial context.
     * @throws Exception if there is an error getting properties.
     */
    
    protected final Properties getContextProperties(String jndiName, String defaultPropsName)
        throws Exception {

        InputStream istream = getClass().getClassLoader().getResourceAsStream(jndiName
                                                                              + "_" + defaultPropsName);

        if (istream == null) {
            istream = getClass().getClassLoader().getResourceAsStream(defaultPropsName);
        }

        Properties properties = new Properties();

        if (istream != null) {
            properties.load(istream);
            istream.close();
        } else {
            LOG.fine("Properties " + defaultPropsName + " or " + jndiName
                     + defaultPropsName + "  not found in ClassPath");
        }

        return properties;

    }

    /**
     * Creates a new Initial Context with the specified properties.
     * @param props Properties for initial context.
     * @return Context initial context for EJB lookup.
     * @throws NamingException if there is an error getting initial context.
     */
    public Context getInitialContext(Properties propers) throws NamingException {
        return new InitialContext(propers);
    }

    /**
     * Looksup EJBHome in the context based on the JNDI name.
     * @param ejbContext Context to be used in lookup of EJB.
     * @param jndiName JNDI name
     * @return EJBHome
     * @throws NamingException if there is an error getting EJBHome.
     */
    protected EJBHome getEJBHome(Context ejbContext, String jndiName) throws NamingException {
        Object obj = ejbContext.lookup(jndiName);
        return (EJBHome) PortableRemoteObject.narrow(obj, EJBHome.class);
    }
//     public ClassLoader getEJBHomeClassLoader() {
//         return ejbHomeClassLoader;
//     }
}
