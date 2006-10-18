/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.service.factory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.MethodComparator;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.invoker.ApplicationScopePolicy;
import org.apache.cxf.service.invoker.FactoryInvoker;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.invoker.LocalFactory;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.TypeInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

/**
 * Introspects a class and builds a {@link Service} from it. If a WSDL URL is specified, 
 * a Service model will be directly from the WSDL and then metadata will be filled in 
 * from the service class. If no WSDL URL is specified, the Service will be constructed
 * directly from the class structure. 
 */
public class ReflectionServiceFactoryBean extends AbstractServiceFactoryBean {

    private static final Logger LOG = Logger.getLogger(ReflectionServiceFactoryBean.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReflectionServiceFactoryBean.class);
    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    
    protected URL wsdlURL;

    protected Class<?> serviceClass;
    
    private List<AbstractServiceConfiguration> serviceConfigurations = 
        new ArrayList<AbstractServiceConfiguration>();
    private QName serviceName;
    private Invoker invoker;
    private Executor executor;
    private List<String> ignoredClasses = new ArrayList<String>();
    private SimpleMethodDispatcher methodDispatcher = new SimpleMethodDispatcher();
    private boolean wrappedStyle = true;
    
    public ReflectionServiceFactoryBean() {
        getServiceConfigurations().add(0, new DefaultServiceConfiguration());

        ignoredClasses.add("java.lang.Object");
        ignoredClasses.add("java.lang.Throwable");
        ignoredClasses.add("org.omg.CORBA_2_3.portable.ObjectImpl");
        ignoredClasses.add("org.omg.CORBA.portable.ObjectImpl");
        ignoredClasses.add("javax.ejb.EJBObject");
        ignoredClasses.add("javax.rmi.CORBA.Stub");
    }

    @Override
    public Service create() {
        initializeServiceConfigurations();

        initializeServiceModel();

        initializeDataBindings();
        
        initializeDefaultInterceptors();

        if (invoker != null) {
            getService().setInvoker(getInvoker());
        } else {
            getService().setInvoker(createInvoker());
        }
        
        if (getExecutor() != null) {
            getService().setExecutor(getExecutor());
        } else {
            getService().setExecutor(new Executor() {
                public void execute(Runnable r) {
                    r.run();
                }
            });
        }

        getService().put(MethodDispatcher.class.getName(), getMethodDispatcher());

        createEndpoints();
        
        return getService();
    }
    
    protected void createEndpoints() {
        Service service = getService();

        for (EndpointInfo ei : service.getServiceInfo().getEndpoints()) {
            try {
                Endpoint ep = createEndpoint(ei);

                service.getEndpoints().put(ei.getName(), ep);
            } catch (EndpointException e) {
                throw new ServiceConstructionException(e);
            }
        }
    }

    protected Endpoint createEndpoint(EndpointInfo ei) throws EndpointException {
        return new EndpointImpl(getBus(), getService(), ei);
    }

