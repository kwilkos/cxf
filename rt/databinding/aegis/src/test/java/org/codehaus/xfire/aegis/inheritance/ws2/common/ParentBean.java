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
package org.codehaus.xfire.aegis.inheritance.ws2.common;

import org.codehaus.xfire.aegis.inheritance.ws2.common.pack1.ContentBean1;

/**
 * <br/>
 * 
 * @author xfournet
 */
public class ParentBean
{
    private String m_id;

    private ContentBean1 m_content;

    public ParentBean()
    {
    }

    public ParentBean(String id, ContentBean1 content)
    {
        m_id = id;
        m_content = content;
    }

    public String getId()
    {
        return m_id;
    }

    public void setId(String id)
    {
        m_id = id;
    }

    public ContentBean1 getContent()
    {
        return m_content;
    }

    public void setContent(ContentBean1 content)
    {
        m_content = content;
    }

    public String toString()
    {
        return "[" + getClass().getName() + "] id=" + m_id + "; content={" + m_content + "}";
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

        final ParentBean that = (ParentBean) o;

        if (m_content != null ? !m_content.equals(that.m_content) : that.m_content != null)
        {
            return false;
        }
        if (m_id != null ? !m_id.equals(that.m_id) : that.m_id != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (m_id != null ? m_id.hashCode() : 0);
        result = 29 * result + (m_content != null ? m_content.hashCode() : 0);
        return result;
    }
}
