package org.apache.cxf.binding.attachment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.cxf.message.Attachment;

public class AttachmentImpl implements Attachment {

    private DataHandler handler;
    private String id;
    private Map<String, String> headers = new HashMap<String, String>();
    private boolean xop;

    public AttachmentImpl(String idParam, DataHandler handlerParam) {
        this.id = idParam;
        this.handler = handlerParam;
    }

    public String getId() {
        return id;
    }

    public DataHandler getDataHandler() {
        return handler;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public Iterator<String> getHeaderNames() {
        return headers.keySet().iterator();
    }

    public boolean isXOP() {
        return xop;
    }

    public void setXOP(boolean xopParam) {
        this.xop = xopParam;
    }

}
