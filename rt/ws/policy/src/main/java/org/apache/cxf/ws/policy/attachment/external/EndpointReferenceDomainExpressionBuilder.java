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

package org.apache.cxf.ws.policy.attachment.external;

import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.policy.PolicyException;

/**
 * 
 */
public class EndpointReferenceDomainExpressionBuilder implements DomainExpressionBuilder {

    private static final ResourceBundle BUNDLE 
        = BundleUtils.getBundle(EndpointReferenceDomainExpressionBuilder.class);
    
    private Unmarshaller unmarshaller;
    
    EndpointReferenceDomainExpressionBuilder() {
        try {
            JAXBContext context = JAXBContext.newInstance(EndpointReferenceType.class.getPackage().getName());
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new PolicyException(new Message("EPR_DOMAIN_EXPRESSION_BUILDER_INIT_EXC", BUNDLE, 
                                                  (Object[])null), ex);
        }
    }
    
    public DomainExpression build(Element e) {
        Object obj = null;
        try {
            obj = unmarshaller.unmarshal(e);
        } catch (JAXBException ex) {
            throw new PolicyException(new Message("EPR_DOMAIN_EXPRESSION_BUILD_EXC", BUNDLE, 
                                                  (Object[])null), ex);
        }
        if (obj instanceof JAXBElement<?>) {
            JAXBElement<?> el = (JAXBElement<?>)obj;
            obj = el.getValue();
        } 

        EndpointReferenceDomainExpression eprde = new EndpointReferenceDomainExpression();
        eprde.setEndpointReference((EndpointReferenceType)obj);
        return eprde;
    }

    
}
