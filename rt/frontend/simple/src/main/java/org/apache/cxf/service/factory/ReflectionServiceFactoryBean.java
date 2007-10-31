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

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.source.mime.MimeAttribute;
import org.apache.cxf.databinding.source.mime.MimeSerializer;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.frontend.FaultInfoException;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.helpers.MethodComparator;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.FaultOutInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.invoker.ApplicationScopePolicy;
import org.apache.cxf.service.invoker.FactoryInvoker;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.invoker.LocalFactory;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.cxf.wsdl11.WSDLServiceFactory;
import org.apache.ws.commons.schema.ValidationEventHandler;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;

/**
 * Introspects a class and builds a {@link Service} from it. If a WSDL URL is
 * specified, a Service model will be directly from the WSDL and then metadata
 * will be filled in from the service class. If no WSDL URL is specified, the
 * Service will be constructed directly from the class structure.
 */
public class ReflectionServiceFactoryBean extends AbstractServiceFactoryBean {

    public static final String ENDPOINT_CLASS = "endpoint.class";
    public static final String GENERIC_TYPE = "generic.type";
    public static final String MODE_OUT = "messagepart.mode.out";
    public static final String MODE_INOUT = "messagepart.mode.inout";
    public static final String HOLDER = "messagepart.isholder";
    public static final String HEADER = "messagepart.isheader";
    public static final String ELEMENT_NAME = "messagepart.elementName";
    public static final String METHOD = "operation.method";
    public static final String METHOD_PARAM_ANNOTATIONS = "method.parameters.annotations";
    public static final String METHOD_ANNOTATONS = "method.return.annotations";

    private static final Logger LOG = LogUtils.getL7dLogger(ReflectionServiceFactoryBean.class,
                                                            "SimpleMessages");

    protected String wsdlURL;

    protected Class<?> serviceClass;

    private List<AbstractServiceConfiguration> serviceConfigurations =
        new ArrayList<AbstractServiceConfiguration>();
    private QName serviceName;
    private Invoker invoker;
    private Executor executor;
    private List<String> ignoredClasses = new ArrayList<String>();
    private List<Method> ignoredMethods = new ArrayList<Method>();
    private SimpleMethodDispatcher methodDispatcher = new SimpleMethodDispatcher();
    private Boolean wrappedStyle;
    private Map<String, Object> properties;
    private QName endpointName;
    private boolean populateFromClass;
    private boolean anonymousWrappers;
    private boolean qualifiedSchemas = true;


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
//        System.setProperty(Constants.SystemConstants.EXTENSION_REGISTRY_KEY,
//                           CustomExtensionRegistry.class.getName());

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
        
        fillInSchemaCrossreferences();

