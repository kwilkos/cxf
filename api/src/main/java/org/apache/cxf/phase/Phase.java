package org.apache.cxf.phase;

public class Phase implements Comparable {
    
    // can be removed from once defined as default value in configuration metadata for bus
    
    // --- out ---
    
    public static final String POST_INVOKE = "post-invoke";
    public static final String PRE_LOGICAL = "pre-logical";
    public static final String USER_LOGICAL = "user-logical";
    public static final String POST_LOGICAL = "post-logical";
    public static final String MARSHAL = "marshal";
    public static final String PRE_PROTOCOL = "pre-protocol";
    public static final String USER_PROTOCOL = "user-protocol";
    public static final String POST_PROTOCOL = "post-protocol";
    public static final String PREPARE_SEND = "prepare-send";
    public static final String PRE_STREAM = "pre-stream";
    public static final String USER_STREAM = "user-stream";
    public static final String POST_STREAM = "port-stream";
    public static final String WRITE = "write";
    public static final String SEND = "send";
    
    // --- in ---
    
    public static final String RECEIVE = "receive";
    public static final String READ = "read";
    public static final String PROTOCOL = "protocol";
    public static final String UNMARSHAL = "unmarshal";
    public static final String PRE_INVOKE = "pre-invoke";
    public static final String INVOKE = "invoke";
    
    
    private String name;
    private int priority;
    
    public Phase() {
    }
    
    public Phase(String n, int p) {
        this.name = n;
        this.priority = p;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String n) {
        this.name = n;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int p) {
        this.priority = p;
    }
    
    public int hashCode() {
        return priority;
    }
    public boolean equals(Object o) {
        Phase p = (Phase)o;
        
        return p.priority == priority
            && p.name.equals(name);
    }

    public int compareTo(Object o) {
        Phase p = (Phase)o;
        
        if (priority == p.priority) {
            return name.compareTo(p.name); 
        }
        return priority - p.priority;
    }
}
