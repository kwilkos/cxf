package org.objectweb.celtix.jbi.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;


public final class JBIMessageHelper { 
    
    private static final Logger LOG = Logger.getLogger(JBIMessageHelper.class.getName());
    
    private static final TransformerFactory TRANSFORMER_FACTORY 
        = TransformerFactory.newInstance();

    private JBIMessageHelper() { 
        // complete 
    }
    
    
    public static InputStream convertMessageToInputStream(Source src) 
        throws IOException, TransformerConfigurationException, TransformerException { 

        final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        StreamResult result = new StreamResult(baos);
        transformer.transform(src, result);
        LOG.finest("received message: " + new String(baos.toByteArray()));
        
        return new ByteArrayInputStream(baos.toByteArray());
    } 
}