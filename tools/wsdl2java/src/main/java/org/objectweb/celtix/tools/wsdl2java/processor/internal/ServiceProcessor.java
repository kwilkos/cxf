package org.objectweb.celtix.tools.wsdl2java.processor.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;

import org.objectweb.celtix.tools.common.extensions.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.common.extensions.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.common.extensions.jms.JMSAddress;

import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.common.toolspec.parser.CommandLineParser;

import org.objectweb.celtix.tools.util.ProcessorUtil;

public class ServiceProcessor extends AbstractProcessor {

    private static final Logger LOG = LogUtils.getL7dLogger(CommandLineParser.class);

    private String soapOPAction = "SOAPACTION";

    private String soapOPStyle = "STYLE";

    private Definition definition;

    private BindingType bindingType;

    private final int inHEADER = 1;

    private final int outHEADER = 2;

    private final int resultHeader = 3;

    private final int noHEADER = 0;

    private Object bindingObj;

    public ServiceProcessor(ProcessorEnvironment penv) {
        super(penv);
    }

    public ServiceProcessor(ProcessorEnvironment penv, Definition def) {
        super(penv);
        this.definition = def;
    }

    public void process(JavaModel model) throws ToolException {
        
        Collection services = definition.getServices().values();
        if (services.size() == 0) {
            Iterator bindingIte = definition.getBindings().values().iterator();
            while (bindingIte.hasNext()) {
                Binding binding = (Binding)bindingIte.next();
                Iterator bopIte = binding.getBindingOperations().iterator();
                while (bopIte.hasNext()) {
                    BindingOperation bop = (BindingOperation)bopIte.next();
                    processOperation(model, bop, binding);
                }
            }
            return;
        }
        Iterator ite = services.iterator();
        while (ite.hasNext()) {
            Service service = (Service)ite.next();
            processService(model, service);
        }
    }

    private boolean isNameCollision(String packageName, String className) {
        return collector.containTypesClass(packageName, className)
               || collector.containSeiClass(packageName, className)
               || collector.containExceptionClass(packageName, className);
    }

    private void processService(JavaModel model, Service service) throws ToolException {
        JavaServiceClass sclz = new JavaServiceClass(model);
        String name = ProcessorUtil.mangleNameToClassName(service.getQName().getLocalPart());
        String namespace = service.getQName().getNamespaceURI();
        String packageName = ProcessorUtil.parsePackageName(namespace, env.mapPackageName(namespace));

        while (isNameCollision(packageName, name)) {
            name = name + "_Service";
        }

        sclz.setName(name);
        sclz.setServiceName(service.getQName().getLocalPart());
        sclz.setNamespace(namespace);
        sclz.setPackageName(packageName);

        Map ports = service.getPorts();

        for (Iterator ite = ports.values().iterator(); ite.hasNext();) {
            Port port = (Port)ite.next();
            JavaPort javaport = processPort(model, port);
            sclz.addPort(javaport);
        }
        model.addServiceClass(name, sclz);
    }

    private JavaPort processPort(JavaModel model, Port port) throws ToolException {
        JavaPort jport = new JavaPort(ProcessorUtil.mangleNameToClassName(port.getName()));
        jport.setPortName(port.getName());
        Binding binding = port.getBinding();
        // TODO: extend other bindings
        jport.setBindingAdress(getPortAddress(port));
        jport.setBindingName(binding.getQName().getLocalPart());

        String namespace = binding.getPortType().getQName().getNamespaceURI();
        String packageName = ProcessorUtil.parsePackageName(namespace, env.mapPackageName(namespace));
        jport.setPackageName(packageName);

        String portType = binding.getPortType().getQName().getLocalPart();
        jport.setPortType(portType);
        jport.setInterfaceClass(ProcessorUtil.mangleNameToClassName(portType));

        bindingType = getBindingType(binding);

        if (bindingType == null) {
            org.objectweb.celtix.common.i18n.Message msg = 
                new org.objectweb.celtix.common.i18n.Message("BINDING_SPECIFY_ONE_PROTOCOL",
                                                              LOG,
                                                              binding.getQName());
            throw new ToolException(msg);
        }

        if (isSoapBinding()) {
            SOAPBinding spbd = (SOAPBinding)this.bindingObj;
            jport.setStyle(getSoapStyle(spbd.getStyle()));
            jport.setTransURI(spbd.getTransportURI());
        }

        /*
         * if (bindingType.name().equals("HTTPBinding")) { // TBD }
         */

        Iterator ite = binding.getBindingOperations().iterator();
        while (ite.hasNext()) {
            BindingOperation bop = (BindingOperation)ite.next();
            processOperation(model, bop, binding);
        }
        return jport;
    }

