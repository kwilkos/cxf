package org.objectweb.celtix.jca.core.servant;

import java.lang.reflect.Method;

import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.jca.celtix.BusFactory;

public class CeltixConnectEJBServant extends EJBServant {
    private static final Logger LOG = Logger.getLogger(CeltixConnectEJBServant.class.toString());

    final String loginModuleName = "";
    final String userName = "";
    final String password = "";
    InitialContext appserverInitialContext;
    BusFactory busFactory;

    public CeltixConnectEJBServant(BusFactory bf, String wsdlLoc, 
                                    String jndiName, Class sei) throws Exception {
        super(wsdlLoc, bf.getBus(), jndiName, null);
        appserverInitialContext = bf.getInitialContext();
        busFactory = bf;
        LOG.info("appserverClassLoader=" + bf.getAppserverClassLoader());
    }

    public BusFactory getBusFactory() {
        return busFactory;
    }

    public Context getInitialContext(Properties props) throws NamingException {
        //        return super.getInitialContext(props);
        return appserverInitialContext;
    }

    public void setInitialContext(InitialContext ic) {
        this.appserverInitialContext = ic;
    }
    
    public synchronized Object getTargetObject() throws BusException {
        
        Object retval = null;
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(busFactory.getAppserverClassLoader());
            retval = super.getTargetObject();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }

        LOG.info("target object=" + retval);
        return retval;
    }

    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
        return doInvoke(target, method, args);
    }

    protected Object doInvoke(Object target, Method method, Object[] args)
        throws Throwable {
        LOG.info("CeltixConnectEJBServant invoke() in thread: " + Thread.currentThread());
        Object retval = null;
        retval = super.invoke(target, method, args);
        return retval;
    }

}

