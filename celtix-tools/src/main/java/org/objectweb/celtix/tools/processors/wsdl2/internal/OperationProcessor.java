package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaReturn;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.utils.ProcessorUtil;
import org.objectweb.celtix.tools.utils.SOAPBindingUtil;
import org.objectweb.celtix.tools.utils.StringUtils;

public class OperationProcessor  extends AbstractProcessor {
    private static final Logger LOG = LogUtils.getL7dLogger(OperationProcessor.class);
    
 
    private JavaParameter wrapperRequest;
    private JavaParameter wrapperResponse;

    public OperationProcessor(ProcessorEnvironment penv) {
        super(penv);
    }

    @SuppressWarnings("unchecked")
    public void process(JavaInterface intf, Operation operation) throws ToolException {
        JavaMethod method = new JavaMethod(intf);
        method.setName(ProcessorUtil.mangleNameToVariableName(operation.getName()));
        method.setOperationName(operation.getName());
        method.setStyle(operation.getStyle());
        if (method.getStyle() == null) {
            if (operation.getOutput() == null) {
                method.setStyle(OperationType.ONE_WAY);
            } else {
                method.setStyle(OperationType.REQUEST_RESPONSE);
            }
        }

        method.setWrapperStyle(isWrapperStyle(operation));
        method.setJAXWSBinding(customizing(intf, operation));
        processMethod(method, operation);
        Map<String, Fault> faults = operation.getFaults();
        FaultProcessor faultProcessor = new FaultProcessor(env);
        faultProcessor.process(method, faults);

        intf.addMethod(method);
    }

    @SuppressWarnings("unchecked")
    public void processMethod(JavaMethod method, Operation operation) throws ToolException {
        List<String> parameterOrder = operation.getParameterOrdering();
        Message inputMessage = operation.getInput() == null ? null : operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        if (inputMessage == null) {
            LOG.log(Level.WARNING, "NO_INPUT_MESSAGE",
                    new Object[] {operation.getName(), operation.getInput().getName()});
        }
        
        ParameterProcessor paramProcessor = new ParameterProcessor(env);
        method.clear();
        paramProcessor.process(method,
                               inputMessage,
                               outputMessage,
                               isRequestResponse(operation),
                               parameterOrder);
        isWrapperStyle(operation);        
        addWebMethodAnnotation(method);
        addWrapperAnnotation(method, operation);
        addWebResultAnnotation(method);
        addSOAPBindingAnnotation(method);
        if (!method.isOneWay() && method.getJAXWSBinding().isEnableAsyncMapping()) {
            addAsyncMethod(method);
        }
    }

