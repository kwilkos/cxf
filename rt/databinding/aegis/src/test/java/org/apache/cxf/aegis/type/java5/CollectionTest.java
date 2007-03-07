package org.apache.cxf.aegis.type.java5;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.CustomTypeMapping;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.collection.CollectionType;
import org.apache.cxf.aegis.type.java5.dto.CollectionDTO;
import org.apache.cxf.aegis.type.java5.dto.DTOService;
import org.apache.cxf.aegis.type.java5.dto.ObjectDTO;
import org.apache.cxf.transport.local.LocalTransportFactory;

public class CollectionTest extends AbstractAegisTest {
    private CustomTypeMapping tm;
    private Java5TypeCreator creator;

    public void setUp() throws Exception {
        super.setUp();

        tm = new CustomTypeMapping();
        creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);
    }

    @SuppressWarnings("unchecked")
    public void testType() throws Exception {
        Method m = CollectionService.class.getMethod("getStrings", new Class[0]);

        Type type = creator.createType(m, -1);
        tm.register(type);
        assertTrue(type instanceof CollectionType);

        CollectionType colType = (CollectionType)type;
        QName componentName = colType.getSchemaType();

        assertEquals("ArrayOfString", componentName.getLocalPart());
        assertEquals("ArrayOfString", componentName.getLocalPart());

        type = colType.getComponentType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
    }

    @SuppressWarnings("unchecked")
    public void testRecursiveCollections() throws Exception {
        Method m = CollectionService.class.getMethod("getStringCollections", new Class[0]);

        Type type = creator.createType(m, -1);
        tm.register(type);
        assertTrue(type instanceof CollectionType);

        CollectionType colType = (CollectionType)type;
        QName componentName = colType.getSchemaType();

        assertEquals("ArrayOfArrayOfString", componentName.getLocalPart());

        type = colType.getComponentType();
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);

        CollectionType colType2 = (CollectionType)type;
        componentName = colType2.getSchemaType();

        assertEquals("ArrayOfString", componentName.getLocalPart());

        type = colType2.getComponentType();
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
    }

    @SuppressWarnings("unchecked")
    public void testPDType() throws Exception {
        PropertyDescriptor pd = Introspector.getBeanInfo(CollectionDTO.class, Object.class)
            .getPropertyDescriptors()[0];
        Type type = creator.createType(pd);
        tm.register(type);
        assertTrue(type instanceof CollectionType);

        CollectionType colType = (CollectionType)type;

        type = colType.getComponentType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
    }

    public void testCollectionDTO() {
        CustomTypeMapping tm = new CustomTypeMapping();
        Java5TypeCreator creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);

        Type dto = creator.createType(CollectionDTO.class);
        Set deps = dto.getDependencies();

        Type type = (Type)deps.iterator().next();

        assertTrue(type instanceof CollectionType);

        CollectionType colType = (CollectionType)type;

        deps = dto.getDependencies();
        assertEquals(1, deps.size());

        Type comType = colType.getComponentType();
        assertEquals(String.class, comType.getTypeClass());
    }

    public void testObjectDTO() {
        CustomTypeMapping tm = new CustomTypeMapping();
        Java5TypeCreator creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);

        Type dto = creator.createType(ObjectDTO.class);
        Set deps = dto.getDependencies();

        assertFalse(deps.isEmpty());

        Type type = (Type)deps.iterator().next();

        assertTrue(type instanceof CollectionType);

        CollectionType colType = (CollectionType)type;

        deps = dto.getDependencies();
        assertEquals(1, deps.size());

        Type comType = colType.getComponentType();
        assertEquals(Object.class, comType.getTypeClass());
    }

    public void testCollectionDTOService() throws Exception {
        createService(DTOService.class, null);
        invoke("DTOService", 
               LocalTransportFactory.TRANSPORT_ID,
               "/org/apache/cxf/aegis/type/java5/dto/GetDTO.xml");
    }

    public void testCollectionServiceWSDL() throws Exception {
        createService(CollectionService.class, null);

        Document wsdl = getWSDLDocument("CollectionService");
        assertValid("//xsd:element[@name='return'][@type='ArrayOfString']", wsdl);
    }

    public void testUnannotatedStrings() throws Exception {
        createService(CollectionService.class, null);
        
        Document doc = getWSDLDocument("CollectionService");
        // printNode(doc);
        assertValid("//xsd:element[@name='getUnannotatedStringsResponse']/xsd:complexType/xsd:sequence/xsd:element[@type='ArrayOfString']",
                    doc);
    }

    public class CollectionService {
        public Collection<String> getStrings() {
            return null;
        }

        public void setLongs(Collection<Long> longs) {
        }

        public Collection getUnannotatedStrings() {
            return null;
        }

        public Collection<Collection<String>> getStringCollections() {
            return null;
        }
    }
}
