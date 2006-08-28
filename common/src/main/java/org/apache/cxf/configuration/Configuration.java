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

package org.apache.cxf.configuration;

import java.util.List;

public interface Configuration {
   
    /** 
     * Returns the identifier for this configuration instance (unique within all instances
     * configuration instances for the same metadata model).
     * 
     * @return the name for this configuration.
     */
    CompoundName getId();
    
    /**
     * Returns the configuration metadata model for this <code>Configuration</code>.
     * 
     * @return the configuration metadata model.
     */
    ConfigurationMetadata getModel();
    
    /**
     * Sets the list of configuration providers for this configuration - these will be 
     * consulted in orde when looking up the value for a particular configuration item.
     * 
     * @param providers the configuration providers to use for this configuration.
     */
    void setProviders(List<ConfigurationProvider> providers);
    
    /**
     * Returns the list of configuration providers for this configuration.
     * 
     * @return the list of configuration providers for this configuration.
     */
    List<ConfigurationProvider> getProviders();
    
    /**
     * Returns the object holding the value for the configuration item with the specified name. 
     * The runtime class of this object is determined by the jaxb mapping of the configuration
     * item's type, e.g. for a boolean item it is an instance of java.lang.Boolean.
     * 
     * @throws ConfigurationException if no such item is defined in this configuration's 
     * metadata model, or if no value for this item can be found in either this configuration
     * or any of its parent configuration's and if no default value is specified for the
     * item in the metadata model. 
     * 
     * @param name the name of the configuration item.
     * @return the object holding the configuration item's value.
     */   
    Object getObject(String name);
    
    /**
     * Changes the value of the configuration item identified by the name to the given value.
     * @throws ConfigurationException if no such item is defined in this configuration's 
     * metadata model, or if the value is illegal. Returns true if the change was accepted.
     * 
     * @param name the name of the configuration item.
     * @param value the new value for the configuration item.
     * @return true if the change was accepted.
     */
    boolean setObject(String name, Object value);
    
    /**
     * Returns the object holding the value for the configuration item with the specified name. 
     * The runtime class of this object is determined by the jaxb mapping of the configuration
     * item's type, e.g. for a boolean item it is an instance of java.lang.Boolean.
     * 
     * @throws ConfigurationException if no such item is defined in this configuration's 
     * metadata model, or if no value for this item can be found in either this configuration
     * or any of its parent configuration's and if no default value is specified for the
     * item in the metadata model. 
     * 
     * @param name the name of the configuration item.
     * @param cls the class of the configuration item.
     * @return the object holding the configuration item's value.
     */   
    <T> T getObject(Class<T> cls, String name);
    
    /** Convenience method to extract the value of a boolean type configuration item from
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    boolean getBoolean(String name);
    
    /** Convenience method to set the value of a boolean type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setBoolean(String name, boolean value);

    /** Convenience method to extract the value of a short type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    short getShort(String name);
    
    /** Convenience method to set the value of a short type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setShort(String name, short value);

    /** Convenience method to extract the value of an int type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    int getInt(String name);
  
    /** Convenience method to set the value of a int type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setInt(String name, int value);
    
    /** Convenience method to extract the value of a long type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    long getLong(String name);
    
    /** Convenience method to set the value of a long type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setLong(String name, long value);

    /** Convenience method to extract the value of a float type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    float getFloat(String name);
    
    /** Convenience method to set the value of a float type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setFloat(String name, float value);
    
    /** Convenience method to extract the value of a double type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    double getDouble(String name);
    
    /** Convenience method to set the value of a double type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setDouble(String name, double value);

    /** Convenience method to extract the value of a string type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    String getString(String name);
    
    /** Convenience method to set the value of a String type configuration item to
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @param value the value of the configuration item.
     * @return true if the change was accepted.
     */
    boolean setString(String name, String value);

    /** Convenience method to extract the value of a string list type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    List<String> getStringList(String name);

}
