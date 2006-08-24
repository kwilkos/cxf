package org.apache.cxf.common.util;

import org.apache.cxf.common.i18n.Exception;
import org.apache.cxf.common.i18n.Message;

public class Base64Exception extends Exception {

    public Base64Exception(Message msg) {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    public Base64Exception(Message msg, Throwable t) {
        super(msg, t);
        // TODO Auto-generated constructor stub
    }

    public Base64Exception(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }


}