    private javax.jws.soap.SOAPBinding.Style getSoapStyle(String soapStyle) {
        if ("".equals(soapStyle)) {
            return null;
        } else if ("RPC".equalsIgnoreCase(soapStyle)) {
            return javax.jws.soap.SOAPBinding.Style.RPC;
        } else {
            return javax.jws.soap.SOAPBinding.Style.DOCUMENT;
        }
    }

    private javax.jws.soap.SOAPBinding.Use getSoapUse(String soapUse) {

        if ("".equals(soapUse)) {
            return null;
        } else if ("ENCODED".equalsIgnoreCase(soapUse)) {
            return javax.jws.soap.SOAPBinding.Use.ENCODED;
        } else {
            return javax.jws.soap.SOAPBinding.Use.LITERAL;
        }

    }

    

    private void processOperation(JavaModel model, BindingOperation bop, Binding binding)
        throws ToolException {
        String portType = ProcessorUtil
            .mangleNameToClassName(binding.getPortType().getQName().getLocalPart());
        JavaInterface jf = model.getInterfaces().get(portType);
        // TODO: extend other bindings
        doCustomizeBinding(model, jf, binding);
        if (isSoapBinding()) {
            SOAPBinding soapBinding = (SOAPBinding)bindingObj;
            if (getSoapStyle(soapBinding.getStyle()) == null) {
                jf.setSOAPStyle(javax.jws.soap.SOAPBinding.Style.DOCUMENT);
            } else {
                jf.setSOAPStyle(getSoapStyle(soapBinding.getStyle()));
            }
        } else {
            // REVISIT: fix for xml binding
            jf.setSOAPStyle(javax.jws.soap.SOAPBinding.Style.DOCUMENT);
        }

        Object[] methods = jf.getMethods().toArray();
        for (int i = 0; i < methods.length; i++) {
            JavaMethod jm = (JavaMethod)methods[i];
            if (jm.getOperationName() != null && jm.getOperationName().equals(bop.getName())) {
                if (isSoapBinding()) {
                    doCustomizeOperation(jf, jm, bop);
                    Map prop = getSoapOperationProp(bop);
                    String soapAction = prop.get(soapOPAction) == null ? "" : (String)prop.get(soapOPAction);
                    String soapStyle = prop.get(soapOPStyle) == null ? "" : (String)prop.get(soapOPStyle);
                    jm.setSoapAction(soapAction);
                    if (getSoapStyle(soapStyle) == null && this.bindingObj == null) {
                        org.objectweb.celtix.common.i18n.Message msg = 
                            new org.objectweb.celtix.common.i18n.Message("BINDING_STYLE_NOT_DEFINED",
                                                                          LOG);
                        throw new ToolException(msg);
                    }
                    if (getSoapStyle(soapStyle) == null) {
                        jm.setSoapStyle(jf.getSOAPStyle());
                    } else {
                        jm.setSoapStyle(getSoapStyle(soapStyle));
                    }
                } else {
                    // REVISIT: fix for xml binding
                    jm.setSoapStyle(jf.getSOAPStyle());
                }
                
                if (jm.getSoapStyle().equals(javax.jws.soap.SOAPBinding.Style.RPC)) {
                    jm.getAnnotationMap().remove("SOAPBinding");
                }
                
                OperationProcessor processor = new OperationProcessor(env);

                int headerType = isNonWrappable(bop);

                if (jm.isWrapperStyle() && headerType > this.noHEADER) {
                    // changed wrapper style
                   
                    jm.setWrapperStyle(false);
                    processor.processMethod(jm, bop.getOperation());
                    jm.getAnnotationMap().remove("ResponseWrapper");
                    jm.getAnnotationMap().remove("RequestWrapper");

                } else {
                    processor.processMethod(jm, bop.getOperation());
                  
                }

                if (headerType == this.resultHeader) {
                    JavaAnnotation resultAnno = jm.getAnnotationMap().get("WebResult");
                    if (resultAnno != null) {
                        resultAnno.addArgument("header", "true", "");
                    }
                }
                processParameter(jm, bop);
            }
        }
    }

