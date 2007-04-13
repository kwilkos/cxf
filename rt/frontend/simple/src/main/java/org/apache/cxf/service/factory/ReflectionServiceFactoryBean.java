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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.helpers.MethodComparator;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
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
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.apache.ws.commons.schema.XmlSchemaSerializer.XmlSchemaSerializerException;
import org.apache.ws.commons.schema.utils.NamespaceMap;

/**
 * Introspects a class and builds a {@link Service} from it. If a WSDL URL is specified, 
 * a Service model will be directly from the WSDL and then metadata will be filled in 
 * from the service class. If no WSDL URL is specified, the Service will be constructed
 * directly from the class structure. 
 */
public class ReflectionServiceFactoryBean extends AbstractServiceFactoryBean {

    public static final String GENERIC_TYPE = "generic.type";
    public static final String MODE_OUT = "messagepart.mode.out";
    public static final String MODE_INOUT = "messagepart.mode.inout";
    public static final String HOLDER = "messagepart.isholder";
    public static final String HEADER = "messagepart.isheader";
    
    private static final Logger LOG = Logger.getLogger(ReflectionServiceFactoryBean.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ReflectionServiceFactoryBean.class);
    
    protected String wsdlURL;

    protected Class<?> serviceClass;
    
    private List<AbstractServiceConfiguration> serviceConfigurations = 
        new ArrayList<AbstractServiceConfiguration>();
    private QName serviceName;
    private Invoker invoker;
    private Executor executor;
    private List<String> ignoredClasses = new ArrayList<String>();
    private SimpleMethodDispatcher methodDispatcher = new SimpleMethodDispatcher();
    private Boolean wrappedStyle;
    private Map<String, Object> properties;
    private QName endpointName;
    private boolean populateFromClass;
    
    public ReflectionServiceFactoryBean() {
        getServiceConfigurations().add(0, new DefaultServiceConfiguration());
        setDataBinding(new JAXBDataBinding());
        
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

        initializeDefaultInterceptors();

        if (invoker != null) {
            getService().setInvoker(getInvoker());
        } else {
            getService().setInvoker(createInvoker());
        }
        
        if (getExecutor() != null) {
            getService().setExecutor(getExecutor());
        } 
        if (getDataBinding() != null) {
            getService().setDataBinding(getDataBinding());
        }   
        
        getService().put(MethodDispatcher.class.getName(), getMethodDispatcher());

        createEndpoints();
        
        return getService();
    }
    
    protected void createEndpoints() {
        Service service = getService();

        for (ServiceInfo inf : service.getServiceInfos()) {
            for (EndpointInfo ei : inf.getEndpoints()) {
                try {
                    Endpoint ep = createEndpoint(ei);
    
                    service.getEndpoints().put(ei.getName(), ep);
                } catch (EndpointException e) {
                    throw new ServiceConstructionException(e);
                }
            }
        }
    }

    public Endpoint createEndpoint(EndpointInfo ei) throws EndpointException {
        return new EndpointImpl(getBus(), getService(), ei);
    }

