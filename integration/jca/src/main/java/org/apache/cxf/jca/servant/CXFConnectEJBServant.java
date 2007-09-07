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
package org.apache.cxf.jca.servant;

import java.lang.reflect.Method;

import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.BusException;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jca.cxf.JCABusFactory;

public class CXFConnectEJBServant extends EJBServant {
    private static final Logger LOG = LogUtils.getL7dLogger(CXFConnectEJBServant.class);

    final String loginModuleName = "";
    final String userName = "";
    final String password = "";
    InitialContext appserverInitialContext;
    JCABusFactory jcaBusFactory;

    public CXFConnectEJBServant(JCABusFactory bf, String wsdlLoc, 
                                    String jndiName, Class sei) throws Exception {
        super(wsdlLoc, bf.getBus(), jndiName, null);
        appserverInitialContext = bf.getInitialContext();
        jcaBusFactory = bf;
        LOG.info("appserverClassLoader=" + bf.getAppserverClassLoader());
    }

    public JCABusFactory getBusFactory() {
        return jcaBusFactory;
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
            Thread.currentThread().setContextClassLoader(jcaBusFactory.getAppserverClassLoader());
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
        LOG.info("CXFConnectEJBServant invoke() in thread: " + Thread.currentThread());
        Object retval = null;
        retval = super.invoke(target, method, args);
        return retval;
    }

}

