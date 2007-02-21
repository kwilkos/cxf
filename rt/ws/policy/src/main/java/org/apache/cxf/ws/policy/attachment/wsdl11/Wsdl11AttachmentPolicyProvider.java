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

package org.apache.cxf.ws.policy.attachment.wsdl11;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.AbstractDescriptionElement;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.DescriptionInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.Extensible;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyProvider;
import org.apache.cxf.ws.policy.attachment.AbstractPolicyProvider;
import org.apache.cxf.ws.policy.attachment.reference.LocalServiceModelReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;

/**
 * PolicyAttachmentManager provides methods to retrieve element policies and
 * calculate effective policies based on the policy subject's scope.
 * 
 */
public class Wsdl11AttachmentPolicyProvider extends AbstractPolicyProvider 
    implements PolicyProvider {

    
    public Wsdl11AttachmentPolicyProvider() {
        this(null);
    }
    
    public Wsdl11AttachmentPolicyProvider(Bus bus) {
        super(bus);
    }  
    
    public Policy getEffectivePolicy(ServiceInfo si) {
        return getElementPolicy(si);
    }
    
    /**
     * The effective policy for a WSDL endpoint policy subject includes the element policy of the 
     * wsdl11:port element that defines the endpoint merged with the element policy of the
     * referenced wsdl11:binding element and the element policy of the referenced wsdl11:portType
     * element that defines the interface of the endpoint. 
     * 
     * @param ei the EndpointInfo object identifying the endpoint
     * @return the effective policy
     */
    public Policy getEffectivePolicy(EndpointInfo ei) {
        Policy p = getElementPolicy(ei);
        p = p.merge(getElementPolicy(ei.getBinding()));
        p = p.merge(getElementPolicy(ei.getInterface()));
        
        return p;
    }

    /**
     * The effective policy for a WSDL operation policy subject is calculated in relation to a 
     * specific port, and includes the element policy of the wsdl11:portType/wsdl11:operation 
     * element that defines the operation merged with that of the corresponding 
     * wsdl11:binding/wsdl11:operation element.
     * 
     * @param bi the BindingOperationInfo identifying the operation in relation to a port
     * @return the effective policy
     */
    public Policy getEffectivePolicy(BindingOperationInfo bi) {
        DescriptionInfo di = bi.getBinding().getDescription();
        Policy p = getElementPolicy(bi, di);
        p = p.merge(getElementPolicy(bi.getOperationInfo(), di));
        return p;
    }
    
    /**
     * The effective policy for a specific WSDL message (input or output) is calculated
     * in relation to a specific port, and includes the element policy of the wsdl:message
     * element that defines the message's type merged with the element policy of the 
     * wsdl11:binding and wsdl11:portType message definitions that describe the message.
     * For example, the effective policy of a specific input message for a specific port
     * would be the (element policies of the) wsdl11:message element defining the message type,
     * the wsdl11:portType/wsdl11:operation/wsdl11:input element and the corresponding
     * wsdl11:binding/wsdl11:operation/wsdl11:input element for that message.
     * 
     * @param bmi the BindingMessageInfo identifiying the message
     * @return the effective policy
     */
    public Policy getEffectivePolicy(BindingMessageInfo bmi) {
        ServiceInfo si = bmi.getBindingOperation().getBinding().getService();
        DescriptionInfo di = si.getDescription();

        Policy p = getElementPolicy(bmi, di);
        MessageInfo mi = bmi.getMessageInfo();
        p = p.merge(getElementPolicy(mi, di));
        Extensible ex = getMessageTypeInfo(mi.getName(), di);
        p = p.merge(getElementPolicy(ex, di));

        return p;
    }
    

    
    public Policy getEffectivePolicy(BindingFaultInfo bfi) {
        ServiceInfo si = bfi.getBindingOperation().getBinding().getService();
        DescriptionInfo di = si.getDescription();

        Policy p = getElementPolicy(bfi, di);
        FaultInfo fi = bfi.getFaultInfo();
        p = p.merge(getElementPolicy(fi, di));
        Extensible ex = getMessageTypeInfo(fi.getName(), di);
        p = p.merge(getElementPolicy(ex, di));

        return p;
    }
    
    Policy getElementPolicy(AbstractDescriptionElement adh) {
        return getElementPolicy(adh, adh.getDescription());
    }

    Policy getElementPolicy(Extensible ex, DescriptionInfo di) {
        
        Policy elementPolicy = new Policy();
        
        if (null == ex) {
            return elementPolicy;
        }
        
        List<UnknownExtensibilityElement> extensions = 
            ex.getExtensors(UnknownExtensibilityElement.class);
        if (null == extensions) {
            return elementPolicy;
        }
        
        for (UnknownExtensibilityElement e : extensions) {
            Policy p = null;
            if (PolicyConstants.getPolicyElemQName().equals(e.getElementType())) {
                p = builder.getPolicy(e.getElement());

            } else if (PolicyConstants.getPolicyReferenceElemQName().equals(e.getElementType())) {
                PolicyReference ref = builder.getPolicyReference(e.getElement());
                if (null != ref) {
                    p = resolveReference(ref, di);
                }
            }
            if (null != p) {
                elementPolicy = elementPolicy.merge(p);
            }
        }

        return elementPolicy;
    }
    
    Policy resolveReference(PolicyReference ref, DescriptionInfo di) {
        Policy p = null;
        if (isExternal(ref)) {
            p = resolveExternal(ref, di.getBaseURI());
        } else {
            p = resolveLocal(ref, di);
        }
        checkResolved(ref, p);
        return p;
    }
    
    Policy resolveLocal(PolicyReference ref, DescriptionInfo di) {
        String uri = ref.getURI().substring(1);
        String absoluteURI = di.getBaseURI() + uri;
        Policy resolved = registry.lookup(absoluteURI);
        if (null != resolved) {
            return resolved;
        }
        ReferenceResolver resolver = new LocalServiceModelReferenceResolver(di, builder);
        resolved = resolver.resolveReference(uri);
        if (null != resolved) {
            ref.setURI(absoluteURI);
            registry.register(absoluteURI, resolved);
        }
        return resolved;
    }
    
     
    private Extensible getMessageTypeInfo(QName name, DescriptionInfo di) {
        Definition def = (Definition)di.getProperty(WSDLServiceBuilder.WSDL_DEFINITION);
        if (null == def) {
            return null;
        }
        
        javax.wsdl.Message m = def.getMessage(name);
        if (null != m) {
            List<ExtensibilityElement> extensors = 
                CastUtils.cast(m.getExtensibilityElements(), ExtensibilityElement.class);
            if (null != extensors) {
                return new ExtensibleInfo(extensors);
            }
        }
        return null;
    }
     
    private class ExtensibleInfo implements Extensible {
        private List<ExtensibilityElement> extensors;
        ExtensibleInfo(List<ExtensibilityElement> e) {
            extensors = e;
        }
        
        public <T> T getExtensor(Class<T> cls) {
            for (ExtensibilityElement e : extensors) {
                if (cls.isInstance(e)) {
                    return cls.cast(e);
                }
            }
            return null;
        }
        public <T> List<T> getExtensors(Class<T> cls) {
            if (null == extensors) {
                return null;
            }
            
            List<T> list = new ArrayList<T>(extensors.size());
            for (ExtensibilityElement e : extensors) {
                if (cls.isInstance(e)) {
                    list.add(cls.cast(e));
                }
            }
            return list;
        }
        
    }
   

}
