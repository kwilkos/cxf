

package demo.servlet;

import java.io.PrintWriter;
import java.net.URL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.objectweb.celtix.connector.CeltixConnectionFactory;
import org.objectweb.celtix.connector.Connection;

public class HelloWorldServlet extends DemoServletBase {

    private static final String EIS_JNDI_NAME = "java:comp/env/eis/CeltixConnector";

    private String operationName; 
    private String userName; 

    public HelloWorldServlet() {
        super("J2EE Connector Hello World Demo");
    }


    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try { 
            operationName = req.getParameter("Operation"); 
            userName = req.getParameter("User"); 
            super.doGet(req, resp);
        } finally { 
            operationName = null; 
        } 
    }



    /** 
     * get a connection to the SOAP service
     */ 
    protected Connection getServiceConnection() throws NamingException, ResourceException {

        Context ctx = new InitialContext();

        // retrieve the connection factory from JNDI 
        //
        CeltixConnectionFactory factory = (CeltixConnectionFactory)ctx.lookup(EIS_JNDI_NAME);
            
        URL wsdlLocation = getClass().getResource("/hello_world.wsdl");
        QName serviceName = 
            new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        QName portName = new QName("", "SoapPort");


        // create the connection 
        //
        return (Connection)factory.getConnection(Greeter.class, wsdlLocation, serviceName, portName);
    }



    protected void writeMainBody(PrintWriter writer) {
        operationForm(writer);
        writer.println("&nbsp;&nbsp;" + getResponseFromWebService());
    }


    protected void operationForm(PrintWriter writer) {

        writer.println("<TABLE ALIGN=\"center\">");
        writer.println("<FORM ACTION=\"./*.do\" METHOD=GET>");
        writer.println("<TR><TD><b>Operation:</b><BR>");
        writer.println("<TR><TD>");   
        writer.println("<INPUT TYPE=RADIO NAME=\"Operation\" VALUE=\"sayHi\"" 
            + ("sayHi".equals(operationName) ? " CHECKED " : "") +  "> sayHi<BR>");
        writer.println("<TR><TD>");
        writer.println("<INPUT TYPE=RADIO NAME=\"Operation\" VALUE=\"greetMe\""
            + ("greetMe".equals(operationName) ? "  CHECKED " : "") + "> greetMe ");
        writer.println("<INPUT TYPE=TEXT NAME=\"User\" VALUE=\""
            + ("greetMe".equals(operationName) ? userName : "") + "\" SIZE=20><BR>");
        writer.println("<TR><TD>");
        writer.println("<INPUT TYPE=SUBMIT VALUE=\"Submit\"><BR></p>");
        writer.println("</FORM>");
        writer.println("</TABLE>");
    }


    String getResponseFromWebService() {

        Greeter greeter = null;
        String responseFromWebService = "No message sent";
        
        try {
            greeter = (Greeter)getServiceConnection();

            if (operationName != null) {

                if ("sayHi".equals(operationName)) {
                    responseFromWebService = greeter.sayHi();
                } else if ("greetMe".equals(operationName)) {
                    responseFromWebService = greeter.greetMe(userName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause() != null) { 
                e.getCause().printStackTrace(); 
            } 
            responseFromWebService = e.toString();
        } finally { 
            try {
                if (greeter != null) {
                    ((Connection)greeter).close();
                }
            } catch (ResourceException e) {
                // report error from close
            }
        } 
        return responseFromWebService; 
    }
}
