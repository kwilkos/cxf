


package demo.servlet;

import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/** Abstract base class for demo servlets.  Provides a consistent
 *  look&feel for the servlet based demos. Subclasses are only
 *  required to display the portions of the page relevant to their
 *  demo, minimizing the amount of clutter in the demo code.
 *
 * The servlet is based on the template method pattern. Subclasses
 * are required to implement the <code>writeMainBody</code> method.
 * the basic structure of the page is already laid out when this
 * method is invoke so there is no need to for &lt;body&gt; or
 * &lt;head&gt; type tags.
 */ 
public abstract class DemoServletBase extends HttpServlet {

    final String pageTitle; 

    public DemoServletBase(String pageTitle) {
    
        this.pageTitle = pageTitle; 
    }
  


    public void  doGet(HttpServletRequest req, HttpServletResponse resp) { 

        PrintWriter writer = null; 
    
        try { 

            resp.setContentType("text/html");
            writer = resp.getWriter();  
            writer.println("<html>"); 

            // write out the page header
            
            writeHeader(writer);

            // write out the body including displaying 
            
            writeBody(writer); 

      
            writer.println("</html>"); 
      
            writer.close(); 

        } catch (Exception ex) { 
            ex.printStackTrace(); 

        } finally { 
            if (writer != null) { 
                writer.close(); 
            } 
      
        } 
    
    } 


    protected void writeBody(PrintWriter writer) { 
    
        writer.println("<body  topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\" margins=\"0\">"); 
        writePageHeader(writer); 
    
    
        writer.println("<h2><br>&nbsp;&nbsp;" + pageTitle + "</h2>"); 
        writeMainBody(writer); 
    
        writer.println("</td>"); 
        writer.println("</tr>"); 
        writer.println("</table>"); 
        writer.println("</body>"); 
        writer.println("</html>"); 
        writer.close(); 
    } 


    protected void writePageHeader(PrintWriter writer) { 

        writer.println(
            "<table width=\"100%\"  height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<TR><td width=\"9%\" height=\"1%\"></td><td width=\"1%\">"
            + "<img src=\"images/spacer.gif\" width=\"2\">"
            + "</td><td width=\"90%\">"
            + "<img src=\"images/celtix_banner.gif\" width=\"350\" height=\"57\"></td></tr>"); 

        writer.println(
            "<TR><td id=\"grey\"><img src=\"images/spacer.gif\" width=\"185\" height=\"1\">"
            + "</td><td id=\"grey\"><img src=\"images/spacer.gif\" width=\"2\" height=\"2\">"
            + "</td><td id=\"grey\"><img src=\"images/spacer.gif\" width=\"300\" height=\"2\"></td>");

        writer.println(
            "</tr><TR><td id=\"grey\"><img src=\"images/spacer.gif\" width=\"1\" height=\"1\">"
            + "</td><td id=\"grey\"><img src=\"images/spacer.gif\" width=\"2\" height=\"1\">" 
            + "</td><td id=\"blue\">"); 

    } 


    protected void writeHeader(PrintWriter writer) { 

        writer.println("<head>"); 
        writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./celtix_doc.css\"/>");
        writer.println("<title>Celtix J2EE  Demos</title>"); 
        writer.println("</head>"); 
    } 

    protected abstract void writeMainBody(PrintWriter writer); 

}
