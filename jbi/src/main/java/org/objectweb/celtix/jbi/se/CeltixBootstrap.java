package org.objectweb.celtix.jbi.se;


import java.util.logging.Logger;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;

/** Bootstraps the JBI component.  Does nothing for now but required
 * by Open-ESB.
 *
 */
public class CeltixBootstrap implements Bootstrap {

    private static final Logger LOG = LogUtils.getL7dLogger(CeltixBootstrap.class);

     // Implementation of javax.jbi.component.Bootstrap

    public final ObjectName getExtensionMBeanName() {
        return null;
    }

    public final void cleanUp() throws JBIException {
        LOG.fine(new Message("BOOTSTRAP.CLEANUP", LOG).toString());
       
    }

    public final void onInstall() throws JBIException {
        LOG.fine(new Message("BOOTSTRAP.ONINSTALL", LOG).toString());

    }

    public final void onUninstall() throws JBIException {
        LOG.fine(new Message("BOOTSTRAP.ONUNINSTALL", LOG).toString());
    }

    public final void init(final InstallationContext argCtx) throws JBIException {
        LOG.fine(new Message("BOOTSTRAP.INIT", LOG).toString());
    }
}
