package org.apache.cxf.service.factory;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;

public class ServiceConstructionException extends UncheckedException {

    public ServiceConstructionException(Message msg, Throwable t) {
        super(msg, t);
    }

    public ServiceConstructionException(Message msg) {
        super(msg);
    }

    public ServiceConstructionException(Throwable cause) {
        super(cause);
    }

}
