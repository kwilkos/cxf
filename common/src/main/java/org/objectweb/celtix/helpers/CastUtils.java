package org.objectweb.celtix.helpers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class CastUtils {
    
    private CastUtils() {
        //utility class, never constructed
    }
    
    public static <T, U> Map<T, U> cast(Map<?, ?> p) {
        return (Map<T, U>)p;
    }
    public static <T, U> Map<T, U> cast(Map<?, ?> p, Class<T> t, Class<U> u) {
        return (Map<T, U>)p;
    }
    
    public static <T> Collection<T> cast(Collection<?> p) {
        return (Collection<T>)p;
    }
    public static <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>)p;
    }

    public static <T> Set<T> cast(Set<?> p) {
        return (Set<T>)p;
    }
    public static <T> Set<T> cast(Set<?> p, Class<T> cls) {
        return (Set<T>)p;
    }

    public static <T, U> Map.Entry<T, U> cast(Map.Entry<?, ?> p) {
        return (Map.Entry<T, U>)p;
    }
    public static <T, U> Map.Entry<T, U> cast(Map.Entry<?, ?> p, Class<T> pc, Class<U> uc) {
        return (Map.Entry<T, U>)p;
    }

    
}
