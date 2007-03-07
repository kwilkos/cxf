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
package org.codehaus.xfire.aegis.inheritance.ws1;

/**
 * <br/>
 * 
 * @author xfournet
 */
public class WS1Exception
    extends Exception
{
    private int m_errorCode;
    private Object simpleBean;
    
    public WS1Exception()
    {
    }

    public WS1Exception(String message)
    {
        super(message);
    }

    public WS1Exception(String message, int errorCode1)
    {
        super(message);
        m_errorCode = errorCode1;
    }

    public int getErrorCode()
    {
        return m_errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        m_errorCode = errorCode;
    }

    public Object getSimpleBean()
    {
        return simpleBean;
    }

    public void setSimpleBean(Object simpleBean)
    {
        this.simpleBean = simpleBean;
    }

    public String toString()
    {
        return "[" + getClass().getName() + "] msg=" + getMessage() + "; errorCode=" + m_errorCode;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final WS1Exception that = (WS1Exception) o;

        if (getMessage() != null ? !getMessage().equals(that.getMessage())
                : that.getMessage() != null)
        {
            return false;
        }

        if (m_errorCode != that.m_errorCode)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return m_errorCode;
    }
}
