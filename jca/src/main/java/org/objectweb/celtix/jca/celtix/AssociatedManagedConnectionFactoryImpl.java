package org.objectweb.celtix.jca.celtix;

import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;


import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;

public class AssociatedManagedConnectionFactoryImpl 
    extends ManagedConnectionFactoryImpl implements ResourceAdapterAssociation {

    private static final Logger LOG = 
        Logger.getLogger(AssociatedManagedConnectionFactoryImpl.class.getName());
    private ResourceAdapter ra;

    public AssociatedManagedConnectionFactoryImpl() {
        super();
        LOG.info("AssociatedManagedConnectionFactoryImpl constructed without props by appserver...");
    }

    public AssociatedManagedConnectionFactoryImpl(Properties props) {
        super(props);
        LOG.info("AssociatedManagedConnectionFactoryImpl constructed with props by appserver."
                 + " props = " + props);
    }

    public Object createConnectionFactory(ConnectionManager connMgr) throws ResourceException {
        Object connFactory = super.createConnectionFactory(connMgr);
        registerBus();
        return connFactory;
    }

    public void setResourceAdapter(ResourceAdapter aRA) throws ResourceException {
        LOG.info("Associate Resource Adapter with ManagedConnectionFactory by appserver. ra = " + ra);
        if (!(aRA instanceof ResourceAdapterImpl)) {
            throw new ResourceAdapterInternalException(
                "ResourceAdapter is not correct, it should be instance of ResourceAdapterImpl");
        }
        this.ra = aRA;
        mergeResourceAdapterProps();
    }

    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    protected void mergeResourceAdapterProps() {
        Properties raProps = ((ResourceAdapterImpl)ra).getPluginProps();
        Properties props = getPluginProps();
        Enumeration raPropsEnum = raProps.propertyNames();
        while (raPropsEnum.hasMoreElements()) {
            String key = (String)raPropsEnum.nextElement();
            if (!props.containsKey(key)) {
                setProperty(key, raProps.getProperty(key));
            } else {
                LOG.info("ManagedConnectionFactory's props already contains "
                            + key + ". No need to merge");
            }
        }
    }

    protected void registerBus() throws ResourceException {
        if (ra == null) {
            throw new ResourceAdapterInternalException("ResourceAdapter can not be null");
        }
        
        //TODO ((ResourceAdapterImpl)ra).registerBus(getBus());
    }

    protected Object getBootstrapContext() {
        return ((ResourceAdapterImpl)ra).getBootstrapContext();
    }
}





