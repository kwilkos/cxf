package org.apache.cxf.aegis.type.java5.dto;

import java.util.HashMap;

public class MapDTOService
{
    public MapDTO getDTO()
    {
    	MapDTO dto = new MapDTO();
        
        HashMap<String,Integer> strings = new HashMap<String,Integer>();
        strings.put("hi", 4);
        dto.setStrings(strings);
        
        return dto;
    }
}