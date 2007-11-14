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

package org.apache.cxf.service;

import org.apache.cxf.common.xmlschema.InvalidXmlSchemaReferenceException;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;

/**
 * 
 */
public class ServiceModelSchemaValidator extends ServiceModelVisitor {
    
    private SchemaCollection schemaCollection;
    private StringBuilder complaints;

    public ServiceModelSchemaValidator(ServiceInfo serviceInfo) {
        super(serviceInfo);
        schemaCollection = serviceInfo.getXmlSchemaCollection();
        complaints = new StringBuilder();
    }
    
    public String getComplaints() {
        return complaints.toString();
    }

    @Override
    public void begin(FaultInfo fault) {
        try {
            schemaCollection.validateQNameNamespace(fault.getFaultName());
            schemaCollection.validateQNameNamespace(fault.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(fault.getName() + " fault name " + ixsre.getMessage() + "\n");
        }
    }

    @Override
    public void begin(InterfaceInfo intf) {
        try {
            schemaCollection.validateQNameNamespace(intf.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(intf.getName() + " interface name " + ixsre.getMessage() + "\n");
        }
    }

    @Override
    public void begin(MessageInfo msg) {
        try {
            schemaCollection.validateQNameNamespace(msg.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(msg.getName() + " message name " + ixsre.getMessage() + "\n");
        }
    }

    @Override
    public void begin(MessagePartInfo part) {
        try {
            schemaCollection.validateQNameNamespace(part.getConcreteName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(part.getName() + " part concrete name " + ixsre.getMessage() + "\n");
        }

        try {
            schemaCollection.validateQNameNamespace(part.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(part.getName() + " part name " + ixsre.getMessage() + "\n");
        }

        if (part.isElement()) {
            try {
                schemaCollection.validateElementName(part.getName(), part.getElementQName());
            } catch (InvalidXmlSchemaReferenceException ixsre) {
                complaints.append(part.getName() + " element name " + ixsre.getMessage() + "\n");
            }
        } else {
            if (part.getTypeQName() == null) {
                complaints.append(part.getName() + " part type QName null.\n");
            } else {
                try {
                    schemaCollection.validateTypeName(part.getName(), part.getTypeQName());
                } catch (InvalidXmlSchemaReferenceException ixsre) {
                    complaints.append(part.getName() + " type name " + ixsre.getMessage() + "\n");
                }
            }
        }
    }

    @Override
    public void begin(OperationInfo op) {
        try {
            schemaCollection.validateQNameNamespace(op.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(op.getName() + " operation " + ixsre.getMessage() + "\n");
        }
    }

    @Override
    public void begin(ServiceInfo service) {
        try {
            schemaCollection.validateQNameNamespace(service.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(service.getName() + " service " + ixsre.getMessage() + "\n");
        }
    }

    @Override
    public void begin(UnwrappedOperationInfo op) {
        try {
            schemaCollection.validateQNameNamespace(op.getName());
        } catch (InvalidXmlSchemaReferenceException ixsre) {
            complaints.append(op.getName() + " unwrapped operation " + ixsre.getMessage() + "\n");
        }
    }
}
