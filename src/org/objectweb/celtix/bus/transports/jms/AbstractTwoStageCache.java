package org.objectweb.celtix.bus.transports.jms;

import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.List;


/**
 * This class pools objects, for efficiency accross a lightweight
 * fixed-size primary cache and a variable-size secondary cache - the
 * latter uses soft references to allow the polled object be GCed if
 * necessary.
 * <p>
 * To use the cache, a subclass is defined which provides an implementation
 * of the abstract get() method - this may be conveniently achieved via
 * an anonymous subclass. The cache is then populated by calling the
 * populate_cache() method - the reason a two-stage process is used is
 * to avoid problems with the inner class create() method accessing outer
 * class data members from the inner class ctor (before its reference to
 * the outer class is initialized).
 * <p>
 *
 * @author Eoghan Glynn
 */
public abstract class AbstractTwoStageCache {
    private Object mutex;
    private int preallocation;
    private Object primaryCache[];
    private int primaryCacheSize;
    private int primaryCacheNextFree;
    private int secondaryCacheHighWaterMark;
    private List secondaryCache;

    /**
     * Constructor.
     *
     * @param pCacheSize primary cache size
     * @param secondary_cache_max secondary cache high water mark
     * @param preallocation the number of object to preallocation when the
     * cache is created
     */
    public AbstractTwoStageCache(int pCacheSize, int highWaterMark, int prealloc) {
        this(pCacheSize, highWaterMark, prealloc, null);
    }


    /**
     * Constructor.
     *
     * @param pCacheSize primary cache size
     * @param secondary_cache_max secondary cache high water mark
     * @param preallocation the number of object to preallocation when the
     * cache is created
     * @param mutex object to use as a monitor
     */
    public AbstractTwoStageCache(int pCacheSize, int highWaterMark, int prealloc, Object mutexParam) {
        this.primaryCacheSize = Math.min(pCacheSize, highWaterMark);
        this.secondaryCacheHighWaterMark = highWaterMark - pCacheSize;
        this.preallocation = prealloc;
        this.mutex = mutexParam != null ? mutexParam : this;
    }

    public String toString() {
        return "AbstractTwoStageCache";
    }


    /**
     * Over-ride this method to create objects to populate the pool
     *
     * @return newly created object
     */
    protected abstract Object create() throws Throwable;


    /**
     * Populate the cache
     */
    public void populateCache() throws Throwable {
        // create cache
        primaryCache = new Object[primaryCacheSize];

        // preallocate objects into primary cache
        int primaryCachePreallocation = 
            (preallocation > primaryCacheSize) ? primaryCacheSize : preallocation;

        primaryCacheNextFree = primaryCacheSize - primaryCachePreallocation;

        for (int i = primaryCacheNextFree; i < primaryCacheSize; i++) {
            primaryCache[i] = create();
        }

        // preallocate objects into secondary cache
        secondaryCache = new LinkedList();

        int secondaryCachePreallocation = preallocation - primaryCachePreallocation;

        for (int i = 0; i < secondaryCachePreallocation; i++) {
            secondaryCache.add(new SoftReference(create()));
        }
    }


    /**
     * Return a cached or newly created object
     *
     * @return an object
     */
    public Object get() throws Throwable {
        Object ret = null;

        synchronized (mutex) {
            if (primaryCache != null) {
                if (primaryCacheNextFree < primaryCacheSize) {
                    ret = primaryCache[primaryCacheNextFree];
                    primaryCache[primaryCacheNextFree++] = null;
                }

                if ((ret == null) && (secondaryCache.size() > 0)) {
                    ret = ((SoftReference) ((LinkedList) secondaryCache).removeFirst()).get();
                }
            }
        }

        if (ret == null) {
            ret = create();
        }

        return ret;
    }


    /**
     * Return a cached object if one is available
     *
     * @return an object
     */
    public Object poll() {
        Object ret = null;

        synchronized (mutex) {
            if (primaryCache != null) {
                if (primaryCacheNextFree < primaryCacheSize) {
                    ret = primaryCache[primaryCacheNextFree];
                    primaryCache[primaryCacheNextFree++] = null;
                }

                if ((ret == null) && (secondaryCache.size() > 0)) {
                    ret = ((SoftReference) ((LinkedList)secondaryCache).removeFirst()).get();
                }
            }
        }

        return ret;
    }


    /**
     * Recycle an old Object.
     *
     * @param oldObject the object to recycle
     * @return true iff the object can be accomodated in the cache
     */
    public boolean recycle(Object oldObject) {
        boolean cached = false;

        synchronized (mutex) {
            if (primaryCache != null) {
                if (primaryCacheNextFree > 0) {
                    primaryCache[--primaryCacheNextFree] = oldObject;
                    cached = true;
                }

                if (!cached && (secondaryCache.size() < secondaryCacheHighWaterMark)) {
                    secondaryCache.add(new SoftReference(oldObject));
                    cached = true;
                }
            }
        }

        return cached;
    }
}
