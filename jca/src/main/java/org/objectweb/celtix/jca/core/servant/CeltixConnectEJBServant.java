package org.objectweb.celtix.jca.core.servant;

//import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// import java.security.PrivilegedActionException;
// import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
// import javax.security.auth.Subject;
// import javax.security.auth.callback.Callback;
// import javax.security.auth.callback.CallbackHandler;
// import javax.security.auth.callback.NameCallback;
// import javax.security.auth.callback.PasswordCallback;
// import javax.security.auth.callback.UnsupportedCallbackException;
// import javax.security.auth.login.LoginContext;
// import javax.security.auth.login.LoginException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.jca.celtix.BusFactory;

// import com.iona.jbus.ContextConstants;
// import com.iona.jbus.ContextException;
// import com.iona.jbus.DispatchLocals;
// import com.iona.jbus.IonaMessageContext;
//import com.iona.jbus.servants.EJBServant;

//import com.iona.schemas.bus.security_context.BusSecurity;

public class CeltixConnectEJBServant extends EJBServant {
    private static final Logger LOG = Logger.getLogger(CeltixConnectEJBServant.class.toString());

//     private static final String JBOSS_SUBJECT = "javax.security.auth.Subject";
//     private static final String WEBLOGIC_SUBJECT = "weblogic.security.Security";
//     private static final String WEBSPHERE_SUBJECT = "com.ibm.websphere.security.auth.WSSubject";
//     private static final String RUN_AS_METHOD_NAME = "runAs";
//     private static final String DO_AS_METHOD_NAME = "doAs";

//     private static Class appServerSubjectClass;
//     private static String doAsMethodName;

    final String loginModuleName = "";
    final String userName = "";
    final String password = "";
    InitialContext appserverInitialContext;
    BusFactory busFactory;

    // deal with appserver specifics around proprietary JAAS/Appserver integration
    /*
    static {
        doAsMethodName = DO_AS_METHOD_NAME;
        try {
            appServerSubjectClass = Class.forName(WEBSPHERE_SUBJECT);
        } catch (ClassNotFoundException okNotWS) {
            try {
                appServerSubjectClass = Class.forName(WEBLOGIC_SUBJECT);
                doAsMethodName = RUN_AS_METHOD_NAME;
            } catch (ClassNotFoundException okNowWlSoDefault) {
                appServerSubjectClass = Subject.class;
            }
        }
        LOG.info("AppServer Subject class=" + appServerSubjectClass.getName());
    }
    */
    public CeltixConnectEJBServant(BusFactory bf, String wsdlLoc, String jndiName) throws Exception {
        super(wsdlLoc, bf.getBus(), jndiName, null);
//         loginModuleName = bf.getJAASLoginConfigName();
//         userName = bf.getJAASLoginUserName();
//         password = bf.getJAASLoginPassword();
        appserverInitialContext = bf.getInitialContext();
        busFactory = bf;
        LOG.info("appserverClassLoader=" + bf.getAppserverClassLoader());
    }

    public BusFactory getBusFactory() {
        return busFactory;
    }

    public Context getInitialContext(Properties props) throws NamingException {
        return appserverInitialContext;
    }

    public void setInitialContext(InitialContext ic) {
        this.appserverInitialContext = ic;
    }
    
    public synchronized Object getTargetObject() throws BusException {
        
        Object retval = null;
//        if (isInsecure()) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(busFactory.getAppserverClassLoader());
            retval = super.getTargetObject();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
//        } else {
            // busSecurity is not available at this stage as arguments have not been read
            // of the wire by artix, the read is driven by the argument types as defined
            // by the target object method parameters
            //
//             retval = doRunAsWithAction(busFactory.getAppserverClassLoader(),
//                                        null, new GetTargetObjectPrivilegedAction()); 
//        }
        LOG.info("target object=" + retval);
        return retval;
    }

    public Object invoke(Object target, Method method, Object[] args) throws BusException {
//         return doInvoke(target, method, args, getBusSecurity());
        return doInvoke(target, method, args);
    }

//     protected Object doInvoke(Object target, Method method, Object[] args, BusSecurity busSecurity)
//         throws BusException {
    protected Object doInvoke(Object target, Method method, Object[] args)
        throws BusException {
        LOG.info("CeltixConnectEJBServant invoke() in thread: " + Thread.currentThread());
        Object retval = null;
//         if (isInsecure()) {
        retval = super.invoke(target, method, args);
//         } else {
//             retval = doRunAsWithAction(target.getClass().getClassLoader(), busSecurity, 
//                               new InvokePrivilegedAction(target, method, args)); 
//         }
        return retval;
    }

