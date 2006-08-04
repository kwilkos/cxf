package org.objectweb.celtix.bus.configuration.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.celtix.configuration.Configuration;

class BeanName {
    
    static final char LOOSE_BINDING = '*';
    static final char TIGHT_BINDING = '.';    
    static final char NAMESPACE_URI_OPEN = '{';
    static final char NAMESPACE_URI_CLOSE = '}';
    
    static final String ANY_COMPONENT = "?";
    
    String name;
    String normalisedName;
    ComponentIterator iterator;    
    
    BeanName(String n) {
        this(n, false);
    }
    
    BeanName(String n, boolean doNormalise) {
        name = n;
        normalisedName = null;
        if (doNormalise) {
            normalise();
        }
    }   
    
    BeanName(Configuration conf) {
        StringBuffer buf = new StringBuffer();
        Configuration c = conf;
        while (null != c) {
            if (buf.length() > 0) {
                buf.insert(0, TIGHT_BINDING);
            }
            buf.insert(0, c.getId().toString());
            c = c.getParent();
        }
        name = buf.toString();
        normalisedName = name;
    }
    
    public String toString() {
        return name;
    }  
    
    int compareTo(BeanName other, String component) {

        int result = 0;
        
        // An entry that contains a matching component (whether name or  "?")
        // takes precedence over entries that elide the level (that is, entries
        // that match the level in a loose binding).
        
        char lb = iterator.lastBinding();
        char olb = other.iterator.lastBinding();
       
        
        if (matchCurrentComponentName(component)) {
            if (!other.matchCurrentComponentName(component) && LOOSE_BINDING == olb) {
                result = -1;
            }
        } else if (other.matchCurrentComponentName(component) && LOOSE_BINDING == lb) {
            return 1;           
        }
        
        if (0 != result) {
            return result;
        }
        
        // An entry with a matching name takes precedence over
        // entries that match using "?".

        String c = iterator.current;
        String oc = other.iterator.current;
        
        if (ANY_COMPONENT.equals(c)) {
            if (!ANY_COMPONENT.equals(oc)) {
                result = 1;
            }
        } else {
            if (ANY_COMPONENT.equals(oc)) {
                result = -1;
            }
        }
        if (0 != result) {
            return result;
        }
        
        // An entry preceded by a tight binding takes precedence
        // over entries preceded by a loose binding.   
        
        if (isTightBinding(lb) && isLooseBinding(olb)) {
            result = -1;
        } else if (isLooseBinding(lb) && isTightBinding(olb)) {
            result = 1;
        }
        return result;
    }

    String getName() {
        return name;
    }
    
    String getNormalisedName() {
        return normalisedName;
    }
    
    BeanName findBestMatch(List<BeanName> candidateBeans) {
 
        List<BeanName> candidates = new ArrayList<BeanName>(candidateBeans);
        
        for (BeanName bn : candidates) {
            if (name.equals(bn.name)) {
                return bn;
            }
            bn.normalise();
            bn.reset();
            if (bn.iterator.hasNext()) {
                bn.iterator.next();
            }
        }
        
        normalise();
        reset();
        
        while (iterator.hasNext() && candidates.size() > 0) {
            
            iterator.next();            
            
            // at each level:
            
            // remove the non matching candidates
            
            for (int i = candidates.size() - 1; i >= 0; i--) {
                BeanName candidate = candidates.get(i);
                if (!match(candidate)) {
                    candidates.remove(i);
                }
            }
            
            // sort the remainder - using a comparator specific to the current level

            Comparator<BeanName> comparator = new Comparator<BeanName>() {
                public int compare(BeanName o1, BeanName o2) {
                    return o1.compareTo(o2, iterator.current);
                }   
            };
            Collections.sort(candidates, comparator);
            
            // keep only the ties
            
            int i = 1;
            while (i < candidates.size()) {
                int diff = candidates.get(0).compareTo(candidates.get(i), 
                                                       iterator.current);                    
                if (diff < 0) {
                    break;
                } 
                i++;
            }
            while (i < candidates.size()) {
                candidates.remove(candidates.size() - 1);      
            }
            
            
            // advance remaining candidate iterators where necessary,
            // pruning the list if necessary
            
            ListIterator<BeanName> it = candidates.listIterator();
            BeanName candidate = it.hasNext() ? it.next() : null;
            while (null != candidate) {
                BeanName nextCandidate = it.hasNext() ? it.next() : null;
                char lb = candidate.iterator.lastBinding();
                if (0 == lb || isTightBinding(lb)) {
                    if (candidate.iterator.hasNext()) {
                        candidate.iterator.next();
                    } else if (iterator.hasNext()) {
                        candidates.remove(candidate);
                    }
                } else if (matchCurrentComponentName(candidate)) {
                    assert isLooseBinding(lb);
                    if (candidate.iterator.hasNext()) {
                        candidate.iterator.next();
                    } else if (iterator.hasNext()) {
                        candidates.remove(candidate);
                    }
                }
                candidate = nextCandidate;
            }
        } 
        if (candidates.size() > 0) {
            return candidates.get(0);
        }
        return null;
    }
    
    
    boolean match(BeanName other) {
        char olb = other.iterator.lastBinding();
        if (isLooseBinding(olb)) {
            return true;
        } else {
            return matchCurrentComponentName(other);
        }
    }
    
