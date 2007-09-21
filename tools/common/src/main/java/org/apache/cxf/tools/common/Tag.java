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

package org.apache.cxf.tools.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

public class Tag {
    QName name;
    Set<QName> attributes;
    String text;

    List<Tag> tags;
    Tag parent;

    List<String> ignoreAttr;


    public List<String> getIgnoreAttr() {
        if (ignoreAttr == null) {
            ignoreAttr = new ArrayList<String>();
        }
        return ignoreAttr;
    }

    public Tag getParent() {
        return this.parent;
    }

    public void setParent(Tag nTag) {
        this.parent = nTag;
    }

    public List<Tag> getTags() {
        if (tags == null) {
            tags = new ArrayList<Tag>();
        }
        return tags;
    }

    public String getText() {
        return text;
    }

    public void setText(String nText) {
        this.text = nText;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName nName) {
        this.name = nName;
    }

    public Set<QName> getAttributes() {
        if (attributes == null) {
            attributes = new HashSet<QName>();
        }
        return attributes;
    }
    
    private String getIndent(int size) {
        String indent = "    ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            sb.append(indent);
        }
        return sb.toString();
    }

    private String formatAttribute(final Tag tag) {
        StringBuffer sb = new StringBuffer();
        sb.append(tag.getName().getLocalPart());
        sb.append(" ");
        for (QName attr : tag.getAttributes()) {
            sb.append(attr.getNamespaceURI());
            sb.append("=\"");
            sb.append(attr.getLocalPart());
            sb.append("\" ");
        }
        return sb.toString().trim();
    }

    private String formatTag(Tag tag, int indent) {
        StringBuffer sb = new StringBuffer();
        sb.append(getIndent(indent));
        sb.append(indent);
        sb.append("<");
        sb.append(formatAttribute(tag));
        sb.append(">");
        if (tag.getParent() != null) {
            sb.append(" (" + tag.getParent().getName().getLocalPart() + ")");
        }
        if (text != null) {
            sb.append(text); 
        }
        sb.append("\n");

        if (tag.getTags().size() > 0) {
            indent++;
            for (Tag subTag : tag.getTags()) {
                sb.append(formatTag(subTag, indent));
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(formatTag(this, 0));
        return sb.toString();
    }

    public int hashCode() {
        return getName().hashCode() + getAttributes().hashCode();
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof Tag)) {
            return false;
        }

        if (object == this) {
            return true;
        }
        Tag tag = (Tag) object;
        if (!getName().equals(tag.getName())) {
            return false;
        }
        for (QName attr : getAttributes()) {
            if (getIgnoreAttr().contains(attr.getNamespaceURI())) {
                continue;
            }
            if (!tag.getAttributes().contains(attr)) {
                return false;
            }
        }
        return true;
    }
}
