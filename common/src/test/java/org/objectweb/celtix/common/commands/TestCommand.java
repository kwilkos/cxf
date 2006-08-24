package org.apache.cxf.common.commands;

public class TestCommand {
    
    private int result;
    private int duration;
    private String err;
    private String out;
    
    public TestCommand(String[] args) {
        int i = 0;
        while (i < args.length) {
            if ("-duration".equals(args[i]) && i < (args.length - 1)) {
                i++;
                try {
                    duration = Integer.parseInt(args[i]);
                } catch (NumberFormatException ex) {
                    // leave at default
                }
            } else if ("-result".equals(args[i]) && i < (args.length - 1)) {
                i++;
                try {
                    result = Integer.parseInt(args[i]);
                } catch (NumberFormatException ex) {
                    // leave at default
                } 
            } else if ("-err".equals(args[i]) && i < (args.length - 1)) {
                i++;               
                err = args[i];             
            } else if ("-out".equals(args[i]) && i < (args.length - 1)) {
                i++;
                out = args[i];
            } else {
                result = -1;
                System.err.println("Usage: TestCommand [-duration <duration>] [-result <result>]" 
                                   + "                   [-out <out>] [-err <err>]");
                break;
            }
            i++;
        }
    }
    
    void execute() {
       
        if (null != out) {
            System.out.println(out);
        }
        if (null != err) {
            System.err.println(err);
        }
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException ex) {
            // ignore
        }
        System.exit(result); 
    }
    
    public static void main(String[] args) {
        TestCommand tc = new TestCommand(args);
        tc.execute();
    }
}
