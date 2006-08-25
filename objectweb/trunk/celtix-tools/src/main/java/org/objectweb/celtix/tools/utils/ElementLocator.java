package org.objectweb.celtix.tools.utils;


public class ElementLocator {

    private int line;
    private int column;

    public ElementLocator(int l, int c) {
        this.line = l;
        this.column = c;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
    
   
    

}
