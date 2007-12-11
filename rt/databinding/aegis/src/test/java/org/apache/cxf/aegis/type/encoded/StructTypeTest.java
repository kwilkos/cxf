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
package org.apache.cxf.aegis.type.encoded;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.type.DefaultTypeMappingRegistry;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.basic.BeanTypeInfo;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;
import org.apache.cxf.aegis.xml.jdom.JDOMWriter;
import org.apache.cxf.aegis.xml.stax.ElementReader;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.helpers.CastUtils;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class StructTypeTest extends AbstractAegisTest {
    private TypeMapping mapping;
    private StructType addressType;
    private StructType purchaseOrderType;
    private TrailingBlocks trailingBlocks;

    public void setUp() throws Exception {
        super.setUp();

        addNamespace("b", "urn:Bean");
        addNamespace("a", "urn:anotherns");
        addNamespace("xsi", SOAPConstants.XSI_NS);

        DefaultTypeMappingRegistry reg = new DefaultTypeMappingRegistry(true);
        mapping = reg.createTypeMapping(true);

        // address type
        BeanTypeInfo addressInfo = new BeanTypeInfo(Address.class, "urn:Bean");
        addressInfo.setTypeMapping(mapping);

        addressType = new StructType(addressInfo);
        addressType.setTypeClass(Address.class);
        addressType.setSchemaType(new QName("urn:Bean", "address"));
        mapping.register(addressType);

        // purchase order type
        BeanTypeInfo poInfo = new BeanTypeInfo(PurchaseOrder.class, "urn:Bean");
        poInfo.setTypeMapping(mapping);

        purchaseOrderType = new StructType(poInfo);
        purchaseOrderType.setTypeClass(PurchaseOrder.class);
        purchaseOrderType.setTypeMapping(mapping);
        purchaseOrderType.setSchemaType(new QName("urn:Bean", "po"));
        mapping.register(purchaseOrderType);

        // serialization root type
        trailingBlocks = new TrailingBlocks();
//        trailingBlocks.setTypeMapping(mapping);

    }

    @Test
    public void testSimpleStruct() throws Exception {
        // Test reading
        ElementReader reader = new ElementReader(getClass().getResourceAsStream("struct1.xml"));
        Address address = (Address) addressType.readObject(reader, new Context());
        validateAddress(address);
        reader.getXMLStreamReader().close();

        // Test reading - no namespace on nested elements
        reader = new ElementReader(getClass().getResourceAsStream("struct2.xml"));
        address = (Address) addressType.readObject(reader, new Context());
        validateAddress(address);
        reader.getXMLStreamReader().close();

        // Test writing
        Element element = new Element("root", "b", "urn:Bean");
        new Document(element);
        addressType.writeObject(address, new JDOMWriter(element), new Context());
        validateShippingAddress(element);
    }

    @Test
    public void testComplexStruct() throws Exception {
        // Test reading
        ElementReader reader = new ElementReader(getClass().getResourceAsStream("struct3.xml"));
        PurchaseOrder po = (PurchaseOrder) purchaseOrderType.readObject(reader, new Context());
        validatePurchaseOrder(po);
        reader.getXMLStreamReader().close();

        // Test reading - no namespace on nested elements
        reader = new ElementReader(getClass().getResourceAsStream("struct4.xml"));
        po = (PurchaseOrder) purchaseOrderType.readObject(reader, new Context());
        validatePurchaseOrder(po);
        reader.getXMLStreamReader().close();

        // Test writing
        Element element = writeRef(po);
        validatePurchaseOrder(element);
    }

    @Test
    public void testStructRef() throws Exception {
        PurchaseOrder purchaseOrder;

        // Simple nested ref
        purchaseOrder = (PurchaseOrder) readRef("ref1.xml");
        validatePurchaseOrder(purchaseOrder);

        // Strings referenced
        purchaseOrder = (PurchaseOrder) readRef("ref2.xml");
        validatePurchaseOrder(purchaseOrder);

        // completely unrolled
        purchaseOrder = (PurchaseOrder) readRef("ref3.xml");
        validatePurchaseOrder(purchaseOrder);

        // Test writing
        Element element = writeRef(purchaseOrder);

        validatePurchaseOrder(element);
    }

    private Object readRef(String file) throws XMLStreamException {
        Context context = new Context();
        context.setTypeMapping(mapping);
        ElementReader root = new ElementReader(getClass().getResourceAsStream(file));

        // get Type based on the element qname
        MessageReader reader = root.getNextElementReader();
        Type type = this.mapping.getType(reader.getName());
        assertNotNull("type is null", type);

        // read ref
        SoapRefType soapRefType = new SoapRefType(type);
        SoapRef ref = (SoapRef) soapRefType.readObject(reader, context);
        reader.readToEnd();

        // read the trailing blocks (referenced objects)
        List<Object> roots = trailingBlocks.readBlocks(root, context);
        assertNotNull(roots);

        // close the input stream
        root.getXMLStreamReader().close();

        // return the ref
        return ref.get();
    }

    private Element writeRef(Object instance) {
        // create the document
        Element element = new Element("root", "b", "urn:Bean");
        new Document(element);
        JDOMWriter rootWriter = new JDOMWriter(element);
        Context context = new Context();
        context.setTypeMapping(mapping);

        // get Type based on the object instance
        Type type = this.mapping.getType(instance.getClass());
        assertNotNull("type is null", type);

        // write the ref
        SoapRefType soapRefType = new SoapRefType(type);
        MessageWriter cwriter = rootWriter.getElementWriter(soapRefType.getSchemaType());
        soapRefType.writeObject(instance, cwriter, context);
        cwriter.close();

        // write the trailing blocks (referenced objects)
        trailingBlocks.writeBlocks(rootWriter, context);
        return element;
    }

    private void validateAddress(Address address) {
        assertNotNull(address);
        assertEquals("1234 Riverside Drive", address.getStreet());
        assertEquals("Gainesville", address.getCity());
        assertEquals("FL", address.getState());
        assertEquals("30506", address.getZip());
    }

    private void validateShippingAddress(Element shipping) {
        assertNotNull("shipping is null", shipping);
        assertChildEquals("1234 Riverside Drive", shipping, "street");
        assertChildEquals("Gainesville", shipping, "city");
        assertChildEquals("FL", shipping, "state");
        assertChildEquals("30506", shipping, "zip");
    }

    private void validateBillingAddress(Element billing) {
        assertNotNull("billing is null", billing);
        assertChildEquals("1234 Fake Street", billing, "street");
        assertChildEquals("Las Vegas", billing, "city");
        assertChildEquals("NV", billing, "state");
        assertChildEquals("89102", billing, "zip");
    }

    private void validatePurchaseOrder(PurchaseOrder purchaseOrder) {
        assertNotNull(purchaseOrder);
        assertNotNull(purchaseOrder.getShipping());
        assertEquals("1234 Riverside Drive", purchaseOrder.getShipping().getStreet());
        assertEquals("Gainesville", purchaseOrder.getShipping().getCity());
        assertEquals("FL", purchaseOrder.getShipping().getState());
        assertEquals("30506", purchaseOrder.getShipping().getZip());
        assertNotNull(purchaseOrder.getBilling());
        assertEquals("1234 Fake Street", purchaseOrder.getBilling().getStreet());
        assertEquals("Las Vegas", purchaseOrder.getBilling().getCity());
        assertEquals("NV", purchaseOrder.getBilling().getState());
        assertEquals("89102", purchaseOrder.getBilling().getZip());
    }

    private void validatePurchaseOrder(Element element) throws Exception {
        Element poRefElement = null;
        Map<String, Element> blocks = new TreeMap<String, Element>();
        for (Content content : CastUtils.<Content>cast(element.getContent())) {
            if (content instanceof Element) {
                Element child = (Element) content;
                if (poRefElement == null) {
                    poRefElement = child;
                } else {
                    String id = getId("Trailing block ", child);
                    blocks.put(id, child);
                }
            }
        }

        Element po = getReferencedElement("poRef", poRefElement, blocks);

        Element shippingRef = po.getChild("shipping");
        Element shipping = getReferencedElement("shipping", shippingRef, blocks);
        validateShippingAddress(shipping);

        Element billingRef = po.getChild("billing");
        Element billing = getReferencedElement("billing", billingRef, blocks);
        validateBillingAddress(billing);
    }

    private Element getReferencedElement(String childName,
            Element element,
            Map<String, Element> blocks) {
        assertNotNull(childName + " is null", element);
        assertNotNull("element is null", element);
        String refId = getRef(childName, element);
        Element refElement = blocks.get(refId);
        assertNotNull(childName + " referenced non-existant element " + refId, refElement);
        return refElement;
    }

    private void assertChildEquals(String expected, Element element, String childName) {
        assertEquals(expected, element.getChild(childName).getText());
    }

    private String getId(String childName, Element child) {
        assertNotNull(childName + " is null", child);
        Attribute idAttribute = child.getAttribute("id");
        XMLOutputter xmlOutputter = new XMLOutputter();
        assertNotNull(childName + " id is null \n" + xmlOutputter.outputString(child), idAttribute);
        String id = idAttribute.getValue();
        assertNotNull(childName + " id is null \n" + xmlOutputter.outputString(child), id);
        return id;
    }

    private String getRef(String childName, Element child) {
        assertNotNull(childName + " is null", child);
        Attribute hrefAttribute = child.getAttribute("href");
        XMLOutputter xmlOutputter = new XMLOutputter();
        assertNotNull(childName + " href is null \n" + xmlOutputter.outputString(child), hrefAttribute);
        String href = hrefAttribute.getValue();
        assertNotNull(childName + " href is null \n" + xmlOutputter.outputString(child), href);
        return href;
    }
}
