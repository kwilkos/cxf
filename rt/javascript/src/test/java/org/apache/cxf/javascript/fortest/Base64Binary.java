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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * This class is here to make JAXB happy. We will in fact use a DataHandler along 
 * with this schema type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.w3.org/2005/05/xmlmime", 
         name = "base64Binary", propOrder = {
                                             "byteValue"
                                             })
public class Base64Binary {

    @XmlValue
    protected byte[] byteValue;
    @XmlAttribute(namespace = "http://www.w3.org/2005/05/xmlmime")
    protected String contentType;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getValue() {
        return byteValue;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setValue(byte[] newValue) {
        this.byteValue = newValue;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }
}
