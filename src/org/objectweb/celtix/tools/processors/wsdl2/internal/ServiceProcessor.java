package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaServiceClass;
import org.objectweb.celtix.tools.utils.ProcessorUtil;


public class ServiceProcessor {
    private final String soapOPAction = "SOAPACTION";
    private final String soapOPStyle = "STYLE";
    private final ProcessorEnvironment env;

    public ServiceProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }

    public void process(JavaModel model, Definition definition) throws Exception {
        Collection services = definition.getServices().values();
        if (services == null) {
            return;
        }
        Iterator ite = services.iterator();
        while (ite.hasNext()) {
            Service service = (Service)ite.next();
            processService(model, service, definition);
        }
    }

    private void processService(JavaModel model, Service service, Definition definition) throws Exception {
        JavaServiceClass sclz = new JavaServiceClass(model);
        String name = ProcessorUtil.mangleNameToClassName(service.getQName().getLocalPart());
        sclz.setName(name);
        String namespace = service.getQName().getNamespaceURI();
        sclz.setNamespace(namespace);
        String packageName = ProcessorUtil.parsePackageName(namespace, (String[])env
            .get(ToolConstants.CFG_PACKAGENAME));
        sclz.setPackageName(packageName);
        Map ports = service.getPorts();

        for (Iterator ite = ports.values().iterator(); ite.hasNext();) {
            Port port = (Port)ite.next();
            JavaPort javaport = processPort(model, port);
            sclz.addPort(javaport);
        }
        model.addServiceClass("name", sclz);
    }

    private JavaPort processPort(JavaModel model, Port port) throws Exception {
        JavaPort jport = new JavaPort(port.getName());
        Binding binding = port.getBinding();
        // if it is SOAPBinding
        jport.setBindingAdress(getSOAPAdress(port));
        jport.setBindingName(binding.getQName().getLocalPart());
        jport.setPortType(binding.getPortType().getQName().getLocalPart());
        SOAPBinding spbd = getSOAPBinding(binding);
        String portType = binding.getPortType().getQName().getLocalPart();
        jport.setPortType(portType);
        jport.setInterfaceClass(ProcessorUtil.mangleNameToClassName(portType));
        jport.setStyle(spbd.getStyle());
        jport.setTransURI(spbd.getTransportURI());
        Iterator ite = binding.getBindingOperations().iterator();
        while (ite.hasNext()) {
            BindingOperation bop = (BindingOperation)ite.next();
            processOperation(model, port, bop);
        }
        return jport;

    }

    private void processOperation(JavaModel model, Port port, BindingOperation bop) throws Exception {
        String portType = port.getBinding().getPortType().getQName().getLocalPart();
        JavaInterface jf = model.getInterfaces().get(portType);
        Iterator ite = jf.getMethods().iterator();
        while (ite.hasNext()) {
            JavaMethod jm = (JavaMethod)ite.next();
            if (jm.getName().equals(bop.getName())) {
                Map prop = getSoapOperationProp(bop);
                jm.setSoapAction(prop.get(soapOPAction) == null ? "" : (String)prop.get(soapOPAction));
                jm.setSoapStyle(prop.get(soapOPStyle) == null ? "" : (String)prop.get(soapOPStyle));

                if (jm.isWrapperStyle() && isNonWrappable(bop)) {
                    // changed wrapper style
                    jm.setWrapperStyle(false);
                    OperationProcessor processor = new OperationProcessor(env);
                    processor.processMethod(jm, bop.getOperation());
                }
                processParameter(jm, bop);

            }
        }
    }

    private void processParameter(JavaMethod jm, BindingOperation operation) {
        // process input
        Iterator ite = operation.getBindingInput().getExtensibilityElements().iterator();
        String use = null;
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody)obj;
                use = soapBody.getUse();

            }
        }
        jm.setSoapUse(use);
        if ("RPC".equalsIgnoreCase(jm.getSoapStyle()) && "Encoded".equalsIgnoreCase(jm.getSoapUse())) {
            System.out.println("** Unsupported RPC-Encoded Style Use **");
        }
        if ("RPC".equalsIgnoreCase(jm.getSoapStyle()) && "literal".equalsIgnoreCase(jm.getSoapUse())) {
            processRPCLiteralParameter(jm, operation);
        }
        if ("Docuement".equalsIgnoreCase(jm.getSoapStyle()) && "literal".equalsIgnoreCase(jm.getSoapUse())) {
            return;
        }

    }

    private Map getSoapOperationProp(BindingOperation bop) {
        Map<String, Object> soapOPProp = new HashMap<String, Object>();
        Iterator ite = bop.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPOperation) {
                SOAPOperation soapOP = (SOAPOperation)obj;
                soapOPProp.put(this.soapOPAction, soapOP.getSoapActionURI());
                soapOPProp.put(this.soapOPStyle, soapOP.getStyle());
            }
        }
        return soapOPProp;
    }

    private String getSOAPAdress(Port port) {
        Iterator it = port.getExtensibilityElements().iterator();
        String address = null;
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof SOAPAddress) {
                address = ((SOAPAddress)obj).getLocationURI();
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

    private boolean isNonWrappable(BindingOperation bop) {

        Iterator ite = bop.getBindingInput().getExtensibilityElements().iterator();

        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPBody) {
                SOAPBody body = (SOAPBody)obj;
                if (body.getParts() == null) {
                    return false;
                }
                if (body.getParts().size() > 0) {
                    return true;
                }
            }
        }

        ite = bop.getBindingInput().getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPBody) {
                SOAPBody body = (SOAPBody)obj;
                if (body.getParts() == null) {
                    return false;
                }
                if (body.getParts().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processRPCLiteralParameter(JavaMethod jm, BindingOperation operation) {
        // to be done
    }   
}