    protected void initializeServiceConfigurations() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            c.setServiceFactory(this);
        }
    }
    
    protected void buildServiceFromWSDL(String url) {
        LOG.info("Creating Service " + getServiceQName() + " from WSDL.");        
        
        WSDLServiceFactory factory = new WSDLServiceFactory(getBus(), url, getServiceQName());
        setService(factory.create());

        if (properties != null) {
            getService().putAll(properties);
        }
        
        initializeWSDLOperations();

        if (getDataBinding() != null) {
            getDataBinding().initialize(getService());
        }        
    }
    
    protected void buildServiceFromClass() {
        LOG.info("Creating Service " + getServiceQName() + " from class " + getServiceClass().getName());
        ServiceInfo serviceInfo = new ServiceInfo();
        ServiceImpl service = new ServiceImpl(serviceInfo);

        setService(service);  
        
        if (properties != null) {
            service.putAll(properties);
        }
        
        service.put(MethodDispatcher.class.getName(), getMethodDispatcher());
        
        serviceInfo.setName(getServiceQName());
        serviceInfo.setTargetNamespace(serviceInfo.getName().getNamespaceURI());
        
        createInterface(serviceInfo);

        getDataBinding().initialize(service);
        
        if (isWrapped()) {
            initializeWrappedElementNames(serviceInfo);
        }
        
        if (isWrapped()) {
            initializeWrappedSchema(serviceInfo);
        }      
    }

    protected void initializeServiceModel() {
        String wsdlurl = getWsdlURL();

        if (!populateFromClass && wsdlurl != null) {
            buildServiceFromWSDL(wsdlurl);
        } else {
            buildServiceFromClass();
        }
    }
    
    public boolean isPopulateFromClass() {
        return populateFromClass;
    }

    public void setPopulateFromClass(boolean fomClass) {
        this.populateFromClass = fomClass;
    }
    
    protected void initializeWSDLOperations() {
        Method[] methods = serviceClass.getMethods();
        Arrays.sort(methods, new MethodComparator());

        InterfaceInfo intf;
        if (getEndpointInfo() != null) {
            intf = getEndpointInfo().getInterface();
        } else {
            intf = getService().getServiceInfos().get(0).getInterface();
        }

        Map<QName, Method> validMethods = new HashMap<QName, Method>();
        for (Method m : methods) {
            if (isValidMethod(m)) {
                QName opName = getOperationName(intf, m);
                validMethods.put(opName, m);
            }
        }
            
        
        for (OperationInfo o : intf.getOperations()) {
            Method selected = null;
            for (Map.Entry<QName, Method> m : validMethods.entrySet()) {
                QName opName = m.getKey();

                if (o.getName().getNamespaceURI().equals(opName.getNamespaceURI())
                    && isMatchOperation(o.getName().getLocalPart(), opName.getLocalPart())) {
                    selected = m.getValue();
                    break;
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
                MessageInfo msg = new MessageInfo(op, uOp.getInput().getName());                
                op.setInput(uOp.getInputName(), msg);
                msg.addMessagePart(uOp.getInput().getName());
                
                for (MessagePartInfo p : uOp.getInput().getMessageParts()) {                    
                    p.setConcreteName(p.getName());
                }
            } 
            
            if (uOp.hasOutput()) {
                
                QName name = uOp.getOutput().getName();
                MessageInfo msg = new MessageInfo(op, name);
                op.setOutput(uOp.getOutputName(), msg);
                
                MessagePartInfo part = msg.addMessagePart(name);
                part.setIndex(-1);
                
                for (MessagePartInfo p : uOp.getOutput().getMessageParts()) {
                    p.setConcreteName(p.getName());
                }
            }
        } else {
            createMessageParts(intf, op, m);
        }

        getMethodDispatcher().bind(op, m);


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
        XmlSchemaCollection col = new XmlSchemaCollection();
        XmlSchema schema = new XmlSchema(getServiceNamespace(), col);
        schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
        serviceInfo.setXmlSchemaCollection(col);
        
        NamespaceMap nsMap = new NamespaceMap();
        nsMap.add("xsd", "http://www.w3.org/2001/XMLSchema");
        schema.setNamespaceContext(nsMap);
        
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.getUnwrappedOperation() != null) {
                if (op.hasInput()) {
                    createWrappedMessage(op.getInput(), op.getUnwrappedOperation().getInput(), schema);
                }
                if (op.hasOutput()) {
                    createWrappedMessage(op.getOutput(), op.getUnwrappedOperation().getOutput(), schema);
                }
            }
        }
        
        Document[] docs;
        try {
            docs = XmlSchemaSerializer.serializeSchema(schema, false);
        } catch (XmlSchemaSerializerException e1) {
            throw new ServiceConstructionException(e1);
        }
        Element e = docs[0].getDocumentElement();
        SchemaInfo schemaInfo = new SchemaInfo(serviceInfo, getServiceNamespace());
        schemaInfo.setElement(e);
        schemaInfo.setSchema(schema);
        serviceInfo.addSchema(schemaInfo);
    }

    private void createWrappedMessage(MessageInfo wrappedMessage, 
                                      MessageInfo unwrappedMessage, 
                                      XmlSchema schema) {
        XmlSchemaElement el = new XmlSchemaElement();
        el.setQName(wrappedMessage.getName());
        el.setName(wrappedMessage.getName().getLocalPart());
        schema.getItems().add(el);
        
        wrappedMessage.getMessageParts().get(0).setXmlSchema(el);
        
        XmlSchemaComplexType ct = new XmlSchemaComplexType(schema);
        el.setSchemaType(ct);
        
        XmlSchemaSequence seq = new XmlSchemaSequence();
        ct.setParticle(seq);
        
        for (MessagePartInfo mpi : unwrappedMessage.getMessageParts()) {
            el = new XmlSchemaElement();
            el.setName(mpi.getName().getLocalPart());
            el.setQName(mpi.getName());
            el.setMinOccurs(1);
            el.setMaxOccurs(1);
            el.setNillable(true);
            
            if (mpi.isElement()) {
                el.setRefName(mpi.getElementQName());
            } else {
                el.setSchemaTypeName(mpi.getTypeQName());
            }
            seq.getItems().add(el);
        }
    }

    protected void createMessageParts(InterfaceInfo intf, OperationInfo op, Method method) {
        final Class[] paramClasses = method.getParameterTypes();

        // Setup the input message
        MessageInfo inMsg = op.createMessage(getInputMessageName(op, method));        
        op.setInput(inMsg.getName().getLocalPart(), inMsg);

        for (int j = 0; j < paramClasses.length; j++) {
            if (isInParam(method, j)) {
                final QName q = getInParameterName(op, method, j);
                final QName q2 = getInPartName(op, method, j);
                MessagePartInfo part = inMsg.addMessagePart(q2);
                initializeParameter(part, paramClasses[j], method.getGenericParameterTypes()[j]);
                //TODO - RPC vs DOC (type vs element)
                if (isHeader(method, j)) {
                    part.setElementQName(q);
                    part.setProperty(HEADER, Boolean.TRUE);
                }
                part.setIndex(j);
            }
        }

        if (hasOutMessage(method)) {
            // Setup the output message
            MessageInfo outMsg = op.createMessage(createOutputMessageName(op, method));
            op.setOutput(outMsg.getName().getLocalPart(), outMsg);

            final Class<?> returnType = method.getReturnType();
            if (!returnType.isAssignableFrom(void.class)) {
                final QName q = getOutPartName(op, method, -1);                
                MessagePartInfo part = outMsg.addMessagePart(q);
                initializeParameter(part, method.getReturnType(), method.getGenericReturnType());
                part.setIndex(-1);
            }

            for (int j = 0; j < paramClasses.length; j++) {
                if (isOutParam(method, j)) {
                    final QName q = getOutPartName(op, method, j);
                    final QName q2 = getOutParameterName(op, method, j);
                    MessagePartInfo part = outMsg.addMessagePart(q);
                    initializeParameter(part, paramClasses[j], method.getGenericParameterTypes()[j]);
                    part.setIndex(j);
                    
                    if (isInParam(method, j)) {
                        part.setProperty(MODE_INOUT, Boolean.TRUE);
                    }
                    if (isHeader(method, j)) {
                        part.setElementQName(q2);
                        part.setProperty(HEADER, Boolean.TRUE);
                    }
                }
            }
        }

        initializeFaults(intf, op, method);
    }

    // TODO: Remove reference to JAX-WS holder if possible
    // We do need holder support in the simple frontend though as Aegis has its own
    // holder class. I'll tackle refactoring this into a more generic way of handling
    // holders in phase 2.
    protected void initializeParameter(MessagePartInfo part, Class rawClass, Type type) {
        if (rawClass.equals(Holder.class) && type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            rawClass = getHolderClass(paramType);
        }
        part.setProperty(GENERIC_TYPE, type);
        part.setTypeClass(rawClass);
        
    }

    protected Class getHolderClass(ParameterizedType paramType) {
        Object rawType = paramType.getActualTypeArguments()[0];
        Class rawClass;
        if (rawType instanceof GenericArrayType) {
            rawClass = (Class) ((GenericArrayType) rawType).getGenericComponentType();
            rawClass = Array.newInstance(rawClass, 0).getClass();
        } else {
            if (rawType instanceof ParameterizedType) {
                rawType = (Class) ((ParameterizedType) rawType).getRawType();
            }
            rawClass = (Class) rawType;
        }
        return rawClass;
    }
    
    public QName getServiceQName() { 
        if (serviceName == null) {
            serviceName = new QName(getServiceNamespace(), getServiceName());
        }

        return serviceName;
    }
    
    public QName getEndpointName() {
        if (endpointName != null) {
            return endpointName;
        }
        
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            QName name = c.getEndpointName();
            if (name != null) {
                endpointName = name;
                return name;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    public EndpointInfo getEndpointInfo() {
        return getService().getEndpointInfo(getEndpointName());
    }
    
    public void setEndpointName(QName en) {
        this.endpointName = en;
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
        if (serviceName != null) {
            return serviceName.getNamespaceURI();
        }
        
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

    protected QName getInputMessageName(final OperationInfo op, final Method method) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getInputMessageName(op, method);
            if (q != null) {                
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected QName createOutputMessageName(final OperationInfo op, final Method method) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getOutputMessageName(op, method);
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
        
        QName faultName = getFaultName(service, op, exClass, beanClass);
        FaultInfo fi = op.addFault(faultName, faultName);
        fi.setProperty(Class.class.getName(), exClass);
        
        MessagePartInfo mpi = fi.addMessagePart(faultName);
        mpi.setTypeClass(beanClass);
        
        return fi;
    }

    protected Class<?> getBeanClass(Class<?> exClass) {
        return exClass;
    }

    protected QName getFaultName(InterfaceInfo service, OperationInfo o, Class exClass, Class beanClass) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getFaultName(service, o, exClass, beanClass);
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

    public boolean isHeader(Method method, int j) {
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

    protected QName getInPartName(final OperationInfo op, 
                                  final Method method,
                                  final int paramNumber) {
        if (paramNumber == -1) {
            return null;
        }
        
        if (isWrapped(method)) {
            return getInParameterName(op, method, paramNumber);
        }
        
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getInPartName(op, method, paramNumber);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }
    
    protected QName getInParameterName(final OperationInfo op, 
                                       final Method method,
                                       final int paramNumber) {
        if (paramNumber == -1) {
            return null;
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

    protected QName getOutPartName(final OperationInfo op, 
                                   final Method method,
                                   final int paramNumber) {
        if (isWrapped(method)) {
            return getOutParameterName(op, method, paramNumber);
        }
        
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getOutPartName(op, method, paramNumber);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }
    
    protected Class getResponseWrapper(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Class cls = c.getResponseWrapper(selected);
            if (cls != null) {
                return cls;
            }
        }
        return null;
    }
    protected Class getRequestWrapper(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Class cls = c.getRequestWrapper(selected);
            if (cls != null) {
                return cls;
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

    public String getWsdlURL() {
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

    public void setWsdlURL(String wsdlURL) {
        this.wsdlURL = wsdlURL;
    }
    public void setWsdlURL(URL wsdlURL) {
        this.wsdlURL = wsdlURL.toString();
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
        if (this.wrappedStyle != null) {
            return this.wrappedStyle;
        }
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Boolean b = c.isWrapped();
            if (b != null) {
                return b.booleanValue();
            }
        }
        return true;
    }

    public String getStyle() {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            String style = c.getStyle();
            if (style != null) {
                return style;
            }
        }
        return "document";
    }

    public void setWrapped(boolean style) {
        this.wrappedStyle = style;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
}
