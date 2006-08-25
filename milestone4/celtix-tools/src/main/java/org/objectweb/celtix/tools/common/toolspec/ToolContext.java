package org.objectweb.celtix.tools.common.toolspec;

import java.io.*;
import org.w3c.dom.Document;

public interface ToolContext {

    /**
     * Request an input stream.
     * @param id the id of the stream in the streams sections of the tool's definition document.
     */
    InputStream getInputStream(String id) throws ToolException;

    /**
     * Request the standard input stream.
     */
    InputStream getInputStream() throws ToolException;

    /**
     * Request a document based on the input stream.
     * This is only returned if the mime type of incoming stream is xml.
     */
    Document getInputDocument(String id) throws ToolException;

    /**
     * Request a document based on the standard input stream.
     * This is only returned if the mime type of incoming stream is xml.
     */
    Document getInputDocument() throws ToolException;

    OutputStream getOutputStream(String id) throws ToolException;

    OutputStream getOutputStream() throws ToolException;

    String getParameter(String name) throws ToolException;

    String[] getParameters(String name) throws ToolException;

    boolean hasParameter(String name) throws ToolException;

    void sendDocument(String id, Document doc);

    void sendDocument(Document doc);

    void executePipeline();

    void setUserObject(String key, Object o);

    Object getUserObject(String key);

}
