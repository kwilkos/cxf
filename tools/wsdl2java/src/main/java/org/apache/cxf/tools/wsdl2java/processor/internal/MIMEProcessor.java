package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaType;
import org.apache.cxf.tools.util.ProcessorUtil;

public class MIMEProcessor extends AbstractProcessor {

    public MIMEProcessor(ProcessorEnvironment penv) {
        super(penv);
    }

    private static String getJavaTypeForMimeType(MIMEPart mPart) {
        if (mPart.getExtensibilityElements().size() > 1) {
            return "javax.activation.DataHandler";
        } else {
            ExtensibilityElement extElement = (ExtensibilityElement)mPart.getExtensibilityElements().get(0);
            if (extElement instanceof MIMEContent) {
                MIMEContent mimeContent = (MIMEContent)extElement;
                if ("image/jpeg".equals(mimeContent.getType()) || "image/gif".equals(mimeContent.getType())) {
                    return "java.awt.Image";
                } else if ("text/xml".equals(mimeContent.getType())
                           || "application/xml".equals(mimeContent.getType())) {
                    return "javax.xml.transform.Source";
                }
            }
        }
        return "javax.activation.DataHandler";
    }

    public void process(JavaMethod jm, MIMEMultipartRelated ext, JavaType.Style style) throws ToolException {
        List mimeParts = ext.getMIMEParts();
        Iterator itParts = mimeParts.iterator();
        while (itParts.hasNext()) {
            MIMEPart mPart = (MIMEPart)itParts.next();
            Iterator extns = mPart.getExtensibilityElements().iterator();
            while (extns.hasNext()) {
                ExtensibilityElement extElement = (ExtensibilityElement)extns.next();
                if (extElement instanceof MIMEContent) {
                    MIMEContent mimeContent = (MIMEContent)extElement;
                    String mimeJavaType = getJavaTypeForMimeType(mPart);
                    if (JavaType.Style.IN.equals(style)) {
                        String paramName = ProcessorUtil.mangleNameToVariableName(mimeContent.getPart());
                        JavaParameter jp = jm.getParameter(paramName);
                        if (jp == null) {
                            Message message = new Message("MIMEPART_CANNOT_MAP", LOG, mimeContent.getPart());
                            throw new ToolException(message);
                        }
                        if (!jp.getClassName().equals(mimeJavaType)) {
                            // jp.setType(mimeJavaType);
                            jp.setClassName(mimeJavaType);
                        }
                    } else if (JavaType.Style.OUT.equals(style)) {
                        if (mimeParts.size() > 2) {
                            // more than 1 mime:content part (1 root soap body),
                            // javaReturn will be set to void and
                            // all output parameter will be treated as the
                            // holder class
                            String paramName = ProcessorUtil.mangleNameToVariableName(mimeContent.getPart());
                            JavaParameter jp = jm.getParameter(paramName);
                            if (jp == null) {
                                Message message = new Message("MIMEPART_CANNOT_MAP", LOG, mimeContent
                                    .getPart());
                                throw new ToolException(message);
                            } else {
                                if (!jp.getClassName().equals(mimeJavaType)) {
                                    // jp.setType(mimeJavaType);
                                    jp.setClassName(mimeJavaType);
                                    jp.setHolderClass(mimeJavaType);
                                }
                            }
                        } else {
                            if (!jm.getReturn().getClassName().equals(mimeJavaType)) {
                                // jm.getReturn().setType(mimeJavaType);
                                jm.getReturn().setClassName(mimeJavaType);
                            }
                        }
                    }
                }
            }
        }
    }
}
