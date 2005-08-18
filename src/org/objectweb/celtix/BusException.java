package org.objectweb.celtix;

import org.objectweb.celtix.common.i18n.Message;


public class BusException extends org.objectweb.celtix.common.i18n.Exception {

    private static final long serialVersionUID = 1L;
        
    public BusException(String msg) {
        super(msg);
    }
    public BusException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public BusException(Throwable cause) {
        super(cause);
    }
    /* (non-Javadoc)
     * @see org.objectweb.celtix.common.i18n.Exception#createMessage(java.lang.String, java.lang.Object...)
     */
    @Override
    protected Message createMessage(String code, Object... params) {
        return new BusMessage(code, params);
    } 
}
