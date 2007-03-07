package org.apache.cxf.aegis.type.java5.dto;

import java.util.ArrayList;

public class DTOService
{
    public CollectionDTO getDTO()
    {
        CollectionDTO dto = new CollectionDTO();
        
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("hi");
        dto.setStrings(strings);
        
        return dto;
    }
}