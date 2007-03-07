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
package org.apache.cxf.aegis.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * A MessageReader. You must call getNextChildReader() until
 * hasMoreChildReaders() returns false.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public interface MessageReader {
    public String getValue();

    public boolean isXsiNil();

    public int getValueAsInt();

    public long getValueAsLong();

    public double getValueAsDouble();

    public float getValueAsFloat();

    public boolean getValueAsBoolean();

    public char getValueAsCharacter();

    public MessageReader getAttributeReader(QName qName);

    public boolean hasMoreAttributeReaders();

    public MessageReader getNextAttributeReader();

    public boolean hasMoreElementReaders();

    public MessageReader getNextElementReader();

    public QName getName();

    /**
     * Get the local name of the element this reader represents.
     * 
     * @return Local Name
     */
    public String getLocalName();

    /**
     * @return Namespace
     */
    public String getNamespace();

    public String getNamespaceForPrefix(String prefix);

    public XMLStreamReader getXMLStreamReader();

    public void readToEnd();
}
