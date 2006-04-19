package org.objectweb.celtix.bus.bindings;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;


public class WSDLOperationInfo {
    BindingOperation bindingOp;
    WSDLMetaDataCache cache;
    
    SOAPBinding.Style style;
    SOAPBinding.Use use;
    SOAPBinding.ParameterStyle paramStyle;
    String soapAction;
    String targetNs;
    QName requestWrapperQName;
    QName responseWrapperQName;
    WebResult result;
    List<OperationWebParam> params;
    
    public WSDLOperationInfo(WSDLMetaDataCache c, BindingOperation op) {
        cache = c;
        bindingOp = op;
    }
    
    public String getName() {
        return bindingOp.getName();
    }
    
    public boolean isOneWay() {
        return getMessage(true) != null && getMessage(false) == null;
    }
    
    public SOAPBinding.Style getSOAPStyle() {
        if (style == null) {
            SOAPOperation soapOperation = getExtensibilityElement(bindingOp.getExtensibilityElements(),
                                                                  SOAPOperation.class);
            if (soapOperation != null
                && soapOperation.getStyle() != null
                && !"".equals(soapOperation.getStyle())) {
                style = SOAPBinding.Style.valueOf(soapOperation.getStyle().toUpperCase());
            }
            if (style == null) {
                style = cache.getStyle();
            }
        }
        return style;        
    }
    
    public SOAPBinding.Use getSOAPUse() {
        if (use == null) {
            SOAPBody body = getSOAPBody(true);
            
            if (body != null
                && body.getUse() != null
                && !"".equals(body.getUse())) {
                use = SOAPBinding.Use.valueOf(body.getUse().toUpperCase());
            }
            if (use == null) {
                use = SOAPBinding.Use.LITERAL;
            }
        }
        return use;        
    }
    
    public String getOperationName() {
        return getName();
    }
    
    public String getSOAPAction() {
        if (soapAction == null) {
            SOAPOperation soapOp = getExtensibilityElement(bindingOp.getExtensibilityElements(),
                                                           SOAPOperation.class);
            if (soapOp != null) {
                soapAction = soapOp.getSoapActionURI();
            }
            if (soapAction == null) {
                soapAction = "";
            }
        }
        return soapAction;
    }
    public String getTargetNamespace() {
        if (targetNs == null) {
            SOAPBody soapBody = getSOAPBody(true);
            if (soapBody != null) {
                targetNs = soapBody.getNamespaceURI();
            }
            if (targetNs == null) {
                targetNs = cache.getTargetNamespace();
            }
        }
        return targetNs;
    }
    
    
    
    public SOAPBinding.ParameterStyle getSOAPParameterStyle() {
        if (paramStyle == null) {
            if (isDocLitWrapped() || getSOAPStyle() == SOAPBinding.Style.RPC) {
                paramStyle = SOAPBinding.ParameterStyle.WRAPPED;
            } else {
                paramStyle = SOAPBinding.ParameterStyle.BARE;
            }
        }
        return paramStyle;
    }
    
    public WebResult getWebResult() {
        if (params == null) {
            initParams();
        }
        return result;
    }
    public WebParam getWebParam(int index) {
        if (params == null) {
            initParams();
        }
        return params.get(index);
    }
    public int getParamsLength() {
        if (params == null) {
            initParams();
        }
        return params.size();
    }
    
    public QName getWebResultQName() {
        WebResult res = getWebResult();
        if (null != res) {
            if (getSOAPStyle() == Style.DOCUMENT) {
                if ("".equals(res.name())) {
                    return new QName(res.targetNamespace(),
                            "return");
                }
                return new QName(res.targetNamespace(),
                                 res.name());
            } else {
                return new QName("", res.partName());
            }
        }
        return new QName("", "");
    }

    public QName getRequestWrapperQName() {
        if (requestWrapperQName == null) {
            if (getSOAPParameterStyle() == SOAPBinding.ParameterStyle.BARE
                || getSOAPStyle() == SOAPBinding.Style.RPC) {
                requestWrapperQName = new QName("", "");
            } else {
                Message msg = getMessage(true);
                List parts = msg.getOrderedParts(null);
                Part part = (Part)parts.get(0);
                requestWrapperQName = part.getElementName();
            }
        }
        return requestWrapperQName;
    }
    public QName getResponseWrapperQName() {
        if (responseWrapperQName == null) {
            if (getSOAPParameterStyle() == SOAPBinding.ParameterStyle.BARE
                || getSOAPStyle() == SOAPBinding.Style.RPC) {
                responseWrapperQName = new QName("", "");
            } else {
                Message msg = getMessage(false);
                if (msg != null) {
                    List parts = msg.getOrderedParts(null);
                    Part part = (Part)parts.get(0);
                    responseWrapperQName = part.getElementName();
                } else {
                    responseWrapperQName = new QName("", "");
                }
            }
        }
        return responseWrapperQName;
    }
    
    
    
