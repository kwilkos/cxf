package org.objectweb.celtix.tools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import org.objectweb.celtix.tools.common.ToolException;

public final class WSDLParserUtil {

    private WSDLParserUtil() {
        // complete
    }

    public static List<PortType> getPortTypes(Definition def) {
        List<PortType> portTypes = new ArrayList<PortType>();
        Iterator ite = def.getPortTypes().values().iterator();
        while (ite.hasNext()) {
            PortType portType = (PortType)ite.next();
            portTypes.add(portType);
        }
        return portTypes;
    }

    public static List<Part> getInMessageParts(Operation operation) {
        Input input = operation.getInput();
        List<Part> partsList = new ArrayList<Part>();
        if (input != null) {
            Iterator ite = input.getMessage().getParts().values().iterator();
            while (ite.hasNext()) {
                partsList.add((Part)ite.next());
            }
        }
        return partsList;
    }

    public static List<Part> getOutMessageParts(Operation operation) {
        Output output = operation.getOutput();
        List<Part> partsList = new ArrayList<Part>();
        if (output != null) {
            Iterator ite = output.getMessage().getParts().values().iterator();
            while (ite.hasNext()) {
                partsList.add((Part)ite.next());
            }
        }
        return partsList;
    }

    public static BindingOperation getBindingOperation(Operation operation, Definition def) {
        Iterator ite = def.getBindings().values().iterator();
        while (ite.hasNext()) {
            Binding binding = (Binding)ite.next();
            Iterator ite1 = binding.getBindingOperations().iterator();
            while (ite1.hasNext()) {
                BindingOperation bop = (BindingOperation)ite1.next();
                if (bop.getName().equals(operation.getName())) {
                    return bop;
                }
            }

        }

        return null;
    }

    public static String getBindingStyle(BindingOperation bop, Definition def) {
        String style = "DOCUMENT";
        Iterator ite = def.getBindings().values().iterator();
        while (ite.hasNext()) {
            Binding binding = (Binding)ite.next();
            BindingOperation bindingOp = binding.getBindingOperation(bop.getName(), null, null);
            if (bindingOp != null) {
                Iterator ite2 = bindingOp.getExtensibilityElements().iterator();
                while (ite2.hasNext()) {
                    Object obj = ite2.next();
                    if (obj instanceof SOAPBinding) {
                        SOAPBinding soapBinding = (SOAPBinding)obj;
                        style = soapBinding.getStyle();
                        break;
                    }

                }
                break;
            }
        }
        return style;
    }

    public static String getSOAPOperationStyle(BindingOperation bop) {
        String style = "";
        Iterator ite = bop.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation)obj;
                style = soapOperation.getStyle();
                break;
            }
        }
        return style;

    }

    public static SOAPBody getBindingInputSOAPBody(BindingOperation bop) {
        BindingInput bindingInput = bop.getBindingInput();
        if (bindingInput != null) {
            Iterator ite = bindingInput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPBody) {
                    return (SOAPBody)obj;
                }
            }
        }

        return null;
    }

    public static SOAPHeader getBindingInputSOAPHeader(BindingOperation bop) {
        BindingInput bindingInput = bop.getBindingInput();
        if (bindingInput != null) {
            Iterator ite = bindingInput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPHeader) {
                    return (SOAPHeader)obj;
                }
            }
        }

        return null;
    }

    public static SOAPHeader getBindingOutputSOAPHeader(BindingOperation bop) {
        BindingOutput bindingOutput = bop.getBindingOutput();
        if (bindingOutput != null) {
            Iterator ite = bindingOutput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPHeader) {
                    return (SOAPHeader)obj;
                }
            }
        }

        return null;
    }

    public static SOAPBody getBindingOutputSOAPBody(BindingOperation bop) {
        BindingOutput bindingOutput = bop.getBindingOutput();
        if (bindingOutput != null) {
            Iterator ite = bindingOutput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPBody) {
                    return (SOAPBody)obj;
                }
            }
        }

        return null;
    }

    public static Definition getDefinition(File wsdlFile) {
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();           
            WSDLReader reader = wsdlFactory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            return reader.readWSDL(wsdlFile.toURL().toString());
        } catch (Exception e) {
            throw new ToolException("Get wsdl definition error", e);
        }     
    }   
   
}
