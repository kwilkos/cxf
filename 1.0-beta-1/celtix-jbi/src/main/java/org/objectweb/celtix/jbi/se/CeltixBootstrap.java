package org.objectweb.celtix.jbi.se;


import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

/** Bootstraps the JBI component.  Does nothing for now but required
 * by Open-ESB.
 *
 */
public class CeltixBootstrap implements Bootstrap {

    private static final Logger LOG = Logger.getLogger(CeltixBootstrap.class.getName());

     // Implementation of javax.jbi.component.Bootstrap

    public final ObjectName getExtensionMBeanName() {
        return null;
    }

    public final void cleanUp() throws JBIException {
        LOG.fine("Bootstrap.cleanUp called");
       
    }

    public final void onInstall() throws JBIException {
        LOG.fine("Bootstrap.onInstall called");

    }

    public final void onUninstall() throws JBIException {
        LOG.fine("Bootstrap.onUninstall called");
    }

    public final void init(final InstallationContext argCtx) throws JBIException {
        LOG.fine("Bootstrap.init called");
    }
}
