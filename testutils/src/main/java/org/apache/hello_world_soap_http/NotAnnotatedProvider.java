package org.apache.hello_world_soap_http;


import javax.xml.transform.Source;
import javax.xml.ws.Provider;

public class NotAnnotatedProvider implements Provider<Source> {

    public NotAnnotatedProvider() {
        //Complete
    }

    public Source invoke(Source source) {
        return null;
    }
}