    boolean matchCurrentComponentName(BeanName other) {
        return iterator.current.equals(other.iterator.current) 
            || ANY_COMPONENT.equals(other.iterator.current);
    }
    
    boolean matchCurrentComponentName(String component) {
        return component.equals(iterator.current) 
            || ANY_COMPONENT.equals(iterator.current);
    }
    
    /**
     * Replace contiguous sequences of two or more binding characters by  
     * a single tight binding if the sequence contains only tight binding characters,
     * or by a loose binding character otherwise.
     * @param beanName
     * @return
     */
    
    final void normalise() {
        if (null != normalisedName) {
            return;
        }
        if (null == name || name.length() < 2) {
            normalisedName = name;
            return;
        }
        StringBuffer buf = new StringBuffer();
        
        int i = 0;
        int from = 0;        
        while (i < name.length()) { 
            boolean inNamespaceURI = false;
            from = i;
            if (isBinding(name.charAt(i))) {
                while (i < name.length() && isBinding(name.charAt(i))) {
                    i++;
                }
                if (i - from > 1) {  
                    int pos = name.indexOf(LOOSE_BINDING, from);
                    if (pos >= 0 && pos <= i) {
                        buf.append(LOOSE_BINDING);
                        
                    } else {
                        buf.append(TIGHT_BINDING);
                    }
                } else {
                    buf.append(name.charAt(from));
                }
                
            } else {
                do {
                    buf.append(name.charAt(i));
                    if (NAMESPACE_URI_OPEN == name.charAt(i) && !inNamespaceURI) {
                        inNamespaceURI = true;
                    } else if (NAMESPACE_URI_CLOSE == name.charAt(i) && inNamespaceURI) {
                        inNamespaceURI = false;
                    }
                    i++;
                } while (i < name.length() && (!isBinding(name.charAt(i)) || inNamespaceURI));
            }
        }
       
        normalisedName = buf.toString();
    }
    
    final void reset() {
        iterator = new ComponentIterator();
    }
    
    final ComponentIterator getIterator() {
        if (null == iterator) {
            iterator = new ComponentIterator();
        }
        return iterator;
    }
    
    
    static boolean isBinding(char c) {
        return isTightBinding(c) || isLooseBinding(c);
    }
    
    static boolean isTightBinding(char c) {
        return TIGHT_BINDING == c;
    }
    
    static boolean isLooseBinding(char c) {
        return LOOSE_BINDING == c;
    }
    
    final class ComponentIterator implements Iterator<String> {
        int componentStart = -1;
        int componentEnd = -1;
        String current;
        
        public boolean hasNext() {
            if (null == normalisedName) {
                return false;
            }
            return componentEnd + 1 < normalisedName.length();
        }

        public String next() {
            if (-1 == componentStart && -1 == componentEnd && isBinding(normalisedName.charAt(0))) {
                componentStart++;
                componentEnd++;
            }
            componentStart = componentEnd + 1;
            componentEnd = componentStart + 1;
            boolean inNamespaceURI = false;
            if (componentStart < normalisedName.length()
                && NAMESPACE_URI_OPEN == normalisedName.charAt(componentStart)) {
                inNamespaceURI = true;
            }
            
            while (componentEnd < normalisedName.length()) {
                if (NAMESPACE_URI_OPEN == normalisedName.charAt(componentEnd) && !inNamespaceURI) {
                    inNamespaceURI = true;
                } else if (NAMESPACE_URI_CLOSE == normalisedName.charAt(componentEnd) && inNamespaceURI) {
                    inNamespaceURI = false;
                }
                if (!inNamespaceURI && isBinding(normalisedName.charAt(componentEnd)))
                {
                    break; 
                }
                componentEnd++;
            }
 
            /*
            while (componentEnd < normalisedName.length() 
                && !isBinding(normalisedName.charAt(componentEnd))) {
                componentEnd++;
            }
            */
            current = normalisedName.substring(componentStart, componentEnd);
            return current;
        }

        public void remove() {
            // TODO Auto-generated method stub       
        }
        
        char lastBinding() {
            if (componentStart > 0) {
                return normalisedName.charAt(componentStart - 1);
            }
            return 0;
        }
        
        
    }
}
