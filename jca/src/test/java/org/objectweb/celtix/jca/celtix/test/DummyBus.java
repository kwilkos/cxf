package org.objectweb.celtix.jca.celtix.test;


import java.util.Map;
import java.util.ResourceBundle;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventCache;
import org.objectweb.celtix.BusEventFilter;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.jaxws.EndpointRegistry;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.wsdl.WSDLManager;



public class DummyBus extends Bus {    
    // for initialise behaviours
    static int initializeCount;
    static int shutdownCount;
    static boolean correctThreadContextClassLoader;
    static boolean throwException;
  
   
    static String[] invokeArgs;
    static String celtixHome = "File:/local/temp";
    
    
    public static void reset() {
        initializeCount = 0;
        shutdownCount = 0; 
        correctThreadContextClassLoader = false;
        throwException = false;
    }
    
    
    public static Bus init(String[] args) throws BusException {
        
        initializeCount++;
        correctThreadContextClassLoader = 
            Thread.currentThread().getContextClassLoader() 
                == org.objectweb.celtix.jca.celtix.ManagedConnectionFactoryImpl.class.getClassLoader();
        
        if (throwException) {
            throw new BusException(new Message("tested bus exception!", 
                                               (ResourceBundle)null, new Object[]{}));
        }
        return null;
        
    }

    
    public void shutdown(boolean wait) throws BusException {
        shutdownCount++; 
        
    }


    @Override
    public void sendEvent(BusEvent event) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void addListener(BusEventListener l, BusEventFilter filter) throws BusException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void removeListener(BusEventListener l) throws BusException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public BusEventCache getEventCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigurationBuilder getConfigurationBuilder() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Configuration getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public TransportFactoryManager getTransportFactoryManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public BindingManager getBindingManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public WSDLManager getWSDLManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public PluginManager getPluginManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public BusLifeCycleManager getLifeCycleManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public WorkQueueManager getWorkQueueManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public ResourceManager getResourceManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public InstrumentationManager getInstrumentationManager() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getBusID() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public EndpointRegistry getEndpointRegistry() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void initialize(String[] args, Map<String, Object> properties) throws BusException {
        // TODO Auto-generated method stub
        
    }


    public static String getCeltixHome() {
        return celtixHome;
    }


    public static void setCeltixHome(String home) {
        DummyBus.celtixHome = home;
    }


    public static boolean isCorrectThreadContextClassLoader() {
        return correctThreadContextClassLoader;
    }


    public static void setCorrectThreadContextClassLoader(boolean correct) {
        DummyBus.correctThreadContextClassLoader = correct;
    }


    public static int getInitializeCount() {
        return initializeCount;
    }


    public static void setInitializeCount(int count) {
        DummyBus.initializeCount = count;
    }


    public static String[] getInvokeArgs() {
        return invokeArgs;
    }


    public static void setInvokeArgs(String[] args) {
        DummyBus.invokeArgs = args;
    }


    public static int getShutdownCount() {
        return shutdownCount;
    }


    public static void setShutdownCount(int count) {
        DummyBus.shutdownCount = count;
    }


    public static void setThrowException(boolean fthrow) {
        DummyBus.throwException = fthrow;
    } 

}