        return getService();
    }
    
    /**
     * Code elsewhere in this function will fill in the name of the type of an element but not the reference
     * to the type. This function fills in the type references.
     * 
     * This does not set the type reference for elements that are declared as refs to other elements.
     * It is a giant pain to find them, since they are not (generally) root elements and the code would
     * have to traverse all the types to find all of them. Users should look them up through the collection,
     * that's what it is for.
     */
    private void fillInSchemaCrossreferences() {
        Service service = getService();
        for (ServiceInfo serviceInfo : service.getServiceInfos()) {
            XmlSchemaCollection schemaCollection = serviceInfo.getXmlSchemaCollection();
            
            // First pass, fill in any types for which we have a name but no type.
            for (SchemaInfo schemaInfo : serviceInfo.getSchemas()) {
                XmlSchemaObjectTable elementsTable = schemaInfo.getSchema().getElements();
                Iterator elementsIterator = elementsTable.getNames();
                while (elementsIterator.hasNext()) {
                    QName elementName = (QName)elementsIterator.next();
                    XmlSchemaElement element = schemaInfo.getSchema().getElementByName(elementName);
                    if (element.getSchemaType() == null) {
                        QName typeName = element.getSchemaTypeName();
                        if (typeName != null) {
                            XmlSchemaType type = schemaCollection.getTypeByQName(typeName);
                            if (type == null) {
                                Message message = new Message("REFERENCE_TO_UNDEFINED_TYPE", LOG, 
                                                              element.getQName(),
                                                              typeName,
                                                              service.getName());
                                LOG.severe(message.toString());
                            } else {
                                element.setSchemaType(type);
                            }
                        }
                    }
                }
                
            }
        }
    }

    protected void createEndpoints() {
        Service service = getService();

        BindingFactoryManager bfm = getBus().getExtension(BindingFactoryManager.class);
        
        for (ServiceInfo inf : service.getServiceInfos()) {
            for (EndpointInfo ei : inf.getEndpoints()) {
                
                try {
                    bfm.getBindingFactory(ei.getBinding().getBindingId());
                } catch (BusException e1) {
                    continue;
                }
                
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
        LOG.info("Creating Service " + getServiceQName() + " from WSDL: " + url);
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
        
        if (Proxy.isProxyClass(this.getServiceClass())) {
            LOG.log(Level.WARNING, "USING_PROXY_FOR_SERVICE", getServiceClass());
        }
        
        ServiceInfo serviceInfo = new ServiceInfo();
        XmlSchemaCollection col = serviceInfo.getXmlSchemaCollection();
        col.getExtReg().registerSerializer(MimeAttribute.class, new MimeSerializer());

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

        boolean isWrapped = isWrapped();
        if (isWrapped) {
            initializeWrappedSchema(serviceInfo);
        }

        for (OperationInfo opInfo : serviceInfo.getInterface().getOperations()) {
            Method m = (Method)opInfo.getProperty(METHOD);

            if (!isWrapped(m) && !isRPC(m) && opInfo.getInput() != null) {
                createBareMessage(serviceInfo, opInfo, false);
            }

            if (!isWrapped(m) && !isRPC(m) && opInfo.getOutput() != null) {
                createBareMessage(serviceInfo, opInfo, true);
            }

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

    protected InterfaceInfo getInterfaceInfo() {
        if (getEndpointInfo() != null) {
            return getEndpointInfo().getInterface();
        }
        QName qn = this.getInterfaceName();
        for (ServiceInfo si : getService().getServiceInfos()) {
            if (qn.equals(si.getInterface().getName())) {
                return si.getInterface();
            }
        }
        throw new ServiceConstructionException(new Message("COULD_NOT_FIND_PORTTYPE", LOG, qn));
    }

    protected void initializeWSDLOperations() {
        List<OperationInfo> removes = new ArrayList<OperationInfo>();
        Method[] methods = serviceClass.getMethods();
        Arrays.sort(methods, new MethodComparator());

        InterfaceInfo intf = getInterfaceInfo();

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
                LOG.log(Level.WARNING, "NO_METHOD_FOR_OP", o.getName());
                removes.add(o);
            } else {
                initializeWSDLOperation(intf, o, selected);
            }
        }
        for (OperationInfo op : removes) {
            intf.removeOperation(op);
        }

        //Some of the operations may have switched from unwrapped to wrapped.  Update the bindings.
        for (ServiceInfo service : getService().getServiceInfos()) {
            for (BindingInfo bi : service.getBindings()) {
                List<BindingOperationInfo> biremoves = new ArrayList<BindingOperationInfo>();
                for (BindingOperationInfo binfo : bi.getOperations()) {
                    if (removes.contains(binfo.getOperationInfo())) {
                        biremoves.add(binfo); 
                    } else {
                        binfo.updateUnwrappedOperation();
                    }
                }
                for (BindingOperationInfo binfo : biremoves) {
                    bi.removeOperation(binfo);
                }
            }
        }
    }

    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method selected) {
        // TODO Auto-generated method stub

    }

    protected Invoker createInvoker() {
        return new FactoryInvoker(new LocalFactory(getServiceClass()), new ApplicationScopePolicy());
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

        for (Method m : methods) {
            if (isValidMethod(m)) {
                createOperation(serviceInfo, intf, m);
            }
        }

        return intf;
    }

    protected OperationInfo createOperation(ServiceInfo serviceInfo, InterfaceInfo intf, Method m) {
        OperationInfo op = intf.addOperation(getOperationName(intf, m));
        op.setProperty(m.getClass().getName(), m);
        op.setProperty("action", getAction(op, m));

        if (isWrapped(m)) {
            UnwrappedOperationInfo uOp = new UnwrappedOperationInfo(op);
            op.setUnwrappedOperation(uOp);

            createMessageParts(intf, uOp, m);

            if (uOp.hasInput()) {
                MessageInfo msg = new MessageInfo(op, uOp.getInput().getName());
                op.setInput(uOp.getInputName(), msg);

                createInputWrappedMessageParts(uOp, m, msg);

                for (MessagePartInfo p : uOp.getInput().getMessageParts()) {
                    p.setConcreteName(p.getName());
                }
            }

            if (uOp.hasOutput()) {

                QName name = uOp.getOutput().getName();
                MessageInfo msg = new MessageInfo(op, name);
                op.setOutput(uOp.getOutputName(), msg);

                createOutputWrappedMessageParts(uOp, m, msg);

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

    protected void initializeWrappedSchema(ServiceInfo serviceInfo) {
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.getUnwrappedOperation() != null) {
                if (op.hasInput()) { 
                    if (op.getInput().getMessageParts().get(0).getTypeClass() == null) {
                    
                        QName wrapperBeanName = op.getInput().getMessageParts().get(0).getElementQName();
                        XmlSchemaElement e = null;
                        for (SchemaInfo s : serviceInfo.getSchemas()) {
                            e = s.getElementByQName(wrapperBeanName);
                            if (e != null) {
                                op.getInput().getMessageParts().get(0).setXmlSchema(e);
                                break;
                            }
                        }
                        if (e == null) {
                            createWrappedSchema(serviceInfo, op.getInput(),
                                                op.getUnwrappedOperation().getInput(), wrapperBeanName);
                        }
                    }

                    for (MessagePartInfo mpi : op.getInput().getMessageParts()) {
                        if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                            QName qn = (QName)mpi.getProperty(ELEMENT_NAME);
                            mpi.setElement(true);
                            mpi.setElementQName(qn);


                            checkForElement(serviceInfo, mpi);
                        }
                    }


                }
                if (op.hasOutput()) {
                    if (op.getOutput().getMessageParts().get(0).getTypeClass() == null) {
                    
                        QName wrapperBeanName = op.getOutput().getMessageParts().get(0).getElementQName();
                        XmlSchemaElement e = null;
                        for (SchemaInfo s : serviceInfo.getSchemas()) {
                            e = s.getElementByQName(wrapperBeanName);
                            if (e != null) {
                                break;
                            }
                        }
                        if (e == null) {
                            createWrappedSchema(serviceInfo, op.getOutput(), op.getUnwrappedOperation()
                                .getOutput(), wrapperBeanName);
                        }
                    }
                    for (MessagePartInfo mpi : op.getOutput().getMessageParts()) {
                        if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                            QName qn = (QName)mpi.getProperty(ELEMENT_NAME);
                            mpi.setElement(true);
                            mpi.setElementQName(qn);

                            checkForElement(serviceInfo, mpi);
                        }
                    }
                }
                if (op.hasFaults()) {
                    //check to make sure the faults are elements
                    for (FaultInfo fault : op.getFaults()) {
                        QName qn = (QName)fault.getProperty("elementName");
                        MessagePartInfo part = fault.getMessagePart(0);
                        if (!part.isElement()) {
                            part.setElement(true);
                            part.setElementQName(qn);
                            checkForElement(serviceInfo, part);
                        }
                    }
                }
            }
        }

    }

    protected void checkForElement(ServiceInfo serviceInfo, MessagePartInfo mpi) {
        for (SchemaInfo s : serviceInfo.getSchemas()) {
            XmlSchemaElement e = s.getElementByQName(mpi.getElementQName());
            if (e != null) {
                return;
            }
        }
        SchemaInfo si = getOrCreateSchema(serviceInfo, 
                                          mpi.getElementQName().getNamespaceURI(),
                                          getQualifyWrapperSchema());
        XmlSchema schema = si.getSchema();

        XmlSchemaElement el = new XmlSchemaElement();
        el.setQName(mpi.getElementQName());
        el.setName(mpi.getElementQName().getLocalPart());
        if (!isExistSchemaElement(schema, mpi.getElementQName())) {
            schema.getItems().add(el);
        }
        el.setMinOccurs(1);
        el.setMaxOccurs(0);
        el.setNillable(true);

        XmlSchemaType tp = (XmlSchemaType)mpi.getXmlSchema();
        el.setSchemaTypeName(tp.getQName());
    }

    public boolean getAnonymousWrapperTypes() {
        return anonymousWrappers;
    }
    public boolean isAnonymousWrapperTypes() {
        return anonymousWrappers;
    }
    public void setAnonymousWrapperTypes(boolean b) {
        anonymousWrappers = b;
    }
    
    public boolean getQualifyWrapperSchema() {
        return qualifiedSchemas;
    }
    public boolean isQualifyWrapperSchema() {
        return qualifiedSchemas;
    }
    public void setQualifyWrapperSchema(boolean b) {
        qualifiedSchemas = b;
    }
    
    

    
    
    protected void createWrappedSchema(ServiceInfo serviceInfo, AbstractMessageContainer wrappedMessage,
                                       AbstractMessageContainer unwrappedMessage, QName wrapperBeanName) {
        SchemaInfo schemaInfo = getOrCreateSchema(serviceInfo,
                                                  wrapperBeanName.getNamespaceURI(),
                                                  getQualifyWrapperSchema());

        createWrappedMessageSchema(serviceInfo, wrappedMessage, unwrappedMessage,
                                   schemaInfo.getSchema(), wrapperBeanName);
    }

    protected void createBareMessage(ServiceInfo serviceInfo,
                                     OperationInfo opInfo, boolean isOut) {

        SchemaInfo schemaInfo = null;
        XmlSchema schema = null;
        MessageInfo message = isOut ?  opInfo.getOutput() : opInfo.getInput();

        if (message.getMessageParts().size() == 0) {
            return;
        }

        Method method = (Method)opInfo.getProperty(METHOD);
        int paraNumber = 0;
        for (MessagePartInfo mpi : message.getMessageParts()) {
            QName qname = (QName)mpi.getProperty(ELEMENT_NAME);
            if (message.getMessageParts().size() == 1) {
                qname = qname == null && !isOut ? getInParameterName(opInfo, method, -1) : qname;
                qname = qname == null && isOut ? getOutParameterName(opInfo, method, -1) : qname;
                if (qname.getLocalPart().startsWith("arg") || qname.getLocalPart().startsWith("return")) {
                    qname = isOut
                        ? new QName(qname.getNamespaceURI(), method.getName() + "Response") : new QName(qname
                            .getNamespaceURI(), method.getName());
                }

            }

            if (isOut && message.getMessageParts().size() > 1 && qname == null) {
                while (!isOutParam(method, paraNumber)) {
                    paraNumber++;
                }
                qname = getOutParameterName(opInfo, method, paraNumber);
            } else if (qname == null) {
                qname = getInParameterName(opInfo, method, paraNumber);
            }

            for (SchemaInfo s : serviceInfo.getSchemas()) {
                if (s.getNamespaceURI().equals(qname.getNamespaceURI())) {
                    schemaInfo = s;
                    break;
                }
            }

            if (schemaInfo == null) {
                schemaInfo = getOrCreateSchema(serviceInfo, qname.getNamespaceURI(), true);
                schema = schemaInfo.getSchema();
            } else {
                schema = schemaInfo.getSchema();
                if (schema != null && schema.getElementByName(qname) != null) {
                    mpi.setElement(true);
                    mpi.setElementQName(qname);
                    paraNumber++;
                    continue;
                }
            }

            XmlSchemaElement el = new XmlSchemaElement();
            el.setQName(qname);
            el.setName(qname.getLocalPart());
            el.setMinOccurs(1);
            el.setMaxOccurs(0);
            el.setNillable(true);
            
            if (!isExistSchemaElement(schema, qname)) {
                schema.getItems().add(el);
                schema.getElements().add(qname, el);
            } else {
                el = getExistingSchemaElement(schema, qname);    
            }

            if (mpi.isElement()) {
                XmlSchemaElement oldEl = (XmlSchemaElement)mpi.getXmlSchema();
                if (!oldEl.getQName().equals(qname)) {
                    el.setSchemaTypeName(oldEl.getSchemaTypeName());
                    el.setSchemaType(oldEl.getSchemaType());
                }
                mpi.setXmlSchema(el);
                mpi.setElementQName(qname);
                mpi.setConcreteName(qname);
                continue;
            } else {
                el.setSchemaTypeName(mpi.getTypeQName());
                String ns = mpi.getTypeQName().getNamespaceURI();
                if (!ns.equals(schema.getTargetNamespace()) && !ns.equals(WSDLConstants.NU_SCHEMA_XSD)) {
                    XmlSchemaImport is = new XmlSchemaImport();
                    is.setNamespace(ns);
                    if (!isExistImport(schema, ns)) {
                        schema.getItems().add(is);
                    }
                }
            }

            schemaInfo.setSchema(schema);

            mpi.setElement(true);
            mpi.setElementQName(qname);
            paraNumber++;
        }
    }

    private boolean isExistImport(XmlSchema schema, String ns) {
        boolean isExist = false;

        for (Iterator ite = schema.getItems().getIterator(); ite.hasNext();) {
            XmlSchemaObject obj = (XmlSchemaObject)ite.next();
            if (obj instanceof XmlSchemaImport) {
                XmlSchemaImport xsImport = (XmlSchemaImport)obj;
                if (xsImport.getNamespace().equals(ns)) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;

    }

    private XmlSchemaElement getExistingSchemaElement(XmlSchema schema, QName qn) {
        for (Iterator ite = schema.getItems().getIterator(); ite.hasNext();) {
            XmlSchemaObject obj = (XmlSchemaObject)ite.next();
            if (obj instanceof XmlSchemaElement) {
                XmlSchemaElement xsEle = (XmlSchemaElement)obj;
                if (xsEle.getQName().equals(qn)) {
                    return xsEle;
                }
            }
        }
        return null;
    }
    private boolean isExistSchemaElement(XmlSchema schema, QName qn) {
        return getExistingSchemaElement(schema, qn) != null;
    }


    private void createWrappedMessageSchema(ServiceInfo serviceInfo,
                                            AbstractMessageContainer wrappedMessage,
                                            AbstractMessageContainer unwrappedMessage,
                                            XmlSchema schema,
                                            QName wrapperName) {
        
        XmlSchemaElement el = new XmlSchemaElement();
        el.setQName(wrapperName);
        el.setName(wrapperName.getLocalPart());
        schema.getItems().add(el);

        wrappedMessage.getMessageParts().get(0).setXmlSchema(el);

        XmlSchemaComplexType ct = new XmlSchemaComplexType(schema);
        
        if (!isAnonymousWrapperTypes()) {
            ct.setName(wrapperName.getLocalPart());
            el.setSchemaTypeName(wrapperName);
            schema.addType(ct);
            schema.getItems().add(ct);
        }
        el.setSchemaType(ct);


        XmlSchemaSequence seq = new XmlSchemaSequence();
        ct.setParticle(seq);

        for (MessagePartInfo mpi : unwrappedMessage.getMessageParts()) {
            el = new XmlSchemaElement();
            el.setName(mpi.getName().getLocalPart());
            el.setQName(mpi.getName());
            if (mpi.isElement()) {
                el.setRefName(mpi.getElementQName());
            } else {
                el.setSchemaTypeName(mpi.getTypeQName());
                el.setSchemaType((XmlSchemaType)mpi.getXmlSchema());
                if (schema.getElementFormDefault().getValue().equals(XmlSchemaForm.UNQUALIFIED)) {
                    mpi.setConcreteName(new QName(null, mpi.getName().getLocalPart()));
                }
            }
            if (!Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                if (!mpi.isElement()) {
                    mpi.setXmlSchema(el);                    
                }
                Annotation[] parameterAnnotation = getMethodParameterAnnotations(mpi);
                if (parameterAnnotation != null) {
                    addMimeType(el, parameterAnnotation);
                }
                
                Annotation[] methodAnnotations = getMethodAnnotations(mpi);
                if (methodAnnotations != null) {
                    addMimeType(el, methodAnnotations);
                }                
                                
                if (mpi.getTypeClass() != null && mpi.getTypeClass().isArray()
                    && !Byte.TYPE.equals(mpi.getTypeClass().getComponentType())) {
                    String min = (String)mpi.getProperty("minOccurs");
                    String max = (String)mpi.getProperty("maxOccurs");
                    if (min == null) {
                        min = "0";
                    }
                    if (max == null) {
                        max = "unbounded";
                    }
                    el.setMinOccurs(Long.parseLong(min));
                    el.setMaxOccurs("unbounded".equals(max) ? Long.MAX_VALUE : Long.parseLong(max));
                    Boolean b = (Boolean)mpi.getProperty("nillable");
                    if (b != null && b.booleanValue()) {
                        el.setNillable(b.booleanValue());
                    }
                } else if (Collection.class.isAssignableFrom(mpi.getTypeClass())
                           && mpi.getTypeClass().isInterface()) {
                    Type type = (Type)mpi.getProperty(GENERIC_TYPE);
                    if (!(type instanceof java.lang.reflect.ParameterizedType)
                        && mpi.getTypeQName() == null) {
                        el.setMinOccurs(0);
                        el.setMaxOccurs(Long.MAX_VALUE);
                        el.setSchemaTypeName(Constants.XSD_ANYTYPE);
                    }

                } else {
                    el.setMaxOccurs(1);
                    if (mpi.getTypeClass() != null && !mpi.getTypeClass().isPrimitive()) {
                        el.setMinOccurs(0);
                    }
                }
                seq.getItems().add(el);
            }
            if (Boolean.TRUE.equals(mpi.getProperty(HEADER))) {
                QName qn = (QName)mpi.getProperty(ELEMENT_NAME);

                el.setName(qn.getLocalPart());
                el.setQName(qn);

                SchemaInfo headerSchemaInfo = getOrCreateSchema(serviceInfo, 
                                                                qn.getNamespaceURI(),
                                                                getQualifyWrapperSchema());
                if (!isExistSchemaElement(headerSchemaInfo.getSchema(), qn)) {
                    headerSchemaInfo.getSchema().getItems().add(el);
                }
            }
        }

    }
    
    private Annotation[] getMethodParameterAnnotations(final MessagePartInfo mpi) {
        Annotation[][] paramAnno = (Annotation[][])mpi.getProperty(METHOD_PARAM_ANNOTATIONS);
        int index = mpi.getIndex();
        if (paramAnno != null && index < paramAnno.length && index >= 0) {
            return paramAnno[index];
        }
        return null;
    }
    
    private Annotation[] getMethodAnnotations(final MessagePartInfo mpi) {
        return (Annotation[])mpi.getProperty(METHOD_ANNOTATONS);        
    }    
    
    private void addMimeType(final XmlSchemaElement element, final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof XmlMimeType) {
                MimeAttribute attr = new MimeAttribute();
                attr.setValue(((XmlMimeType)annotation).value());
                element.addMetaInfo(MimeAttribute.MIME_QNAME, attr);
            }
        }         
    }

    
    /**
     * This is a really ugly trick to get around a bug or oversight in XmlSchema, which is that
     * there is no way to programmatically construct an XmlSchema instance that ends up cataloged
     * in a collection. If there is a fix to WSCOMMONS-272, this can go away.
     * @param collection collection to contain new schema
     * @return new schema
     */
    private XmlSchema newXmlSchemaInCollection(XmlSchemaCollection collection, String namespaceURI) {
        StringBuffer tinyXmlSchemaDocument = new StringBuffer();
        tinyXmlSchemaDocument.append("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' ");
        tinyXmlSchemaDocument.append("targetNamespace='" + namespaceURI + "'/>");
        StringReader reader = new StringReader(tinyXmlSchemaDocument.toString());
        return collection.read(reader, new ValidationEventHandler() { });
    }

    private SchemaInfo getOrCreateSchema(ServiceInfo serviceInfo,
                                         String namespaceURI, 
                                         boolean qualified) {
        for (SchemaInfo s : serviceInfo.getSchemas()) {
            if (s.getNamespaceURI().equals(namespaceURI)) {
                return s;
            }
        }

        SchemaInfo schemaInfo = new SchemaInfo(serviceInfo, namespaceURI);
        XmlSchemaCollection col = serviceInfo.getXmlSchemaCollection();

        XmlSchema schema = newXmlSchemaInCollection(col, namespaceURI);
        if (qualified) {
            schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
        }
        schemaInfo.setSchema(schema);

        Map<String, String> explicitNamespaceMappings = this.getDataBinding().getDeclaredNamespaceMappings();
        if (explicitNamespaceMappings == null) {
            explicitNamespaceMappings = Collections.emptyMap();
        }
        NamespaceMap nsMap = new NamespaceMap();
        for (Map.Entry<String, String> mapping : explicitNamespaceMappings.entrySet()) {
            nsMap.add(mapping.getValue(), mapping.getKey());
        }
        
        if (!explicitNamespaceMappings.containsKey(WSDLConstants.NU_SCHEMA_XSD)) {
            nsMap.add(WSDLConstants.NP_SCHEMA_XSD, WSDLConstants.NU_SCHEMA_XSD);
        }
        if (!explicitNamespaceMappings.containsKey(serviceInfo.getTargetNamespace())) {
            nsMap.add(WSDLConstants.CONVENTIONAL_TNS_PREFIX, serviceInfo.getTargetNamespace());
        }
        schema.setNamespaceContext(nsMap);
        serviceInfo.addSchema(schemaInfo);
        return schemaInfo;
    }

    protected void createMessageParts(InterfaceInfo intf, OperationInfo op, Method method) {
        final Class[] paramClasses = method.getParameterTypes();
        // Setup the input message
        op.setProperty(METHOD, method);
        MessageInfo inMsg = op.createMessage(this.getInputMessageName(op, method));
        op.setInput(inMsg.getName().getLocalPart(), inMsg);
        for (int j = 0; j < paramClasses.length; j++) {
            if (isInParam(method, j)) {
                final QName q = getInParameterName(op, method, j);
                final QName q2 = getInPartName(op, method, j);
                MessagePartInfo part = inMsg.addMessagePart(q2);
                initializeParameter(part, paramClasses[j], method.getGenericParameterTypes()[j]);
                part.setProperty(METHOD_PARAM_ANNOTATIONS, method.getParameterAnnotations());                
                if (!isWrapped(method) && !isRPC(method)) {
                    part.setProperty(ELEMENT_NAME, q);
                }

                if (isHeader(method, j)) {
                    part.setProperty(HEADER, Boolean.TRUE);
                    if (isRPC(method) || !isWrapped(method)) {
                        part.setElementQName(q);
                    } else {
                        part.setProperty(ELEMENT_NAME, q);
                    }
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
                final QName q2 = getOutParameterName(op, method, -1);
                MessagePartInfo part = outMsg.addMessagePart(q);
                initializeParameter(part, method.getReturnType(), method.getGenericReturnType());
                if (!isRPC(method) && !isWrapped(method)) {
                    part.setProperty(ELEMENT_NAME, q2);
                }
                part.setProperty(METHOD_ANNOTATONS, method.getAnnotations());
                if (isHeader(method, -1)) {
                    part.setProperty(HEADER, Boolean.TRUE);
                    if (isRPC(method) || !isWrapped(method)) {
                        part.setElementQName(q2);
                    } else {
                        part.setProperty(ELEMENT_NAME, q2);
                    }
                }

                part.setIndex(0);
            }

            for (int j = 0; j < paramClasses.length; j++) {
                if (isOutParam(method, j)) {
                    if (outMsg == null) {
                        outMsg = op.createMessage(createOutputMessageName(op, method));
                    }
                    QName q = getOutPartName(op, method, j);
                    QName q2 = getOutParameterName(op, method, j);

                    if (isInParam(method, j)) {
                        q = op.getInput().getMessagePartByIndex(j).getName();
                        q2 = (QName)op.getInput().getMessagePartByIndex(j).getProperty(ELEMENT_NAME);
                        if (q2 == null) {
                            q2 = op.getInput().getMessagePartByIndex(j).getElementQName();
                        }
                    }

                    MessagePartInfo part = outMsg.addMessagePart(q);
                    initializeParameter(part, paramClasses[j], method.getGenericParameterTypes()[j]);
                    part.setIndex(j + 1);

                    if (!isRPC(method) && !isWrapped(method)) {
                        part.setProperty(ELEMENT_NAME, q2);
                    }

                    if (isInParam(method, j)) {
                        part.setProperty(MODE_INOUT, Boolean.TRUE);
                    }
                    if (isHeader(method, j)) {
                        part.setProperty(HEADER, Boolean.TRUE);
                        if (isRPC(method) || !isWrapped(method)) {
                            part.setElementQName(q2);
                        } else {
                            part.setProperty(ELEMENT_NAME, q2);
                        }
                    }
                }
            }

        }

        initializeFaults(intf, op, method);
    }
    
    protected void createInputWrappedMessageParts(OperationInfo op, Method method, MessageInfo inMsg) {
        MessagePartInfo part = inMsg.addMessagePart("parameters");
        part.setIndex(0);
        part.setElement(true);
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getRequestWrapperName(op, method);
            if (q != null) {
                part.setElementQName(q);
            }
        }
        if (part.getElementQName() == null) {
            part.setElementQName(inMsg.getName());
        } else if (!part.getElementQName().equals(op.getInput().getName())) {
            op.getInput().setName(part.getElementQName());
        }
        if (getRequestWrapper(method) != null) {
            part.setTypeClass(this.getRequestWrapper(method));
        } else if (getRequestWrapperClassName(method) != null) {
            part.setProperty("REQUEST.WRAPPER.CLASSNAME", getRequestWrapperClassName(method));
        }

        for (MessagePartInfo mpart : op.getInput().getMessageParts()) {
            if (Boolean.TRUE.equals(mpart.getProperty(HEADER))) {
                int idx = mpart.getIndex();
                inMsg.addMessagePart(mpart);
                mpart.setIndex(idx);
            }
        }
    }

    protected void createOutputWrappedMessageParts(OperationInfo op, Method method, MessageInfo outMsg) {
        String partName = null;
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            partName = c.getResponseWrapperPartName(op, method);
            if (partName != null) {
                break;
            }
        }
        if (partName == null) {
            partName = "parameters";
        }
        
        MessagePartInfo part = outMsg.addMessagePart(partName);
        part.setElement(true);
        part.setIndex(0);
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getResponseWrapperName(op, method);
            if (q != null) {
                part.setElementQName(q);
                break;
            }
        }

        if (part.getElementQName() == null) {
            part.setElementQName(outMsg.getName());
        } else if (!part.getElementQName().equals(op.getOutput().getName())) {
            op.getOutput().setName(part.getElementQName());
        }

        if (this.getResponseWrapper(method) != null) {
            part.setTypeClass(this.getResponseWrapper(method));
        } else if (getResponseWrapperClassName(method) != null) {
            part.setProperty("RESPONSE.WRAPPER.CLASSNAME", getResponseWrapperClassName(method));
        }

        for (MessagePartInfo mpart : op.getOutput().getMessageParts()) {
            if (Boolean.TRUE.equals(mpart.getProperty(HEADER))) {
                int idx = mpart.getIndex();
                outMsg.addMessagePart(mpart);
                mpart.setIndex(idx);
            }
        }
    }

    // TODO: Remove reference to JAX-WS holder if possible
    // We do need holder support in the simple frontend though as Aegis has its
    // own
    // holder class. I'll tackle refactoring this into a more generic way of
    // handling
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
            rawClass = (Class)((GenericArrayType)rawType).getGenericComponentType();
            rawClass = Array.newInstance(rawClass, 0).getClass();
        } else {
            if (rawType instanceof ParameterizedType) {
                rawType = (Class)((ParameterizedType)rawType).getRawType();
            }
            rawClass = (Class)rawType;
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

    public QName getInterfaceName() {
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
        // TODO: This seems wrong and not sure who put it here. Will revisit -
        // DBD
        boolean ret = false;
        String initOfMethodInClass = methodNameInClass.substring(0, 1);
        String initOfMethodInWsdl = methodNameInWsdl.substring(0, 1);
        if (initOfMethodInClass.equalsIgnoreCase(initOfMethodInWsdl)
            && methodNameInClass.substring(1, methodNameInClass.length())
                .equals(methodNameInWsdl.substring(1, methodNameInWsdl.length()))) {
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
                                    final OperationInfo op, final Method method) {
        // Set up the fault messages
        final Class[] exceptionClasses = method.getExceptionTypes();
        for (int i = 0; i < exceptionClasses.length; i++) {
            Class exClazz = exceptionClasses[i];

            // Ignore XFireFaults because they don't need to be declared
            if (exClazz.equals(Exception.class) || Fault.class.isAssignableFrom(exClazz)
                || exClazz.equals(RuntimeException.class) || exClazz.equals(Throwable.class)) {
                continue;
            }

            addFault(service, op, exClazz);
        }
    }

    protected void initializeDefaultInterceptors() {
        super.initializeDefaultInterceptors();
        
        initializeFaultInterceptors();
    }
    
    protected void initializeFaultInterceptors() {
        getService().getOutFaultInterceptors().add(new FaultOutInterceptor());
    }

    protected FaultInfo addFault(final InterfaceInfo service, final OperationInfo op, Class exClass) {
        Class beanClass = getBeanClass(exClass);
        if (beanClass == null) {
            return null;
        }

        QName faultName = getFaultName(service, op, exClass, beanClass);
        FaultInfo fi = op.addFault(new QName(op.getName().getNamespaceURI(), exClass.getSimpleName()),
                                   new QName(op.getName().getNamespaceURI(), exClass.getSimpleName()));
        fi.setProperty(Class.class.getName(), exClass);
        fi.setProperty("elementName", faultName);
        MessagePartInfo mpi = fi.addMessagePart(new QName(faultName.getNamespaceURI(),
                                                          exClass.getSimpleName()));
        mpi.setElementQName(faultName);
        mpi.setTypeClass(beanClass);
        return fi;
    }

    protected void createFaultForException(Class<?> exClass, FaultInfo fi) {
        Field fields[] = exClass.getDeclaredFields();
        for (Field field : fields) {
            MessagePartInfo mpi = fi
                .addMessagePart(new QName(fi.getName().getNamespaceURI(), field.getName()));
            mpi.setProperty(Class.class.getName(), field.getType());
        }

        MessagePartInfo mpi = fi.addMessagePart(new QName(fi.getName().getNamespaceURI(), "message"));
        mpi.setProperty(Class.class.getName(), String.class);
    }

    protected Class<?> getBeanClass(Class<?> exClass) {
        if (java.rmi.RemoteException.class.isAssignableFrom(exClass)) {
            return null;
        }
        
        if (FaultInfoException.class.isAssignableFrom(exClass)) {
            try {
                Method m = exClass.getMethod("getFaultInfo");
                return m.getReturnType();
            } catch (SecurityException e) {
                throw new ServiceConstructionException(e);
            } catch (NoSuchMethodException e) {
                throw new ServiceConstructionException(e);
            }
        }
        
        
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

    protected String getAction(OperationInfo op, Method method) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            String s = c.getAction(op, method);
            if (s != null) {
                return s;
            }
        }
        return "";
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

    protected QName getInPartName(final OperationInfo op, final Method method, final int paramNumber) {
        if (paramNumber == -1) {
            return null;
        }

        if (isWrapped(method) && !isHeader(method, paramNumber)) {
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

    protected QName getInParameterName(final OperationInfo op, final Method method, final int paramNumber) {
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

    protected QName getOutParameterName(final OperationInfo op, final Method method, final int paramNumber) {
        for (Iterator itr = serviceConfigurations.iterator(); itr.hasNext();) {
            AbstractServiceConfiguration c = (AbstractServiceConfiguration)itr.next();
            QName q = c.getOutParameterName(op, method, paramNumber);
            if (q != null) {
                return q;
            }
        }
        throw new IllegalStateException("ServiceConfiguration must provide a value!");
    }

    protected QName getOutPartName(final OperationInfo op, final Method method, final int paramNumber) {
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
    protected String getResponseWrapperClassName(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            String cls = c.getResponseWrapperClassName(selected);
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
    protected String getRequestWrapperClassName(Method selected) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            String cls = c.getRequestWrapperClassName(selected);
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
                    //create a unique string so if its an interned string (like
                    //from an annotation), caches will clear
                    wsdlURL = new String(wsdlURL);
                    break;
                }
            }
        }
        return wsdlURL;
    }

    public void setWsdlURL(String wsdlURL) {
        //create a unique string so if its an interned string (like
        //from an annotation), caches will clear
        this.wsdlURL = new String(wsdlURL);
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

    public boolean isRPC(Method method) {
        for (AbstractServiceConfiguration c : serviceConfigurations) {
            Boolean b = c.isRPC(method);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return "rpc".equals(getStyle());
    }

    public void setWrapped(boolean style) {
        this.wrappedStyle = style;
    }

    /**
     *  Returns non-null if wrapped mode was explicitely disabled or enabled.
     */
    public Boolean getWrapped() {
        return this.wrappedStyle;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<Method> getIgnoredMethods() {
        return ignoredMethods;
    }

    public void setIgnoredMethods(List<Method> ignoredMethods) {
        this.ignoredMethods = ignoredMethods;
    }

}
