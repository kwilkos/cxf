package org.apache.cxf.aegis.type.java5.dto;

import java.util.List;

public class ObjectDTO
{
    List<? extends Object> objects;

    public List<? extends Object> getObjects()
    {
        return objects;
    }

    public void setObjects(List<? extends Object> objects)
    {
        this.objects = objects;
    }
}