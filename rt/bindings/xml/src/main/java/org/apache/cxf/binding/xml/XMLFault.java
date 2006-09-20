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
package org.apache.cxf.binding.xml;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;

public class XMLFault extends Fault {

    public static final String XML_FAULT_PREFIX = "xfns";

    public static final QName XML_FAULT_ROOT = new QName(XMLConstants.NS_XML_FORMAT, "XMLFault");

    public static final QName XML_FAULT_STRING = new QName(XMLConstants.NS_XML_FORMAT, "faultstring");

    public static final QName XML_FAULT_DETAIL = new QName(XMLConstants.NS_XML_FORMAT, "detail");

    public static final QName XML_FAULT_CODE_SERVER = new QName(XMLConstants.NS_XML_FORMAT, "SERVER");

    public static final QName XML_FAULT_CODE_CLIENT = new QName(XMLConstants.NS_XML_FORMAT, "CLIENT");

    
    static final long serialVersionUID = 100000;

    public XMLFault(Message message, Throwable throwable) {
        super(message, throwable);        
    }

    public XMLFault(Message message) {
        super(message);        
    }

    public XMLFault(String message) {
        super(new Message(message, (ResourceBundle) null));        
    }

    public static XMLFault createFault(Fault f) {
        if (f instanceof XMLFault) {
            return (XMLFault) f;
        }
        Throwable th = f.getCause();
        if (f.getCause() instanceof InvocationTargetException) {
            th = th.getCause();
        }
        XMLFault xmlFault = new XMLFault(new Message(f.getMessage(), (ResourceBundle) null), th);
        xmlFault.setDetail(f.getDetail());
        return xmlFault;
    }

}
