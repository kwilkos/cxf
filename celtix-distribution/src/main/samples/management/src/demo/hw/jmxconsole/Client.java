package demo.hw.jmxconsole;

import java.util.Iterator;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public final class Client {

    private static MBeanServerConnection mbsc;

    private Client() {
        
    }    
    public static void main(String[] args) {
        try {

            JMXServiceURL url = new JMXServiceURL(
                      "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/server");
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            mbsc = jmxc.getMBeanServerConnection();

            String domain = "org.objectweb.celtix.instrumentation";

            echo("\n The Celtix Management Demo MBeanServer has " + mbsc.getMBeanCount() + " MBeans");
            echo("\n There are :");
            
            Set names = mbsc.queryNames(null, null);
            for (Iterator i = names.iterator(); i.hasNext();) {
                ObjectName on = (ObjectName)i.next();
                echo("\n\t get ObjectName = " + on + "\n\t Its Attributes are :");
                getInstrumentationAttributes(on);
            }

            ObjectName serviceMBeanName = new ObjectName(
                domain + ":type=ServerMBean,Bus=celtix,name=ServerMBean");
            echo("\n >>> get the ServiceMBean infor <<<");

            echo("\nServiceMBean.ServiceName = " + mbsc.getAttribute(serviceMBeanName, "ServiceName"));
            echo("\nServiceMBean.Address = " + mbsc.getAttribute(serviceMBeanName, "Address"));

            ObjectName greeterInstrumentation = new ObjectName(
                domain + ":type=GreeterInstrumentation,Bus=celtix,name=Demo.Management");
            
            echo("\n >>> get the GreeterInstrumentation counter infor <<<");

            getInstrumentationAttributes(greeterInstrumentation);

             
            mbsc.setAttribute(greeterInstrumentation, 
                              new Attribute("PingMeCounter", new Integer("20")));
            echo("\n >>> set the GreeterInstrumentation PingMeCounter to be 20 <<<");
            echo("\n >>> get the GreeterInstrumentation counter infor <<<");

            getInstrumentationAttributes(greeterInstrumentation);
                              
            Object[] params = new Object[1];
            params[0] = "JMXConsoleName";
            String[] signature = new String[1];
            signature[0] = "java.lang.String";
            mbsc.invoke(greeterInstrumentation,
                        "setSayHiReturnName", params, signature);
            echo("\n >>> invoke the GreeterInstrumentation setSayHiReturnName method");
            echo("\n JMXConsole runs Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getInstrumentationAttributes(ObjectName name) throws Exception {

        MBeanInfo info = mbsc.getMBeanInfo(name);
        MBeanAttributeInfo[] attrs = info.getAttributes();
        if (attrs == null) {
            return;
        }
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isReadable()) {
                try {
                    Object o = mbsc.getAttribute(name, attrs[i].getName());
                    echo("\n\t\t" + attrs[i].getName() + " = " + o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void echo(String msg) {
        System.out.print(msg);
    }

    
}
