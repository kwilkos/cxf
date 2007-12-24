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

package org.apache.cxf.common.xmlschema;

import javax.xml.namespace.QName;

/**
 * This class holds constants related to XML Schema. Over time, some of the contents
 * of WSDLConstants should move here.
 */
public final class XmlSchemaConstants {
    
    public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";
    public static final QName ANY_TYPE_QNAME = new QName(XSD_NAMESPACE_URI, "anyType");
}
