package org.objectweb.celtix.ws.addressing;


import java.util.logging.Level;
import java.util.logging.Logger;


import org.objectweb.celtix.common.logging.LogUtils;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.DEFAULT_ADDRESSING_BUILDER;


/**
 * Factory for WS-Addressing elements.
 * <p>
 * Note that the JAXB generated types are used directly to represent 
 * WS-Addressing schema types. Hence there are no factory methods defined
 * on this class for those types, as they may be instanted in the normal
 * way via the JAXB generated ObjectFactory.
 */
public abstract class AddressingBuilder implements AddressingType {

    private static final Logger LOG = 
        LogUtils.getL7dLogger(AddressingBuilder.class);
    private static AddressingBuilder builder;

    /**
     * Non-public constructor prevents instantiation.
     */
    protected AddressingBuilder() {
    }

    /**
     * AddressingBuilder factory method.
     *
     * @return AddressingBuilder instance 
     */
    public static AddressingBuilder getAddressingBuilder() { 
        synchronized (AddressingBuilder.class) {
            if (builder == null) {
                String className = DEFAULT_ADDRESSING_BUILDER;
                try {
                    Class cls = Class.forName(className);
                    builder = (AddressingBuilder)cls.newInstance();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                    LogUtils.log(LOG,
                                 Level.WARNING, 
                                 "BUILDER_CLASS_NOT_FOUND_MSG", 
                                 cnfe,
                                 className);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LogUtils.log(LOG,
                                 Level.WARNING,
                                 "BUILDER_INSTANTIATION_FAILED_MSG",
                                 ex,
                                 className);
                }
            }
        }
        return builder;
    }
    
    /**
     * AddressingProperties factory method.
     *  
     * @return a new AddressingProperties instance
     */
    public abstract AddressingProperties newAddressingProperties();
    
    /**
     * Returns an instance of
     * <code>javax.ws.addressing.AddressingConstants</code>
     * <p>
     * <b>Note</b>: This is a new method since Early Draft 1.
     * 
     * @return The new AddressingConstants.
     */
    public abstract AddressingConstants newAddressingConstants();
}
