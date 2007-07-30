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
package org.apache.cxf.headers;

import javax.xml.namespace.QName;

import org.apache.cxf.databinding.DataBinding;

public class Header {
    public static final int DIRECTION_IN = 0;
    public static final int DIRECTION_OUT = 1;
    public static final int DIRECTION_INOUT = 2;
    public static final String HEADER_LIST = Header.class.getName() + ".list";
   
    
    private DataBinding dataBinding;
    private QName name;
    private Object object;
//    private boolean inbound;
    private enum Direction  { DIRECTION_IN, DIRECTION_OUT, DIRECTION_INOUT }  
    
    private Direction direction = Header.Direction.DIRECTION_OUT;

    public Header(QName q, Object o) {
        this(q, o, null);
    }
    
    public Header(QName q, Object o, DataBinding b) {
        object = o;
        name = q;
        dataBinding = b;
    }
    
    public DataBinding getDataBinding() {
        return dataBinding;
    }
    public void setDataBinding(DataBinding dataBinding) {
        this.dataBinding = dataBinding;
    }
    public QName getName() {
        return name;
    }
    public void setName(QName name) {
        this.name = name;
    }
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    
    public void setDirection(int hdrDirection) {
       //this.inbound = true;
        switch (hdrDirection) {
        case DIRECTION_IN:
            this.direction = Header.Direction.DIRECTION_IN;
            break;
        case DIRECTION_INOUT:
            this.direction = Header.Direction.DIRECTION_INOUT;
            break;
        default:
            this.direction = Header.Direction.DIRECTION_OUT;
        }
    }
    
    public int getDirection() {
        int retval;
        switch (this.direction) {
        case DIRECTION_IN:
            retval = Header.DIRECTION_IN;
            break;
        case DIRECTION_INOUT:
            retval = Header.DIRECTION_INOUT;
            break;
        default:
            retval = Header.DIRECTION_OUT;
        }
        
        return retval;
    }
    
}