    private void setParameterAsHeader(JavaParameter parameter) {
        parameter.setHeader(true);
        parameter.getAnnotation().addArgument("header", "true", "");
    }

    private void processParameter(JavaMethod jm, BindingOperation operation) throws ToolException {

        // process input
        Iterator inbindings = operation.getBindingInput().getExtensibilityElements().iterator();
        String use = null;
        while (inbindings.hasNext()) {
            Object obj = inbindings.next();
            if (obj instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody)obj;
                use = soapBody.getUse();
            }
            if (obj instanceof SOAPHeader) {
                SOAPHeader soapHeader = (SOAPHeader)obj;
                boolean found = false;
                for (JavaParameter parameter : jm.getParameters()) {
                    if (soapHeader.getPart().equals(parameter.getPartName())) {
                        setParameterAsHeader(parameter);
                        found = true;
                    }
                }
                if (Boolean.valueOf((String)env.get(ToolConstants.CFG_EXTRA_SOAPHEADER)).booleanValue()
                    && !found) {
                    // Header can't be found in java method parameters, in
                    // different message
                    // other than messages used in porttype operation
                    ParameterProcessor processor = new ParameterProcessor(this.env);
                    Part exPart = this.definition.getMessage(soapHeader.getMessage()).getPart(
                                                                                              soapHeader
                                                                                                  .getPart());
                    JavaType.Style jpStyle = JavaType.Style.IN;
                    if (isInOutParam(soapHeader.getPart(), operation.getBindingOutput())) {
                        jpStyle = JavaType.Style.INOUT;
                    }
                    JavaParameter jp = processor.addParameterFromBinding(jm, exPart, jpStyle);
                    if (soapHeader.getPart() != null && soapHeader.getPart().length() > 0) {
                        jp.getAnnotation().addArgument("partName", soapHeader.getPart());
                    }
                    setParameterAsHeader(jp);
                }
            }
            if (obj instanceof MIMEMultipartRelated && jm.getBindingExt().isEnableMime()) {
                // Commented for future use
                LOG.warning("The MIME content in wsdl file will be ignored, "
                            + "current version does not support MIME content");
                // MIMEProcessor mimeProcessor = new MIMEProcessor(this.env);
                // mimeProcessor.process(jm, (MIMEMultipartRelated)obj,
                // JavaType.Style.IN);
            }
        }

        // process output
        if (operation.getBindingOutput() != null) {
            Iterator outbindings = operation.getBindingOutput().getExtensibilityElements().iterator();
            while (outbindings.hasNext()) {
                Object obj = outbindings.next();
                if (obj instanceof SOAPHeader) {
                    SOAPHeader soapHeader = (SOAPHeader)obj;
                    boolean found = false;
                    for (JavaParameter parameter : jm.getParameters()) {
                        if (soapHeader.getPart().equals(parameter.getPartName())) {
                            setParameterAsHeader(parameter);
                            found = true;
                        }
                    }
                    if (jm.getReturn().getName().equals(soapHeader.getPart())) {
                        found = true;
                    }
                    if (Boolean.valueOf((String)env.get(ToolConstants.CFG_EXTRA_SOAPHEADER)).booleanValue()
                        && !found) {
                        // Header can't be found in java method parameters, in
                        // different message
                        // other than messages used in porttype operation
                        ParameterProcessor processor = new ParameterProcessor(this.env);
                        Part exPart = this.definition.getMessage(soapHeader.getMessage())
                            .getPart(soapHeader.getPart());
                        JavaParameter jp = processor.addParameterFromBinding(jm, exPart, JavaType.Style.OUT);
                        setParameterAsHeader(jp);
                    }
                }
                if (obj instanceof MIMEMultipartRelated && jm.getBindingExt().isEnableMime()) {
                    // Commented for future use
                    LOG.warning("The MIME content in wsdl file will be ignored, "
                                + "current version does not support MIME content");
                    // MIMEProcessor mimeProcessor = new
                    // MIMEProcessor(this.env);
                    // mimeProcessor.process(jm, (MIMEMultipartRelated)obj,
                    // JavaType.Style.OUT);

                }
            }
        }

