package org.apache.cxf.aegis.type.java5;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.CustomTypeMapping;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.collection.CollectionType;
import org.apache.cxf.aegis.type.collection.MapType;
import org.apache.cxf.aegis.type.java5.dto.MapDTO;
import org.apache.cxf.aegis.type.java5.dto.MapDTOService;

public class MapTest
    extends AbstractAegisTest
{
    private CustomTypeMapping tm;
    private Java5TypeCreator creator;

    public void setUp() throws Exception
    {
        super.setUp();
        
        tm = new CustomTypeMapping();
        creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);
    }

    @SuppressWarnings("unchecked")
    public void testType() throws Exception
    {
        Method m = MapService.class.getMethod("getMap", new Class[0]);
        
        Type type = creator.createType(m, -1);
        tm.register(type);
        assertTrue( type instanceof MapType );
        
        MapType mapType = (MapType) type;
        QName keyName = mapType.getKeyName();
        assertNotNull(keyName);
        
        type = mapType.getKeyType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
        
        type = mapType.getValueType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
    }

    public void testRecursiveType() throws Exception
    {
        Method m = MapService.class.getMethod("getMapOfCollections", new Class[0]);
        
        Type type = creator.createType(m, -1);
        tm.register(type);
        assertTrue( type instanceof MapType );
        
        MapType mapType = (MapType) type;
        QName keyName = mapType.getKeyName();
        assertNotNull(keyName);
        
        type = mapType.getKeyType();
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(String.class, ((CollectionType) type).getComponentType().getTypeClass());
        
        type = mapType.getValueType();
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(Double.class, ((CollectionType) type).getComponentType().getTypeClass());
    }
    
    @SuppressWarnings("unchecked")
    public void testPDType() throws Exception
    {
        PropertyDescriptor pd = 
            Introspector.getBeanInfo(MapDTO.class, Object.class).getPropertyDescriptors()[0];
        Type type = creator.createType(pd);
        tm.register(type);
        assertTrue( type instanceof MapType );
        
        MapType mapType = (MapType) type;
        QName keyName = mapType.getKeyName();
        assertNotNull(keyName);
        
        type = mapType.getKeyType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
        
        type = mapType.getValueType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
    }

    @SuppressWarnings("unchecked")
    public void testMapDTO()
    {
        CustomTypeMapping tm = new CustomTypeMapping();
        Java5TypeCreator creator = new Java5TypeCreator();
        creator.setConfiguration(new Configuration());
        tm.setTypeCreator(creator);
        
        Type dto = creator.createType(MapDTO.class);
        Set deps = dto.getDependencies();
        
        Type type = (Type) deps.iterator().next();
        assertTrue( type instanceof MapType );
        
        MapType mapType = (MapType) type;
        
        deps = dto.getDependencies();
        assertEquals(1, deps.size());
        
        type = mapType.getKeyType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(String.class));
        
        type = mapType.getValueType();
        assertNotNull(type);
        assertTrue(type.getTypeClass().isAssignableFrom(Integer.class));
    }
    
    public void testMapDTOService() throws Exception
    {
        createService(MapDTOService.class, null);
        
        invoke("MapDTOService", 
               "/org/apache/cxf/aegis/type/java5/dto/GetDTO.xml");
    }


    public void testMapServiceWSDL() throws Exception
    {
        createService(MapDTOService.class, null);
        
        getWSDLDocument("MapDTOService");
    }
    
    public class MapService
    {
        public Map<String,Integer> getMap()
        {
        	return null;
        }
        
        public void setMap(Map<String,Integer> strings) {
        	
        }
        
        public Map<Collection<String>,Collection<Double>> getMapOfCollections()
        {
            return null;
        }
    }
}
