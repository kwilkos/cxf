package org.apache.cxf.aegis.type.java5.dto;

import java.util.Map;

public class MapDTO
{
    Map<String,Integer> strings;

    public Map<String,Integer> getStrings()
    {
        return strings;
    }

    public void setStrings(Map<String,Integer> strings)
    {
        this.strings = strings;
    }
}