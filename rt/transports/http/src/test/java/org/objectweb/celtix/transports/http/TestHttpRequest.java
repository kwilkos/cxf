package org.apache.cxf.transports.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.http.HttpRequest;
import org.mortbay.util.MultiMap;

/**
 * EasyMock does not seem able to properly mock calls to HttpRequest -
 * expectations set seem to be ignored.
 */
class TestHttpRequest extends HttpRequest {
    private String method;
    private InputStream is;
    private String path;
    private String query;
    private int[] callCounts = {0, 0, 0, 0, 0, 0, 0, 0};
    private Map<String, List<String>> fields;
    private MultiMap parameters;
    
    TestHttpRequest(String m, InputStream i, String p, String q) {
        this(m, i, p, q, null);
    }
    
    TestHttpRequest(InputStream i, MultiMap params) {
        this("POST", i, null, null, params);
    }
    
    TestHttpRequest(String m, InputStream i, String p, String q, MultiMap params) {
        method = m;
        is = i;
        path = p;
        query = q;
        parameters = params;
        fields = new HashMap<String, List<String>>();
        List<String> contentTypes = new ArrayList<String>();
        contentTypes.add("text/xml");
        contentTypes.add("charset=utf8");
        fields.put("content-type", contentTypes);
        List<String> auth = new ArrayList<String>();
        auth.add(JettyHTTPDestinationTest.BASIC_AUTH);
        fields.put(JettyHTTPDestinationTest.AUTH_HEADER, auth);
    }
    
    public String getMethod() {
        callCounts[0]++;
        return method;
    }
    
    int getMethodCallCount() {
        return callCounts[0];
    }
    
    public InputStream getInputStream() {
        callCounts[1]++;
        return is;
    }

    int getInputStreamCallCount() {
        return callCounts[1];
    }

    public String getPath() {
        callCounts[2]++;
        return path;
    }
    
    int getPathCallCount() {
        return callCounts[2];
    }
    
    public String getQuery() {
        callCounts[3]++;
        return query;
    }
    
    int getQueryCallCount() {
        return callCounts[3];
    }
    
    public void setHandled(boolean h) {
        callCounts[4]++;
    }
    
    int getHandledCallCount() {
        return callCounts[4];
    }
    
    public Enumeration getFieldNames() {
        callCounts[5]++;
        return Collections.enumeration(fields.keySet());
    }
    
    int getFieldNamesCallCount() {
        return callCounts[5];
    }
    
    public Enumeration getFieldValues(String f) {
        callCounts[6]++;
        return Collections.enumeration(fields.get(f));            
    }
    
    int getFieldValuesCallCount() {
        return callCounts[6];
    }
    
    public MultiMap getParameters() {
        callCounts[7]++;
        return parameters;
    }
    
    int getParametersCallCount() {
        return callCounts[7];
    }
}
