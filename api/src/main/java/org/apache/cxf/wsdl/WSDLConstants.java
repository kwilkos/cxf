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

package org.apache.cxf.wsdl;

import javax.xml.namespace.QName;

public final class WSDLConstants {

    public static final String WSDL11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl";
    
    public static final String NP_XMLNS = "xmlns";
    public static final String NU_XMLNS = "http://www.w3.org/2000/xmlns/";

    // XML Schema (CR) datatypes + structures
    public static final String NP_SCHEMA_XSD = "xs";
    public static final String NU_SCHEMA_XSD = "http://www.w3.org/2001/XMLSchema";

    // XML Schema instance
    public static final String NP_SCHEMA_XSI = "xsi";
    public static final String NU_SCHEMA_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    public static final String A_XSI_TYPE = "type";
    public static final String A_XSI_NIL = "nil";
    
    // XML Schema attribute names
    public static final QName NA_XSI_TYPE = new QName(NP_SCHEMA_XSI, A_XSI_TYPE, NU_SCHEMA_XSI);
    public static final QName NA_XSI_NIL = new QName(NP_SCHEMA_XSI, A_XSI_NIL, NU_SCHEMA_XSI);    


    private WSDLConstants() {        
    }
    
}