    private void addSOAPBindingAnnotation(JavaMethod method) {
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle()) {
            JavaAnnotation bindingAnnotation = new JavaAnnotation("SOAPBinding");
            bindingAnnotation.addArgument("parameterStyle", SOAPBindingUtil.getBindingAnnotation("BARE"), "");
            method.addAnnotation("SOAPBinding", bindingAnnotation);
        }
    }

    private void addWebMethodAnnotation(JavaMethod method) {
        addWebMethodAnnotation(method, method.getOperationName());
    }
    
    private void addWebMethodAnnotation(JavaMethod method, String operationName) {
        JavaAnnotation methodAnnotation = new JavaAnnotation("WebMethod");
        methodAnnotation.addArgument("operationName", operationName);
        if (!StringUtils.isEmpty(method.getSoapAction())) {
            methodAnnotation.addArgument("action", method.getSoapAction());
        }
        method.addAnnotation("WebMethod", methodAnnotation);
        method.getInterface().addImport("javax.jws.WebMethod");
    }
    
    private void addWebResultAnnotation(JavaMethod method) {
        if (method.isOneWay()) {
            JavaAnnotation oneWayAnnotation = new JavaAnnotation("Oneway");
            method.addAnnotation("Oneway", oneWayAnnotation);
            method.getInterface().addImport("javax.jws.Oneway");
            return;
        }
        if ("void".equals(method.getReturn().getType())) {
            return;
        }
        JavaAnnotation resultAnnotation = new JavaAnnotation("WebResult");
        String targetNamespace = method.getReturn().getTargetNamespace();
        String name = "return";

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
            && !method.isWrapperStyle()) {
            name = method.getName() + "Response";
        }

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            name = method.getReturn().getName();
            targetNamespace = method.getInterface().getNamespace();
        }
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT) {
            if (method.getReturn().getQName() != null) {
                name = method.getReturn().getQName().getLocalPart();
            }
            targetNamespace = method.getReturn().getTargetNamespace();
        }
       
        resultAnnotation.addArgument("name", name);
        resultAnnotation.addArgument("targetNamespace", targetNamespace);
        
        
        
        if (method.getSoapStyle() == SOAPBinding.Style.RPC
            || (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle())) {
            resultAnnotation.addArgument("partName", method.getReturn().getName());
        }

        method.addAnnotation("WebResult", resultAnnotation);
        method.getInterface().addImport("javax.jws.WebResult");
    }
    
    protected void addWrapperAnnotation(JavaMethod method, Operation operation) {
        if (!isWrapperStyle(operation)) {
            return;
        }
        
        if (wrapperRequest != null) {
            JavaAnnotation wrapperRequestAnnotation = new JavaAnnotation("RequestWrapper");
            wrapperRequestAnnotation.addArgument("localName", wrapperRequest.getType());
            wrapperRequestAnnotation.addArgument("targetNamespace", wrapperRequest.getTargetNamespace());
            wrapperRequestAnnotation.addArgument("className", wrapperRequest.getClassName());
            method.addAnnotation("RequestWrapper", wrapperRequestAnnotation);
            method.getInterface().addImport("javax.xml.ws.RequestWrapper");
        }
        if (wrapperResponse != null) {
            JavaAnnotation wrapperResponseAnnotation = new JavaAnnotation("ResponseWrapper");
            wrapperResponseAnnotation.addArgument("localName", wrapperResponse.getType());
            wrapperResponseAnnotation.addArgument("targetNamespace", wrapperResponse.getTargetNamespace());
            wrapperResponseAnnotation.addArgument("className", wrapperResponse.getClassName());
            method.addAnnotation("ResponseWrapper", wrapperResponseAnnotation);
            method.getInterface().addImport("javax.xml.ws.ResponseWrapper");
        }

    }

    @SuppressWarnings("unchecked")
    private boolean isWrapperStyle(Operation operation) {

        Message inputMessage = operation.getInput() == null ? null : operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        Map<String, Part> inputParts = new HashMap<String, Part>();
        Map<String, Part> outputParts = new HashMap<String, Part>();
        
        if (inputMessage != null) {
            inputParts = inputMessage.getParts();
        }
        if (outputMessage != null) {
            outputParts = outputMessage.getParts();
        }
        
        //
        // RULE No.1:
        // The operation's input and output message (if present) each contain only a single part
        //
        if (inputParts.size() > 1 || outputParts.size() > 1) {
            return false;
        }

        //
        // RULE No.2:
        // The input message part refers to a global element decalration whose localname
        // is equal to the operation name
        //
        Part inputPart = null;
        if (inputParts.size() == 1) {
            inputPart = inputParts.values().iterator().next();
            if (inputPart != null) {
                QName inputElement = inputPart.getElementName();
                if (inputElement == null) {
                    return false;
                } else if (!operation.getName().equals(inputElement.getLocalPart())) {
                    return false;
                }
            }
        }
        //
        // RULE No.3:
        // The output message part refers to a global element decalration
        //
        Part outputPart = null;
        if (outputParts.size() == 1) {
            outputPart = outputParts.values().iterator().next();
            if (outputPart != null) {
                QName outputElement = outputPart.getElementName();
                if (outputElement == null) {
                    return false;
                }
            }
        }

        //
        // RULE No.4 and No5:
        // wrapper element should be pure complex type
        //
        if (ProcessorUtil.getBlock(inputPart, env) == null
            || ProcessorUtil.getBlock(outputPart, env) == null) {
            return false;
        }

        if (inputPart != null) {
            wrapperRequest = new JavaParameter();
            wrapperRequest.setName(ProcessorUtil.resolvePartName(inputPart));
            wrapperRequest.setType(ProcessorUtil.getPartType(inputPart));
            wrapperRequest.setTargetNamespace(ProcessorUtil.resolvePartNamespace(inputPart));

            wrapperRequest.setClassName(ProcessorUtil.getFullClzName(inputPart, this.env, this.collector));
                

        }
        if (outputPart != null) {
            wrapperResponse = new JavaParameter();
            wrapperResponse.setName(ProcessorUtil.resolvePartName(outputPart));
            wrapperResponse.setType(ProcessorUtil.getPartType(outputPart));
            wrapperResponse.setTargetNamespace(ProcessorUtil.resolvePartNamespace(outputPart));

            wrapperResponse.setClassName(ProcessorUtil.getFullClzName(outputPart, this.env, this.collector));

        }
        
        return true;
    }

    private boolean isRequestResponse(Operation operation) {
        if (operation.getStyle() == null) {
            return operation.getOutput() != null;
        }
        return OperationType.REQUEST_RESPONSE.equals(operation.getStyle());
    }

    private JAXWSBinding customizing(JavaInterface intf, Operation operation) {
        JAXWSBinding binding = null;
        List extElements = operation.getExtensibilityElements();
        if (extElements.size() > 0) {
            Iterator iterator = extElements.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof JAXWSBinding) {
                    binding = (JAXWSBinding) obj;
                }
            }
        } else {
            String portTypeName = intf.getWebServiceName();
            String operationName = operation.getName();
            binding = CustomizationParser.getInstance().getPortTypeOperationExtension(portTypeName,
                                                                                      operationName);
        }
               
        if (binding == null) {
            binding = new JAXWSBinding();
        }
        if (!binding.isSetAsyncMapping() && (intf.getJavaModel().getJAXWSBinding().isEnableAsyncMapping()
            || intf.getJAXWSBinding().isEnableAsyncMapping())) {
            binding.setEnableAsyncMapping(true);
        }
        return binding;
    }

    private void addAsyncMethod(JavaMethod method) throws ToolException {
        addPollingMethod(method);
        addCallbackMethod(method);

        method.getInterface().addImport("javax.xml.ws.AsyncHandler");
        method.getInterface().addImport("java.util.concurrent.Future");
        method.getInterface().addImport("javax.xml.ws.Response");
    }
    
    private void addPollingMethod(JavaMethod method) throws ToolException {
        JavaMethod pollingMethod = new JavaMethod(method.getInterface());
        pollingMethod.setName(method.getName() + ToolConstants.ASYNC_METHOD_SUFFIX);
        pollingMethod.setStyle(method.getStyle());
        pollingMethod.setWrapperStyle(method.isWrapperStyle());
        pollingMethod.setSoapAction(method.getSoapAction());
        
        JavaReturn future = new JavaReturn();
        future.setClassName("Future<?>");
        pollingMethod.setReturn(future);

        addWebMethodAnnotation(pollingMethod, method.getOperationName());
        pollingMethod.addAnnotation("ResponseWrapper", method.getAnnotationMap().get("ResponseWrapper"));
        pollingMethod.addAnnotation("RequestWrapper", method.getAnnotationMap().get("RequestWrapper"));
        pollingMethod.addAnnotation("SOAPBinding", method.getAnnotationMap().get("SOAPBinding"));
        
        for (Iterator iter = method.getParameters().iterator(); iter.hasNext();) {
            pollingMethod.addParameter((JavaParameter)iter.next());
        }

        JavaParameter asyncHandler = new JavaParameter();
        asyncHandler.setName("asyncHandler");
        asyncHandler.setClassName(getAsyncClassName(method, "AsyncHandler"));
        JavaAnnotation asyncHandlerAnnotation = new JavaAnnotation("WebParam");
        asyncHandlerAnnotation.addArgument("name", "asyncHandler");
        asyncHandlerAnnotation.addArgument("targetNamespace", "");
        asyncHandler.setAnnotation(asyncHandlerAnnotation);

        pollingMethod.addParameter(asyncHandler);

        method.getInterface().addMethod(pollingMethod);
    }

    private void addCallbackMethod(JavaMethod method) throws ToolException {
        JavaMethod callbackMethod = new JavaMethod(method.getInterface());
        callbackMethod.setName(method.getName() + ToolConstants.ASYNC_METHOD_SUFFIX);
        callbackMethod.setStyle(method.getStyle());
        callbackMethod.setWrapperStyle(method.isWrapperStyle());
        callbackMethod.setSoapAction(method.getSoapAction());
        
        JavaReturn response = new JavaReturn();
        response.setClassName(getAsyncClassName(method, "Response"));
        callbackMethod.setReturn(response);

        addWebMethodAnnotation(callbackMethod, method.getOperationName());
        callbackMethod.addAnnotation("RequestWrapper", method.getAnnotationMap().get("RequestWrapper"));
        callbackMethod.addAnnotation("ResponseWrapper", method.getAnnotationMap().get("ResponseWrapper"));
        callbackMethod.addAnnotation("SOAPBinding", method.getAnnotationMap().get("SOAPBinding"));

        for (Iterator iter = method.getParameters().iterator(); iter.hasNext();) {
            callbackMethod.addParameter((JavaParameter)iter.next());
        }

        method.getInterface().addMethod(callbackMethod);
    }

    private String getAsyncClassName(JavaMethod method, String clzName) {
        String response;
        if (wrapperResponse != null) {
            response = wrapperResponse.getClassName();
        } else {
            response = method.getReturn().getClassName();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(clzName);
        sb.append("<");
        sb.append(response);
        sb.append(">");
        return sb.toString();
    }
}