    private boolean isDocLitWrapped() {
        boolean flag = getSOAPStyle() == SOAPBinding.Style.DOCUMENT
            && getSOAPUse() == SOAPBinding.Use.LITERAL;
        if (!flag) {
            return false;
        }
        Message msg = getMessage(true);
        if (msg == null) {
            return false;
        }
        List parts = msg.getOrderedParts(null);
        if (parts.size() != 1) {
            return false;
        }
        Part part = (Part)parts.get(0);
        QName element = part.getElementName();
        if (element == null
            || !element.getLocalPart().equals(bindingOp.getOperation().getName())) {
            return false;
        } 
        //REVISIT check for squence
        
        msg = getMessage(true);
        if (msg != null) {
            parts = msg.getOrderedParts(null);
            if (parts.size() != 1) {
                flag = false;
            } else {
                part = (Part)parts.get(0);
                element = part.getElementName();
                if (element == null
                    || !element.getLocalPart().startsWith(bindingOp.getOperation().getName())) {
                    flag = false;
                }
                //REVISIT check for squence
            }
        }
        return true;
    } 
    private Message getMessage(boolean isInput) {
        Operation operation = bindingOp.getOperation();
        if (operation == null) {
            return null;
        }

        if (isInput) {
            final Input input = operation.getInput();
            return input == null ? null : input.getMessage();
        }
        final Output output = operation.getOutput();
        return output == null ? null : output.getMessage();
    }
    private SOAPBody getSOAPBody(boolean input) {
        List elements = null;
        if (input) {
            BindingInput bindingInput = bindingOp.getBindingInput();
            if (bindingInput == null) {
                return null;
            }
            elements = bindingInput.getExtensibilityElements();
        } else {
            BindingOutput bindingOutput = bindingOp.getBindingOutput();
            if (bindingOutput == null) {
                return null;
            }
            elements = bindingOutput.getExtensibilityElements();
        }
        return getExtensibilityElement(elements, SOAPBody.class);
    }    
    
