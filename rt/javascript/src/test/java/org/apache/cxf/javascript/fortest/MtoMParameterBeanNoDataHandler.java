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

package org.apache.cxf.javascript.fortest;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlSeeAlso(Base64Binary.class)
@XmlType(namespace = "uri:org.apache.cxf.javascript.testns")
public class MtoMParameterBeanNoDataHandler {
    private String ordinary;
    private String notXml10;
    
    public String getOrdinary() {
        return ordinary;
    }
    public void setOrdinary(String ordinary) {
        this.ordinary = ordinary;
    }
    
    @XmlMimeType("text/plain;charset=utf-8")
    @XmlSchemaType(namespace = "http://www.w3.org/2005/05/xmlmime", name = "base64Binary")
    public byte[] getNotXml10() throws UnsupportedEncodingException {
        return notXml10.getBytes("utf-8");
    }
    
    public void setNotXml10(String notXml10) {
        this.notXml10 = notXml10;
    }
}
