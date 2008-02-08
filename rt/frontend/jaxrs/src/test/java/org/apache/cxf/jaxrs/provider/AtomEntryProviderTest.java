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

package org.apache.cxf.jaxrs.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.stax.FOMEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomEntryProviderTest extends Assert {

    private AtomEntryProvider afd;
    
    @Before
    public void setUp() {
        afd = new AtomEntryProvider();
    }
    
    @Test
    public void testReadFrom() throws Exception {
        InputStream is = getClass().getResourceAsStream("atomEntry.xml");
        Entry simple = afd.readFrom(Entry.class, null, null, is);
        assertEquals("Wrong entry title", 
                     "Atom-Powered Robots Run Amok", simple.getTitle());
        
    }
    
    @Test
    public void testWriteTo() throws Exception {
        InputStream is = getClass().getResourceAsStream("atomEntry.xml");
        Entry simple = afd.readFrom(Entry.class, null, null, is);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        afd.writeTo(simple, null, null, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Entry simpleCopy = afd.readFrom(Entry.class, null, null, bis);
        assertEquals("Wrong entry title", 
                     "Atom-Powered Robots Run Amok", simpleCopy.getTitle());
        assertEquals("Wrong entry title", 
                     simple.getTitle(), simpleCopy.getTitle());
    }
    
    @Test
    public void testSupports() {
        assertTrue(afd.supports(Entry.class));
        assertTrue(afd.supports(FOMEntry.class));
        assertFalse(afd.supports(Feed.class));
    }
    
    @Test
    public void testAnnotations() {
        assertEquals("application/atom+xml",
                     afd.getClass().getAnnotation(ProduceMime.class).value()[0]);
        assertEquals("application/atom+xml",
                     afd.getClass().getAnnotation(ConsumeMime.class).value()[0]);
    }
    
}
