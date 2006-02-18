package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.extensions.jms.JMSAddress;
import org.objectweb.celtix.tools.utils.ClassCollectorUtil;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class ServiceProcessor {

    ClassCollectorUtil collector = ClassCollectorUtil.getInstance();

    private final String soapOPAction = "SOAPACTION";

    private final String soapOPStyle = "STYLE";

    private final ProcessorEnvironment env;

    private final Definition definition;

    private final int inHEADER = 1;

    private final int outHEADER = 2;

    private final int resultHeader = 3;

    private final int noHEADER = 0;

    public ServiceProcessor(ProcessorEnvironment penv, Definition def) {
        this.env = penv;
        this.definition = def;

    }

    public void process(JavaModel model) throws ToolException {
        Collection services = definition.getServices().values();
        if (services == null) {
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
        JavaPort jport = new JavaPort(port.getName());
        Binding binding = port.getBinding();
        // TODO: extend other bindings
        jport.setBindingAdress(getPortAddress(port));
        jport.setBindingName(binding.getQName().getLocalPart());
        jport.setPortType(binding.getPortType().getQName().getLocalPart());
        SOAPBinding spbd = getSOAPBinding(binding);
        String portType = binding.getPortType().getQName().getLocalPart();
        jport.setPortType(portType);
        jport.setInterfaceClass(ProcessorUtil.mangleNameToClassName(portType));
        jport.setStyle(getSoapStyle(spbd.getStyle()));
        jport.setTransURI(spbd.getTransportURI());

        Iterator ite = binding.getBindingOperations().iterator();
        while (ite.hasNext()) {
            BindingOperation bop = (BindingOperation)ite.next();
            processOperation(model, port, bop);
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

    private void processOperation(JavaModel model, Port port, BindingOperation bop) throws ToolException {
        String portType = ProcessorUtil.mangleNameToClassName(port.getBinding().getPortType().getQName()
            .getLocalPart());
        JavaInterface jf = model.getInterfaces().get(portType);
        // TODO: extend other bindings
        SOAPBinding soapBinding = getSOAPBinding(port.getBinding());
        if (soapBinding != null) {
            if (getSoapStyle(soapBinding.getStyle()) == null) {
                jf.setSOAPStyle(javax.jws.soap.SOAPBinding.Style.DOCUMENT);
            } else {
                jf.setSOAPStyle(getSoapStyle(soapBinding.getStyle()));
            }
        }
        Object[] methods = jf.getMethods().toArray();
        for (int i = 0; i < methods.length; i++) {
            JavaMethod jm = (JavaMethod)methods[i];
            if (jm.getOperationName() != null && jm.getOperationName().equals(bop.getName())) {
                Map prop = getSoapOperationProp(bop);
                String soapAction = prop.get(soapOPAction) == null ? "" : (String)prop.get(soapOPAction);
                String soapStyle = prop.get(soapOPStyle) == null ? "" : (String)prop.get(soapOPStyle);
                jm.setSoapAction(soapAction);
                if (getSoapStyle(soapStyle) == null && soapBinding == null) {
                    throw new ToolException("Operation Binding Style Should Be Defined");
                }
                if (getSoapStyle(soapStyle) == null) {
                    jm.setSoapStyle(jf.getSOAPStyle());
                } else {
                    jm.setSoapStyle(getSoapStyle(soapStyle));
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

    private void processParameter(JavaMethod jm, BindingOperation operation) {
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
                for (JavaParameter parameter : jm.getParameters()) {
                    if (soapHeader.getPart().equals(parameter.getPartName())) {
                        setParameterAsHeader(parameter);
                    }
                }
            }
        }

        // process output
        if (operation.getBindingOutput() != null) {
            Iterator outbindings = operation.getBindingOutput().getExtensibilityElements().iterator();
            while (outbindings.hasNext()) {
                Object obj = outbindings.next();
                if (obj instanceof SOAPHeader) {
                    SOAPHeader soapHeader = (SOAPHeader)obj;
                    for (JavaParameter parameter : jm.getParameters()) {
                        if (soapHeader.getPart().equals(parameter.getPartName())) {
                            setParameterAsHeader(parameter);
                        }
                    }
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
            processRPCLiteralParameter(jm, operation);
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
        }
        return address;
    }
    
    private SOAPBinding getSOAPBinding(Binding binding) {
        Iterator it = binding.getExtensibilityElements().iterator();
        SOAPBinding spbinding = null;
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SOAPBinding) {
                spbinding = (SOAPBinding)obj;
            }
        }
        return spbinding;
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

    private void processRPCLiteralParameter(JavaMethod jm, BindingOperation operation) {
        // to be done
    }
}
