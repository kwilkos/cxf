package org.objectweb.celtix.systest.rest;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.objectweb.celtix.testutil.common.AbstractTestServerBase;

@WebServiceProvider
@ServiceMode(value = Service.Mode.PAYLOAD)            
@BindingType("http://celtix.objectweb.org/bindings/xmlformat")
public class RestProvider extends AbstractTestServerBase implements Provider<Source> {
    
    public static final String PUBLISH_ADDRESS = "http://localhost:9876/systest/rest";
    
    private  final DocumentBuilder builder; 
    
    @Resource private WebServiceContext wsContext; 
    
    public RestProvider() throws Exception {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @PostConstruct public void init() throws Exception {
    }
    
    
    public Source invoke(Source source) {
        
        MessageContext ctx = wsContext.getMessageContext();
        
        try {
            if ("GET".equals(ctx.get(MessageContext.HTTP_REQUEST_METHOD)))  {
                return buildContextDetails(ctx);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return source;
    }
    
    
    private Source buildContextDetails(MessageContext ctx) { 
        
        String reqMethod = (String)ctx.get(MessageContext.HTTP_REQUEST_METHOD); 
        String pathInfo = (String)ctx.get(MessageContext.PATH_INFO);
        String queryString = (String)ctx.get(MessageContext.QUERY_STRING);
        
        Document doc = builder.newDocument();
        Element detailsRoot = doc.createElement("context-details");
        doc.appendChild(detailsRoot);
        detailsRoot.appendChild(createElementWithText(doc, "request-method", reqMethod));
        detailsRoot.appendChild(createElementWithText(doc, "path-info", pathInfo));
        detailsRoot.appendChild(createElementWithText(doc, "query-string", queryString));
        return new DOMSource(doc.getFirstChild());
    }
    
    private Element createElementWithText(Document doc, String name, String content) {
        Element el = doc.createElement(name);
        el.setTextContent(content);
        return el;
    }
    
    
    @Override
    protected void run() {
        try {
            RestProvider rp = new RestProvider();
            Endpoint.publish(PUBLISH_ADDRESS, rp);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        RestProvider rp = new RestProvider();
        rp.init();
        System.out.println("calling RestProvider.run");
        Method m = rp.getClass().getMethod("start");
        System.out.println(m);
        rp.start();
        
        
    }
     
}
