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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean with a selection of elements suitable for testing the JavaScript client.
 */
@XmlRootElement(namespace = "uri:org.apache.cxf.javascript.testns")
@XmlType(namespace = "uri:org.apache.cxf.javascript.testns")
public class TestBean1 {
    
    public TestBean1() {
        intItem = 43;
        doubleItem = -1.0;
        beanTwoItem = new TestBean2("required=true");
        beanTwoNotRequiredItem = null;
    }
    
    //CHECKSTYLE:OFF
    public String stringItem;
    @XmlElement(namespace = "uri:org.apache.cxf.javascript.testns2")
    public int intItem;
    @XmlElement(defaultValue = "43")
    public long longItem;
    public byte[] base64Item;
    @XmlElement(required = false)
    public int optionalIntItem;
    @XmlElement(required = false, namespace = "uri:org.apache.cxf.javascript.testns2")
    public String optionalStringItem;
    @XmlElement(required = false)
    public int[] optionalIntArrayItem;
    @XmlElement(defaultValue = "-1.0")
    public double doubleItem;
    @XmlElement(required = true)
    public TestBean2 beanTwoItem;
    @XmlElement(required = false)
    public TestBean2 beanTwoNotRequiredItem;
    //CHECKSTYLE:ON
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestBean1)) {
            return false;
        }
        TestBean1 other = (TestBean1) obj;
        return stringItem.equals(other.stringItem) 
            && intItem == other.intItem
            && longItem == other.longItem
            && base64Item == other.base64Item
            && optionalIntItem == other.optionalIntItem
            && optionalIntArrayItem == other.optionalIntArrayItem
            && doubleItem == other.doubleItem
            && beanTwoItem.equals(other.beanTwoItem)
            && beanTwoNotRequiredItem.equals(other.beanTwoNotRequiredItem);
    }

    @Override
    public int hashCode() {
        // intentionally stupid. We don't use this object in collections.
        return super.hashCode();
    }
    
}