    /*
    private Object doRunAsWithAction(ClassLoader loader, BusSecurity busSecurity,
                                     PrivilegedExceptionAction action) throws BusException {
        LoginContext lc = null;
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            lc = getLoginContext(getLoginModuleName(), busSecurity);
            LOG.debug("login context=" + lc);
            return doAs(login(lc), action);
        } finally {
            logout(lc);
            Thread.currentThread().setContextClassLoader(original);
        }
    } 

    
    protected boolean isInsecure() {
        return (userName == null);
    }
    
    
    protected boolean isValidBusSecurityContext(BusSecurity busSecurity) { 
        return busSecurity != null && 
            null != busSecurity.getWSSEUsernameToken() && !"".equals(busSecurity.getWSSEUsernameToken()) && 
            null != busSecurity.getWSSEPasswordToken() && !"".equals(busSecurity.getWSSEPasswordToken());
    } 


    protected void logout(LoginContext lc) {
        if (lc != null) {
            try {
                lc.logout();
            } catch (LoginException le) {
                LOG.info("Logout failed, reason: " + le);
            }
        }
    }

    protected Subject login(LoginContext lc) throws BusException {
        try {
            lc.login();
            return lc.getSubject();
        } catch (LoginException le) {
            String exstr = "Authentication failed with LoginContext = " + lc + ", reason: " + le;
            LOG.info(exstr, le);
            BusException be = new BusException(exstr);
            be.initCause(le);
            throw be;
        }
    }

    protected LoginContext getLoginContext(String moduleName, BusSecurity busSecurity) throws BusException {
        LoginContext lc = null;
        if (busSecurity == null) {
            lc = getLoginContext(moduleName, userName, password);
        } else {
            lc = getLoginContext(moduleName, busSecurity.getWSSEUsernameToken(),
            busSecurity.getWSSEPasswordToken());
        }
        return lc;
    }
    
    protected LoginContext getLoginContext(String moduleName, String user, String pass) throws BusException {
        LOG.info("getLoginContext: username=" + user + ", password=" + ((pass == null) ? null : "????"));
        if (user == null || pass == null) {
            throw new BusException("Failed to obtain a LoginContext,
            one of the supplied username or password is null");
        }

        try {
            return new LoginContext(moduleName, new RarCallbackHandler(user, pass));
        } catch (LoginException le) {
            String exstr = "Failed to create JAAS LoginContext using name = " +
                  moduleName + ", reason: " + le;
            LOG.info(exstr, le);
            BusException be = new BusException(exstr);
            be.initCause(le);
            throw be;
        }
    }

    
    BusSecurity getBusSecurity() {
        BusSecurity retVal = null;

        IonaMessageContext mc = (IonaMessageContext) DispatchLocals.getCurrentMessageContext();
        LOG.debug("MessageContext is: " + mc);

        if (mc != null) {
            try {
                BusSecurity busSecurity = (BusSecurity) mc.getRequestContext(ContextConstants.
                                                                             SECURITY_SERVER_CONTEXT);
                if (isValidBusSecurityContext(busSecurity)) {
                    retVal = busSecurity;
                } else {
                    LOG.debug("Security context is not valid, value=" + busSecurity);
                }
            } catch(ContextException ce) {
                LOG.debug("Failed to access BusSecurity, unexpected exception: " + ce.toString(), ce);
            }
        }

        LOG.debug("getBusSecurity() return: " + retVal);
        return retVal;
    }
    
    protected Object doAs(Subject subject, PrivilegedExceptionAction action) throws BusException {
        try {
            Method doAsMethod = appServerSubjectClass.getMethod(doAsMethodName, new Class[]{Subject.class,
                                                                PrivilegedExceptionAction.class});
            return doAsMethod.invoke(null, new Object[]{subject, action});
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause instanceof PrivilegedActionException) {
                    throw (BusException)cause.getCause();
                } else {
                    BusException be = new BusException("Failed to invoke JAAS " +
                              doAsMethodName + "on class:" + appServerSubjectClass + ", reason: " + cause);
                    be.initCause(cause); 
                    LOG.error(be.toString(), be);
                    throw be;
                }
            } else {
                BusException be = new BusException("Failed to invoke JAAS doAs, reason: " + e);
                be.initCause(e);
                LOG.error(be.toString(), be);
                throw be;
            }
        }
    }


    
    protected String getLoginModuleName() {
        return loginModuleName;
    }
    

    class GetTargetObjectPrivilegedAction implements PrivilegedExceptionAction {
        public Object run() throws Exception {
            return ArtixConnectEJBServant.super.getTargetObject();
        }
    }

    class InvokePrivilegedAction implements PrivilegedExceptionAction {
        private Object target;
        private Method method;
        private Object[] args;

        public InvokePrivilegedAction(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }

        public Object run() throws Exception {
            return ArtixConnectEJBServant.super.invoke(target, method, args);
        }
    }

    class RarCallbackHandler implements CallbackHandler {
        private String usernameToken;
        private String passwordToken;
        
        public RarCallbackHandler(String username, String password) {
            usernameToken = username;
            passwordToken = password;
        }

        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for (int i=0; i<callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback nc = (NameCallback)callbacks[i];
                    nc.setName(usernameToken);
                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback)callbacks[i];
                    pc.setPassword(passwordToken.toCharArray());
                } else {
                    // we don't know what will be passed to us in advance
                    // if we don't fill some required field then the appserver 
                    // will complain if needed. We just do what we can!
                    //
                    LOG.info("Ignoring unrecognised callback type: " + callbacks[i]);
                }
            }
        }
    }
    */
}

