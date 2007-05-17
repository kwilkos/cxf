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
package org.apache.cxf.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.Assert;
import org.junit.Test;

public class WrapperHelperTest extends Assert {


    @Test
    public void getBooleanTypeWrappedPart() throws Exception {
        SetIsOK ok = new SetIsOK();
        ok.setParameter3(new boolean[] {true, false});
        Object object = WrapperHelper.getWrappedPart("Parameter1", ok, "boolean");
        assertTrue(object instanceof Boolean);
        object = WrapperHelper.getWrappedPart("Parameter3", ok, "boolean");
        assertTrue(object instanceof boolean[]);
        assertTrue(((boolean[])object)[0]);
        assertFalse(((boolean[])object)[1]);
        
        WrapperHelper.setWrappedPart("Parameter1", ok, true);
        assertTrue(ok.isParameter1());
        WrapperHelper.setWrappedPart("Parameter3", ok, new boolean[] {false, true});
        assertTrue(ok.getParameter3()[1]);
        assertFalse(ok.getParameter3()[0]);        
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "parameter1", "parameter2", "parameter3" })
    @XmlRootElement(name = "setIsOK")
    class SetIsOK {

        @XmlElement(name = "Parameter1")
        protected boolean parameter1;
        @XmlElement(name = "Parameter2")
        protected int parameter2;
        @XmlElement(name = "Parameter3")
        protected boolean parameter3[];

        /**
         * Gets the value of the parameter1 property.
         * 
         */
        public boolean isParameter1() {
            return parameter1;
        }

        /**
         * Sets the value of the parameter1 property.
         * 
         */
        public void setParameter1(boolean value) {
            this.parameter1 = value;
        }

        /**
         * Gets the value of the parameter2 property.
         * 
         */
        public int getParameter2() {
            return parameter2;
        }

        /**
         * Sets the value of the parameter2 property.
         * 
         */
        public void setParameter2(int value) {
            this.parameter2 = value;
        }
        
        
        /**
         * Gets the value of the parameter2 property.
         * 
         */
        public boolean[] getParameter3() {
            return parameter3;
        }

        /**
         * Sets the value of the parameter2 property.
         * 
         */
        public void setParameter3(boolean value[]) {
            this.parameter3 = value;
        }
    }
}

