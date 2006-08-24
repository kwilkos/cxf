package org.apache.cxf.transports.http;

import java.io.OutputStream;

import org.mortbay.http.HttpResponse;

/**
 * EasyMock does not seem able to properly mock calls to HttpResponse -
 * expectations set seem to be ignored.
 */
class TestHttpResponse extends HttpResponse {
    private OutputStream os;
    private int[] callCounts = {0, 0, 0, 0, 0};
    
    TestHttpResponse(OutputStream o) {
        os = o;
    }
    
    public void commit() {
        callCounts[0]++;
    }
    
    int getCommitCallCount() {
        return callCounts[0];
    }
    
    public OutputStream getOutputStream() {
        callCounts[1]++;
        return os;
    }

    int getOutputStreamCallCount() {
        return callCounts[1];
    }
    
    public void sendRedirect(String url) {
        callCounts[2]++;
    }
    
    int getSendRedirectCallCount() {
        return callCounts[2];
    }
    
    public void setStatus(int s) {
        super.setStatus(s);
        callCounts[3]++;
    }
    
    int getStatusCallCount() {
        return callCounts[3];
    }
    
    public void addField(String name, String value) {
        super.addField(name, value);
        callCounts[4]++;
    }
    
    int getAddFieldCallCount() {
        return callCounts[4];
    }
}