    private void initRPCLitParam(List<OperationWebParam> parms,
                                 Map<String, OperationWebParam> parmMap,
                                 Operation operation) {
        
        Input input = operation.getInput();
        Output output = operation.getOutput();
        if (input == null) {
            //unsupported op type, output only
            return;
        }
        Collection parts = input.getMessage().getParts().values();
        for (Iterator i = parts.iterator(); i.hasNext();) {
            Part part = (Part)i.next();
            OperationWebParam p = new OperationWebParam(part.getName(),
                                                        part.getName(),
                                                        Mode.IN,
                                                        "",
                                                        cache.getTargetNamespace());
            parms.add(p);
            parmMap.put(part.getName(), p);
        }
        if (output != null) {
            parts = output.getMessage().getParts().values();
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Part part = (Part)i.next();
                
                OperationWebParam p = parmMap.get(part.getName());
                if (p == null) {
                    p = new OperationWebParam(part.getName(),
                                              part.getName(),
                                              Mode.OUT,
                                              "",
                                              cache.getTargetNamespace());
                    parms.add(p);
                    parmMap.put(part.getName(), p);
                } else {
                    p.setMode(Mode.INOUT);
                }
            }
        }
    }
                                     
    private void initDocLitBareParam(List<OperationWebParam> parms,
                                     Map<String, OperationWebParam> parmMap,
                                     Operation operation) {
        
        Input input = operation.getInput();
        Output output = operation.getOutput();
        Collection parts = input.getMessage().getParts().values();
        for (Iterator i = parts.iterator(); i.hasNext();) {
            Part part = (Part)i.next();
            OperationWebParam p = new OperationWebParam(part.getElementName().getLocalPart(),
                                                        part.getName(),
                                                        Mode.IN,
                                                        part.getElementName().getNamespaceURI());
            parms.add(p);
            parmMap.put(part.getName(), p);
        }
        if (output != null) {
            parts = output.getMessage().getParts().values();
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Part part = (Part)i.next();
                
                OperationWebParam p = parmMap.get(part.getName());
                if (p == null) {
                    p = new OperationWebParam(part.getElementName().getLocalPart(),
                                              part.getName(),
                                              Mode.OUT,
                                              part.getElementName().getNamespaceURI());
                    parms.add(p);
                    parmMap.put(part.getName(), p);
                } else {
                    p.setMode(Mode.INOUT);
                }
            }
        }                            
    }
    private void initDocLitWrappedParam(List<OperationWebParam> parms,
                                     Map<String, OperationWebParam> parmMap,
                                     Operation operation) {
        //REVISIT - how to handle the wrapped docs/lit cases.  Must dig into schema and stuff.
    }

    private synchronized void initParams() {
        List<OperationWebParam> parms = new ArrayList<OperationWebParam>();
        Map<String, OperationWebParam> parmMap = new HashMap<String, OperationWebParam>();
        
        Operation operation = bindingOp.getOperation();
        if (operation != null) {
            SOAPBinding.Style st = getSOAPStyle();
            
            if (st == SOAPBinding.Style.RPC) {
                initRPCLitParam(parms, parmMap, operation);
            } else {
                // DOC style
                if (isDocLitWrapped()) {
                    initDocLitWrappedParam(parms, parmMap, operation);
                } else {
                    initDocLitBareParam(parms, parmMap, operation);
                }
            }

            // Set the header flags
            BindingInput bindingInput = bindingOp.getBindingInput();
            if (bindingInput != null) {
                javax.wsdl.Message message = operation.getInput().getMessage();
                List elements = bindingInput.getExtensibilityElements();
                for (Iterator i = elements.iterator(); i.hasNext();) {
                    Object extensibilityElement = i.next();
                    Part part = getPartFromSOAPHeader(message, extensibilityElement);
                    if (part != null) {
                        OperationWebParam p = parmMap.get(part.getName());
                        if (p != null) {
                            p.setHeader(true);
                        }
                    }
                }
            }            
            BindingOutput bindingOutput = bindingOp.getBindingOutput();
            if (bindingOutput != null) {
                javax.wsdl.Message message = operation.getOutput().getMessage();
                List elements = bindingOutput.getExtensibilityElements();
                for (Iterator i = elements.iterator(); i.hasNext();) {
                    Object extensibilityElement = i.next();
                    Part part = getPartFromSOAPHeader(message, extensibilityElement);
                    if (part != null) {
                        OperationWebParam p = parmMap.get(part.getName());
                        if (p != null) {
                            p.setHeader(true);
                        }
                    }
                }
            }            
        }
        
        OperationWebParam returnVal = null;
        for (OperationWebParam p : parms) {
            if (p.mode() == Mode.INOUT) {
                break;
            } else if (p.mode() == Mode.OUT) {
                returnVal = p;
                break;
            }
        }
        if (returnVal != null) {
            parms.remove(returnVal);
            result = new OperationWebResult(returnVal);
        }
        
        params = parms;
    }        
 
    private Part getPartFromSOAPHeader(Message message, Object extensibilityElement) {
        Part part = null;
        if (extensibilityElement instanceof SOAPHeader) {
            SOAPHeader soapHeader = (SOAPHeader) extensibilityElement;
            QName msgName = soapHeader.getMessage();
            if (message.getQName().equals(msgName)) {
                part = message.getPart(soapHeader.getPart());
            }
        } else if (extensibilityElement instanceof SOAPHeader) {
            SOAPHeader soapHeader = (SOAPHeader) extensibilityElement;
            QName msgName = soapHeader.getMessage();
            if (message.getQName().equals(msgName)) {
                part = message.getPart(soapHeader.getPart());
            }
        }
        return part;
    }    
    private static <T> T getExtensibilityElement(List elements, Class<T> type) {
        for (Iterator i = elements.iterator(); i.hasNext();) {
            Object element = i.next();
            if (type.isInstance(element)) {
                return type.cast(element);
            }
        }
        return null;
    } 
    
    private class OperationWebResult implements WebResult {
        OperationWebParam parm;
        
        public OperationWebResult(OperationWebParam p) {
            parm = p;
        }

        public String name() {
            return parm.name();
        }

        public String targetNamespace() {
            return parm.webResultTargetNamespace();
        }

        public boolean header() {
            return parm.header();
        }

        public String partName() {
            return parm.partName();
        }

        public Class<? extends Annotation> annotationType() {
            return WebResult.class;
        }
    }
    
    private class OperationWebParam implements WebParam {
        Mode md;
        String name;
        String targetNs;
        String webResultTNS;
        boolean isHeader;
        String partname;
        
        public OperationWebParam(String n, String pn, Mode m, String ns) {
            this(n, pn, m, ns, ns);
        }
        public OperationWebParam(String n, String pn, Mode m, String ns, String wrns) {
            name = n;
            md = m;
            targetNs = ns;
            partname = pn;
            webResultTNS = wrns;
        }

        public String name() {
            return name;
        }

        public Mode mode() {
            return md;
        }
        public void setMode(Mode m) {
            md = m;
        }


        public String targetNamespace() {
            return targetNs;
        }
        public String webResultTargetNamespace() {
            return webResultTNS;
        }

        public boolean header() {
            return isHeader;
        }
        public void setHeader(boolean b) {
            isHeader = b;
        }

        public String partName() {
            return partname;
        }

        public Class<? extends Annotation> annotationType() {
            return WebParam.class;
        }
        
    }
}
