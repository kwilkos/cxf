package org.objectweb.celtix.routing;
//import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class StreamSourceMessageProvider implements Provider<StreamSource> {

    public StreamSourceMessageProvider() {
        //Complete
    }

    public StreamSource invoke(StreamSource request) {
        //StreamSource response = new StreamSource();
        return null;
    }
}
