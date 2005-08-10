package org.objectweb.celtix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public abstract class Bus {
    public static final String BUS_ID = "org.objectweb.celtix.Bus";

    private static final String DEFAULT_BUS_CLASSNAME = "org.objectweb.celtix.internal.CeltixBus";

    /**
     * Initialize a bus
     */
    public static Bus init(Map<String, Object> properties) throws BusException {
        ClassLoader classLoader;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Exception ex) {
            throw new BusException(ex);
        }

        //First, see if the property is set
        String busID = (String)properties.get(BUS_ID);
        if (busID != null) {
            return newInstance(busID, classLoader, properties);
        }

        //Second, see if the system property is set
        String systemProp = System.getProperty(BUS_ID);
        if (systemProp != null) {
            return newInstance(systemProp, classLoader, properties);
        }

        //Next, check for the services stuff in the jar file
        String serviceId = "META-INF/services/" + BUS_ID;
        InputStream is = null;
        String factoryClassName = null;
        if (classLoader == null) {
            is = ClassLoader.getSystemResourceAsStream(serviceId);
        } else {
            is = classLoader.getResourceAsStream(serviceId);
        }
        if (is != null) {
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                factoryClassName = rd.readLine();
                rd.close();
            } catch (UnsupportedEncodingException useex) {
                //we're asking for UTF-8 which is supposed to always be supported,
                //but we'll throw a BusException anyway
                throw new BusException(useex);
            } catch (IOException useex) {
                throw new BusException(useex);
            }
        }
        if (factoryClassName != null && !"".equals(factoryClassName)) {
            return newInstance(factoryClassName, classLoader, properties);
        }

        //Last, just try the default.
        return newInstance(DEFAULT_BUS_CLASSNAME, classLoader, properties);
    }

    private static Bus newInstance(String className,
                                   ClassLoader classLoader,
                                   Map<String, Object> properties)
        throws BusException {

        Class<? extends Bus> busClass;
        try {
            if (classLoader == null) {
                busClass = Class.forName(className).asSubclass(Bus.class);
            } else {
                busClass = classLoader.loadClass(className).asSubclass(Bus.class);
            }
        } catch (ClassCastException ex) {
            throw new BusException(ex);
        } catch (ClassNotFoundException ex) {
            throw new BusException(ex);
        }


        try {
            Bus bus = busClass.newInstance();
            bus.initialize(properties);
            return bus;
        } catch (IllegalAccessException ex) {
            throw new BusException(ex);
        } catch (InstantiationException ex) {
            throw new BusException(ex);
        }
    }


    protected abstract void initialize(Map<String, Object> properties) throws BusException;



}
