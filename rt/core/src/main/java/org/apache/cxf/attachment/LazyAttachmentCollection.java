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
package org.apache.cxf.attachment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.message.Attachment;

public class LazyAttachmentCollection implements Collection<Attachment> {
    private AttachmentDeserializer deserializer;
    private final List<Attachment> attachments = new ArrayList<Attachment>();
    
    public LazyAttachmentCollection(AttachmentDeserializer deserializer) {
        super();
        this.deserializer = deserializer;
    }

    public List<Attachment> getLoadedAttachments() {
        return attachments;
    }

    private void loadAll() {
        try {
            Attachment a = deserializer.readNext();
            while (a != null) {
                attachments.add(a);
                a = deserializer.readNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<Attachment> iterator() {
        return new Iterator<Attachment>() {
            int current;
            
            public boolean hasNext() {
                if (attachments.size() > current) {
                    return true;
                }
                
                // check if there is another attachment
                try {
                    Attachment a = deserializer.readNext();
                    if (a == null) {
                        return false;
                    } else {
                        attachments.add(a);
                        return true;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public Attachment next() {
                Attachment a = attachments.get(current);
                current++;
                return a;
            }

            public void remove() {
                attachments.remove(current);
            }
            
        };
    }
    
    public int size() {
        loadAll();
        
        return attachments.size();
    }

    public boolean add(Attachment arg0) {
        return attachments.add(arg0);
    }

    public boolean addAll(Collection<? extends Attachment> arg0) {
        return attachments.addAll(arg0);
    }

    public void clear() {
        attachments.clear();   
    }

    public boolean contains(Object arg0) {
        return attachments.contains(arg0);
    }

    public boolean containsAll(Collection<?> arg0) {
        return attachments.containsAll(arg0);
    }

    public boolean isEmpty() {
        return attachments.isEmpty();
    }

    public boolean remove(Object arg0) {
        return attachments.remove(arg0);
    }

    public boolean removeAll(Collection<?> arg0) {
        return attachments.removeAll(arg0);
    }

    public boolean retainAll(Collection<?> arg0) {
        return attachments.retainAll(arg0);
    }

    public Object[] toArray() {
        loadAll();
        
        return attachments.toArray();
    }

    public <T> T[] toArray(T[] arg0) {
        loadAll();
        
        return attachments.toArray(arg0);
    }



}
