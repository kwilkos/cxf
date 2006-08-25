package org.objectweb.celtix.common.util;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class TwoStageCacheTest extends TestCase {

    public TwoStageCacheTest(String arg0) {
        super(arg0);
    }

    public void testToString() {
        TestTwoStageCache cache = new TestTwoStageCache(3, 5, 0);
        assertEquals("AbstractTwoStageCache", cache.toString());
    }
    
    /*
     * Test method for 'org.objectweb.celtix.common.util.AbstractTwoStageCache.get()'
     */
    public void testGet() throws Throwable {
        TestTwoStageCache cache = new TestTwoStageCache(3, 5, 0);
        cache.populateCache();

        for (int x = 0; x < 10; x++) {
            assertNotNull(cache.get());
        }
        
        cache = new TestTwoStageCache(3, 5, 5);
        cache.populateCache();

        for (int x = 0; x < 10; x++) {
            assertNotNull(cache.get());
        }
    }

    /*
     * Test method for 'org.objectweb.celtix.common.util.AbstractTwoStageCache.poll()'
     */
    public void testPoll() throws Throwable {
        TestTwoStageCache cache = new TestTwoStageCache(3, 5, 0);
        cache.populateCache();
        int count = 0;
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(0, count);
        
        cache = new TestTwoStageCache(3, 5, 3);
        cache.populateCache();
        count = 0;
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(3, count);

        cache = new TestTwoStageCache(3, 5, 5);
        cache.populateCache();
        count = 0;
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(5, count);

        // try to prealloc more than high water mark...
        cache = new TestTwoStageCache(3, 5, 9);
        cache.populateCache();
        count = 0;
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(5, count);    
        
        
    }

    /*
     * Test method for 'org.objectweb.celtix.common.util.AbstractTwoStageCache.recycle(E)'
     */
    public void testRecycle() throws Throwable {
        TestTwoStageCache cache = new TestTwoStageCache(3, 8, 5, new Object());
        cache.populateCache();

        Object objs[] = new Object[10];
        
        for (int x = 0; x < 10; x++) {
            objs[x] = cache.get();
        }
        for (int x = 0; x < 10; x++) {
            cache.recycle(objs[x]);
        }
        int count = 0;
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(8, count);    

        count = 0;
        for (int x = 0; x < 10; x++) {
            cache.recycle(objs[x]);
            objs[x] = null;
            System.gc();
        }
        objs = null;
        List<byte[]> list = new LinkedList<byte[]>();
        int allocCount = 0;
        try {
            while (allocCount++ < 1000) {
                list.add(new byte[25000 * allocCount]);
                System.gc();
            }
            fail("cannot trigger OutOfMemoryError within a reasonable timeframe"); 
        } catch (OutOfMemoryError ex) {
            list = null;
        }
        cache.recycle(cache.create());
        
        System.gc();
        while (cache.poll() != null) {
            count++;
        }
        assertEquals(4, count);    
    
    }

    
    static class TestTwoStageCache extends AbstractTwoStageCache<Object> {
        public TestTwoStageCache(int pCacheSize, int highWaterMark, int prealloc) {
            super(pCacheSize, highWaterMark, prealloc);
        }
        public TestTwoStageCache(int pCacheSize, int highWaterMark,
                                 int prealloc, Object mutex) {
            super(pCacheSize, highWaterMark, prealloc, mutex);
        }
        public Object create() {
            return new Object();
        }        
    }
}
