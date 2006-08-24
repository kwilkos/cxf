package org.apache.cxf.configuration.impl;

import java.net.URL;

import javax.annotation.Resource;

import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.configuration.ConfigurationProvider;

public class TestProvider implements ConfigurationProvider {

    @Resource()
    URL url;
    
    @Resource()
    Configuration configuration;
    
    @Resource()
    String name;
    
    public Object getObject(String n) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean setObject(String n, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean save() {
        return false;
    }
}