    protected void initializeServiceConfigurations() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            c.setServiceFactory(this);
        }
    }

    protected void initializeServiceModel() {
        URL url = getWsdlURL();

        if (url != null) {
            LOG.info("Creating Service " + getServiceQName() + " from WSDL.");
            WSDLServiceFactory factory = new WSDLServiceFactory(getBus(), url, getServiceQName());

            setService(factory.create());

            initializeWSDLOperations();
        } else {
            LOG.info("Creating Service " + getServiceQName() + " from class " + getServiceClass().getName());
            // If we can't find the wsdlLocation, then we should build a service model ufrom the class.
            ServiceInfo serviceInfo = new ServiceInfo();
            ServiceImpl service = new ServiceImpl(serviceInfo);
            
            serviceInfo.setName(getServiceQName());
            
            createInterface(serviceInfo);

            if (wrappedStyle) {
                initializeWrappedElementNames(serviceInfo);
            }
            
            if (getDataBinding() != null) {
                getDataBinding().initialize(serviceInfo);
            }
            
            if (wrappedStyle) {
                initializeWrappedSchema(serviceInfo);
            }
            
            setService(service);
        }
    }

    protected void initializeWSDLOperations() {
        Method[] methods = serviceClass.getMethods();
        Arrays.sort(methods, new MethodComparator());

        InterfaceInfo intf = getService().getServiceInfo().getInterface();

        for (OperationInfo o : intf.getOperations()) {
            Method selected = null;
            for (Method m : methods) {
                if (isValidMethod(m)) {
                    QName opName = getOperationName(intf, m);

                    if (o.getName().getNamespaceURI().equals(opName.getNamespaceURI())
                        && isMatchOperation(o.getName().getLocalPart(), opName.getLocalPart())) {
                    //if (o.getName().equals(opName)) {
                        selected = m;
                        break;
                    }
                }
            }

            if (selected == null) {
                throw new ServiceConstructionException(new Message("NO_METHOD_FOR_OP", BUNDLE, o.getName()));
            }

            initializeWSDLOperation(intf, o, selected);
        }
    }

    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method selected) {
        // TODO Auto-generated method stub

    }

    protected Invoker createInvoker() {
        return new FactoryInvoker(new LocalFactory(getServiceClass()), 
                                  new ApplicationScopePolicy());
    }
    
    protected ServiceInfo createServiceInfo(InterfaceInfo intf) {
        ServiceInfo svcInfo = new ServiceInfo();
        svcInfo.setInterface(intf);

        return svcInfo;
    }

    protected InterfaceInfo createInterface(ServiceInfo serviceInfo) {
        QName intfName = getInterfaceName();
        InterfaceInfo intf = new InterfaceInfo(serviceInfo, intfName);

        Method[] methods = serviceClass.getMethods();

        // The BP profile states we can't have operations of the same name
        // so we have to append numbers to the name. Different JVMs sort methods
        // differently.
        // We need to keep them ordered so if we have overloaded methods, the
        // wsdl is generated the same every time across JVMs and across
        // client/servers.
        Arrays.sort(methods, new MethodComparator());

        for (Method m : serviceClass.getMethods()) {
            if (isValidMethod(m)) {
                createOperation(serviceInfo, intf, m);
            }
        }
        
        return intf;
    }

    protected OperationInfo createOperation(ServiceInfo serviceInfo, InterfaceInfo intf, Method m) {
        OperationInfo op = intf.addOperation(getOperationName(intf, m));

        if (isWrapped(m)) {
            UnwrappedOperationInfo uOp = new UnwrappedOperationInfo(op);
            op.setUnwrappedOperation(uOp);
            
            createMessageParts(intf, uOp, m);
            
            if (uOp.hasInput()) {
                MessageInfo msg = new MessageInfo(op, op.getName());
                op.setInput(uOp.getInputName(), msg);
                msg.addMessagePart(op.getName());
            } 
            
            if (uOp.hasOutput()) {
                QName name = new QName(op.getName().getNamespaceURI(), 
                                       op.getName().getLocalPart() + "Response");
                MessageInfo msg = new MessageInfo(op, name);
                op.setOutput(uOp.getOutputName(), msg);
                msg.addMessagePart(name);
            }
        } else {
            createMessageParts(intf, op, m);
        }

        methodDispatcher.bind(op, m);

        return op;
    }

    private void initializeWrappedElementNames(ServiceInfo serviceInfo) {
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.hasInput()) {
                setElementNameOnPart(op.getInput());
            }
            if (op.hasOutput()) {
                setElementNameOnPart(op.getOutput());
            }
        }
    }

    private void setElementNameOnPart(MessageInfo m) {
        List<MessagePartInfo> parts = m.getMessageParts();
        if (parts.size() == 1) {
            MessagePartInfo p = parts.get(0);
            p.setElement(true);
            p.setElementQName(m.getName());
        }
    }

    protected void initializeWrappedSchema(ServiceInfo serviceInfo) {
        Document d = DOMUtils.createDocument();
        
        Element schema = d.createElementNS(XSD_NS, "xsd:schema");
        d.appendChild(schema);
        schema.setAttribute("targetNamespace", getServiceNamespace());
        
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.hasInput()) {
                createWrappedMessage(op.getInput(), op.getUnwrappedOperation().getInput(), d, schema);
            }
            if (op.hasOutput()) {
                createWrappedMessage(op.getOutput(), op.getUnwrappedOperation().getOutput(), d, schema);
            }
        }
        
        TypeInfo typeInfo = serviceInfo.getTypeInfo();
        if (typeInfo == null) {
            typeInfo = new TypeInfo(serviceInfo);
            serviceInfo.setTypeInfo(typeInfo);
        }
        
        SchemaInfo schemaInfo = new SchemaInfo(typeInfo, getServiceNamespace());
        schemaInfo.setElement(schema);
        typeInfo.addSchema(schemaInfo);
    }

    private void createWrappedMessage(MessageInfo wrappedMessage, 
                                      MessageInfo unwrappedMessage, 
                                      Document d, 
                                      Element schema) {
        Element el = d.createElementNS(XSD_NS, "xsd:element");
        el.setAttribute("name", wrappedMessage.getName().getLocalPart());
        schema.appendChild(el);
        
        Element ct = d.createElementNS(XSD_NS, "xsd:complexType");
        el.appendChild(ct);
        
        Element seq = d.createElementNS(XSD_NS, "xsd:sequence");
        ct.appendChild(seq);
        
        
        for (MessagePartInfo mpi : unwrappedMessage.getMessageParts()) {
            el = d.createElementNS(XSD_NS, "xsd:element");
            el.setAttribute("name", mpi.getName().getLocalPart());
            el.setAttribute("minOccurs", "1");
            el.setAttribute("maxOccurs", "1");
            
            if (mpi.isElement()) {
                String ns = mpi.getElementQName().getNamespaceURI();
                String prefix = DOMUtils.getPrefixRecursive(el, ns);
                if (prefix == null) {
                    prefix = DOMUtils.createNamespace(schema, ns);
                }
                el.setAttribute("ref", prefix + ":" + mpi.getElementQName().getLocalPart());
            } else {
                String ns = mpi.getTypeQName().getNamespaceURI();
                String prefix = DOMUtils.getPrefixRecursive(el, ns);
                if (prefix == null) {
                    prefix = DOMUtils.createNamespace(schema, ns);
                }
                el.setAttribute("type", prefix + ":" + mpi.getTypeQName().getLocalPart());
            }
            seq.appendChild(el);
        }
    }

    protected void createMessageParts(InterfaceInfo intf, OperationInfo op, Method method) {
        final Class[] paramClasses = method.getParameterTypes();

        // Setup the input message
        MessageInfo inMsg = op.createMessage(getInputMessageName(op));
        op.setInput(inMsg.getName().getLocalPart(), inMsg);

        for (int j = 0; j < paramClasses.length; j++) {
            if (!isHeader(method, j) && isInParam(method, j)) {
                final QName q = getInParameterName(op, method, j);
                MessagePartInfo part = inMsg.addMessagePart(q);
                part.setProperty(Class.class.getName(), paramClasses[j]);
            }
        }

        if (hasOutMessage(method)) {
            // Setup the output message
            MessageInfo outMsg = op.createMessage(createOutputMessageName(op));
            op.setOutput(outMsg.getName().getLocalPart(), outMsg);

            final Class<?> returnType = method.getReturnType();
            if (!returnType.isAssignableFrom(void.class) && !isHeader(method, -1)) {
                final QName q = getOutParameterName(op, method, -1);
                MessagePartInfo part = outMsg.addMessagePart(q);
                part.setProperty(Class.class.getName(), method.getReturnType());
            }

            for (int j = 0; j < paramClasses.length; j++) {
                if (!paramClasses[j].equals(MessageContext.class) && !isHeader(method, j)
                    && isOutParam(method, j)) {
                    final QName q = getInParameterName(op, method, j);
                    MessagePartInfo part = outMsg.addMessagePart(q);
                    part.setProperty(Class.class.getName(), paramClasses[j]);
                }
            }
        }

        initializeFaults(intf, op, method);
    }

    protected QName getServiceQName() { 
        if (serviceName == null) {
            serviceName = new QName(getServiceNamespace(), getServiceName());
        }

        return serviceName;
    }
    
    protected QName getEndpointName() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            QName name = c.getEndpointName();
            if (name != null) {
                return name;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected String getServiceName() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            String name = c.getServiceName();
            if (name != null) {
                return name;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected String getServiceNamespace() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            String name = c.getServiceNamespace();
            if (name != null) {
                return name;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected QName getInterfaceName() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            QName name = c.getInterfaceName();
            if (name != null) {
                return name;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected boolean isValidMethod(final Method method) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Boolean b = c.isOperation(method);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }
    
    protected boolean isWrapped(final Method method) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Boolean b = c.isWrapped(method);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    protected boolean isMatchOperation(String methodNameInClass, String methodNameInWsdl) {
        // TODO: This seems wrong and not sure who put it here. Will revisit - DBD
        boolean ret = false;
        String initOfMethodInClass = methodNameInClass.substring(0, 1);
        String initOfMethodInWsdl = methodNameInWsdl.substring(0, 1);
        if (initOfMethodInClass.equalsIgnoreCase(initOfMethodInWsdl)
            && methodNameInClass.substring(1, methodNameInClass.length()).equals(
                methodNameInWsdl.substring(1, methodNameInWsdl.length()))) {
            ret = true;
        }
        return ret;
    }
    
    protected boolean isOutParam(Method method, int j) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            Boolean b = c.isOutParam(method, j);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    protected boolean isInParam(Method method, int j) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            Boolean b = c.isInParam(method, j);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    protected QName getInputMessageName(final OperationInfo op) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getInputMessageName(op);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected QName createOutputMessageName(final OperationInfo op) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getOutputMessageName(op);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected boolean hasOutMessage(Method m) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            Boolean b = c.hasOutMessage(m);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    protected void initializeFaults(final InterfaceInfo service, 
                                    final OperationInfo op, 
                                    final Method method) {
        // Set up the fault messages
        final Class[] exceptionClasses = method.getExceptionTypes();
        for (int i = 0; i < exceptionClasses.length; i++) {
            Class exClazz = exceptionClasses[i];

            // Ignore XFireFaults because they don't need to be declared
            if (exClazz.equals(Exception.class) 
                || Fault.class.isAssignableFrom(exClazz)
                || exClazz.equals(RuntimeException.class)
                || exClazz.equals(Throwable.class)) {
                continue;
            }

            addFault(service, op, exClazz);
        }
    }

    protected FaultInfo addFault(final InterfaceInfo service, final OperationInfo op, Class exClass) {
        Class beanClass = getBeanClass(exClass);
        
        QName faultName = getFaultName(service, op, getBeanClass(exClass), getBeanClass(beanClass));
        FaultInfo fi = op.addFault(faultName, faultName);
        fi.setProperty(Class.class.getName(), exClass);
        
        MessagePartInfo mpi = fi.addMessagePart(faultName);
        mpi.setProperty(Class.class.getName(), beanClass);
        
        return fi;
    }

    protected Class getBeanClass(Class exClass) {
        return exClass;
    }

    protected QName getFaultName(InterfaceInfo service, OperationInfo o, Class exClass, Class beanClass) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getFaultName(service, o, getBeanClass(exClass), getBeanClass(beanClass));
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected String getAction(OperationInfo op) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            String s = c.getAction(op);
            if (s != null) {
                return s;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected boolean isHeader(Method method, int j) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            Boolean b = c.isHeader(method, j);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    /**
     * Creates a name for the operation from the method name. If an operation
     * with that name already exists, a name is create by appending an integer
     * to the end. I.e. if there is already two methods named
     * <code>doSomething</code>, the first one will have an operation name of
     * "doSomething" and the second "doSomething1".
     * 
     * @param service
     * @param method
     */
    protected QName getOperationName(InterfaceInfo service, Method method) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName s = c.getOperationName(service, method);
            if (s != null) {
                return s;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected boolean isAsync(final Method method) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            Boolean b = c.isAsync(method);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    protected QName getInParameterName(final OperationInfo op, 
                                       final Method method,
                                       final int paramNumber) {
        if (paramNumber == -1) {
            throw new RuntimeException();
        }
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getInParameterName(op, method, paramNumber);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected QName getOutParameterName(final OperationInfo op, 
                                        final Method method,
                                        final int paramNumber) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getOutParameterName(op, method, paramNumber);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }


    protected Class getResponseWrapper(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Class cls = c.getResponseWrapper(selected);
            if (getBeanClass(cls) != null) {
                return getBeanClass(cls);
            }
        }
        return null;
    }
    protected Class getRequestWrapper(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Class cls = c.getRequestWrapper(selected);
            if (getBeanClass(cls) != null) {
                return getBeanClass(cls);
            }
        }
        return null;
    }

    protected SimpleMethodDispatcher getMethodDispatcher() {
        return methodDispatcher;
    }

    public List<AbstractServiceConfiguration> getConfigurations() {
        return serviceConfigurations;
    }

    public void setConfigurations(List<AbstractServiceConfiguration> configurations) {
        this.serviceConfigurations = configurations;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public URL getWsdlURL() {
        if (wsdlURL == null) {
            for (AbstractServiceConfiguration c : serviceConfigurations) {
                wsdlURL = c.getWsdlURL();
                if (wsdlURL != null) {
                    break;
                }
            }
        }
        return wsdlURL;
    }

    public void setWsdlURL(URL wsdlURL) {
        this.wsdlURL = wsdlURL;
    }

    public List<AbstractServiceConfiguration> getServiceConfigurations() {
        return serviceConfigurations;
    }

    public void setServiceConfigurations(List<AbstractServiceConfiguration> serviceConfigurations) {
        this.serviceConfigurations = serviceConfigurations;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public List<String> getIgnoredClasses() {
        return ignoredClasses;
    }

    public void setIgnoredClasses(List<String> ignoredClasses) {
        this.ignoredClasses = ignoredClasses;
    }

    public boolean isWrapped() {
        return wrappedStyle;
    }

    public void setWrapped(boolean style) {
        this.wrappedStyle = style;
    }
    
}