        jm.setSoapUse(getSoapUse(use));
        if (javax.jws.soap.SOAPBinding.Style.RPC == jm.getSoapStyle()
            && javax.jws.soap.SOAPBinding.Use.ENCODED == jm.getSoapUse()) {
            System.err.println("** Unsupported RPC-Encoded Style Use **");
        }
        if (javax.jws.soap.SOAPBinding.Style.RPC == jm.getSoapStyle()
            && javax.jws.soap.SOAPBinding.Use.LITERAL == jm.getSoapUse()) {
            return;
        }
        if (javax.jws.soap.SOAPBinding.Style.DOCUMENT == jm.getSoapStyle()
            && javax.jws.soap.SOAPBinding.Use.LITERAL == jm.getSoapUse()) {
            return;
        }
    }

    private Map getSoapOperationProp(BindingOperation bop) {
        Map<String, Object> soapOPProp = new HashMap<String, Object>();

        if (bop.getExtensibilityElements() != null) {
            Iterator ite = bop.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPOperation) {
                    SOAPOperation soapOP = (SOAPOperation)obj;
                    soapOPProp.put(this.soapOPAction, soapOP.getSoapActionURI());

                    soapOPProp.put(this.soapOPStyle, soapOP.getStyle());
                }
            }
        }
        return soapOPProp;
    }

    private String getPortAddress(Port port) {
        Iterator it = port.getExtensibilityElements().iterator();
        String address = null;
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SOAPAddress) {
                address = ((SOAPAddress)obj).getLocationURI();
            }
            if (obj instanceof JMSAddress) {
                address = ((JMSAddress)obj).getAddress();
            }

            if (obj instanceof HTTPAddress) {
                address = ((HTTPAddress)obj).getLocationURI();
            }

        }
        return address;
    }

    private BindingType getBindingType(Binding binding) {
        Iterator it = binding.getExtensibilityElements().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SOAPBinding) {
                bindingObj = (SOAPBinding)obj;
                return BindingType.SOAPBinding;
            }
            if (obj instanceof HTTPBinding) {
                bindingObj = (HTTPBinding)obj;
                return BindingType.HTTPBinding;
            }
            // TBD XMLBinding
            return BindingType.XMLBinding;

        }
        return null;
    }

    private int isNonWrappable(BindingOperation bop) {
        String operationName = bop.getName();
        Message bodyMessage = null;
        QName headerMessage = null;
        SOAPHeader header = null;
        boolean containParts = false;
        boolean isSameMessage = false;
        boolean isNonWrappable = false;
        boolean allPartsHeader = false;
        int result = this.noHEADER;

        // begin process input
        if (bop.getBindingInput() != null) {
            Iterator ite = bop.getBindingInput().getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPBody) {
                    bodyMessage = getMessage(operationName, true);
                }
                if (obj instanceof SOAPHeader) {
                    header = (SOAPHeader)obj;
                    headerMessage = header.getMessage();
                    if (header.getPart().length() > 0) {
                        containParts = true;
                    }
                }
            }

            if (headerMessage != null && bodyMessage != null
                && headerMessage.getNamespaceURI().equalsIgnoreCase(bodyMessage.getQName().getNamespaceURI())
                && headerMessage.getLocalPart().equalsIgnoreCase(bodyMessage.getQName().getLocalPart())) {
                isSameMessage = true;
            }

            isNonWrappable = isSameMessage && containParts;
            // if is nonwrapple then return
            if (isNonWrappable) {
                result = this.inHEADER;
            }
        }
        isSameMessage = false;
        containParts = false;

        // process output
        if (bop.getBindingOutput() != null) {
            Iterator ite1 = bop.getBindingOutput().getExtensibilityElements().iterator();
            while (ite1.hasNext()) {
                Object obj = ite1.next();
                if (obj instanceof SOAPBody) {
                    bodyMessage = getMessage(operationName, false);
                }
                if (obj instanceof SOAPHeader) {
                    header = (SOAPHeader)obj;
                    headerMessage = header.getMessage();
                    if (header.getPart().length() > 0) {
                        containParts = true;
                    }
                }
            }
            if (headerMessage != null && bodyMessage != null
                && headerMessage.getNamespaceURI().equalsIgnoreCase(bodyMessage.getQName().getNamespaceURI())
                && headerMessage.getLocalPart().equalsIgnoreCase(bodyMessage.getQName().getLocalPart())) {
                isSameMessage = true;
                if (bodyMessage.getParts().size() == 1) {
                    allPartsHeader = true;
                }

            }
            isNonWrappable = isSameMessage && containParts;
            if (isNonWrappable && allPartsHeader) {
                result = this.resultHeader;
            }
            if (isNonWrappable && !allPartsHeader) {
                result = this.outHEADER;
            }
        }

        return result;

    }

    private Message getMessage(String operationName, boolean isIn) {
        Iterator ite = definition.getPortTypes().values().iterator();
        Message msg = null;
        while (ite.hasNext()) {
            PortType portType = (PortType)ite.next();
            Iterator ite1 = portType.getOperations().iterator();
            while (ite1.hasNext()) {
                Operation op = (Operation)ite1.next();
                if (operationName.equals(op.getName())) {
                    if (isIn) {
                        msg = op.getInput().getMessage();
                    } else {
                        msg = op.getOutput().getMessage();
                    }
                    break;
                }
            }
        }

        return msg;
    }

    private void doCustomizeBinding(JavaModel jmodel, JavaInterface ji, Binding binding) {
        JAXWSBinding bindingExt = null;
        List extElements = binding.getExtensibilityElements();
        if (extElements.size() > 0) {
            Iterator iterator = extElements.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof JAXWSBinding) {
                    bindingExt = (JAXWSBinding)obj;
                    ji.setBindingExt(bindingExt);
                    return;
                }
            }
        }
        String portTypeName = binding.getPortType().getQName().getLocalPart();
        bindingExt = CustomizationParser.getInstance().getPortTypeExtension(portTypeName);
        if (bindingExt != null) {
            if (!bindingExt.isSetMimeEnable() && jmodel.getJAXWSBinding().isSetMimeEnable()
                && jmodel.getJAXWSBinding().isEnableMime()) {
                bindingExt.setSetMimeEnable(true);
                bindingExt.setEnableMime(true);
            }
        } else if (jmodel.getJAXWSBinding() != null) {
            bindingExt = new JAXWSBinding();
            if (jmodel.getJAXWSBinding().isSetMimeEnable() && jmodel.getJAXWSBinding().isEnableMime()) {
                bindingExt.setSetMimeEnable(true);
                bindingExt.setEnableMime(true);
            }
        } else {
            // TBD: There is no extensibilityelement in port type
            bindingExt = new JAXWSBinding();
        }
        ji.setBindingExt(bindingExt);
    }

    private void doCustomizeOperation(JavaInterface ji, JavaMethod jm, BindingOperation bo) {
        JAXWSBinding bindingExt = null;
        List extElements = bo.getExtensibilityElements();
        if (extElements.size() > 0) {
            Iterator iterator = extElements.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof JAXWSBinding) {
                    bindingExt = (JAXWSBinding)obj;
                    jm.setBindingExt(bindingExt);
                    return;
                }
            }
        }
        String portTypeName = ji.getWebServiceName();
        String operationName = bo.getName();
        bindingExt = CustomizationParser.getInstance().getPortTypeOperationExtension(portTypeName,
                                                                                     operationName);
        if (bindingExt != null) {
            if (!bindingExt.isSetMimeEnable() && ji.getBindingExt() != null
                && ji.getBindingExt().isSetMimeEnable() && ji.getBindingExt().isEnableMime()) {
                bindingExt.setSetMimeEnable(true);
                bindingExt.setEnableMime(true);
            }
        } else if (ji.getBindingExt() != null) {
            bindingExt = new JAXWSBinding();
            if (ji.getBindingExt().isSetMimeEnable() && ji.getBindingExt().isEnableMime()) {
                bindingExt.setSetMimeEnable(true);
                bindingExt.setEnableMime(true);
            }
        } else {
            // TBD: There is no extensibilityelement in port type
            bindingExt = new JAXWSBinding();
        }
        jm.setBindingExt(bindingExt);
    }

    public enum BindingType {
        HTTPBinding, SOAPBinding, XMLBinding
    }

    private boolean isSoapBinding() {
        return bindingType != null && "SOAPBinding".equals(bindingType.name());

    }

    private boolean isInOutParam(String inPartName, BindingOutput bop) {
        Iterator it = bop.getExtensibilityElements().iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SOAPHeader) {
                String outPartName = ((SOAPHeader)obj).getPart();
                if (inPartName.equals(outPartName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
