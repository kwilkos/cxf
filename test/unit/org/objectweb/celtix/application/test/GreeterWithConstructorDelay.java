package org.objectweb.celtix.application.test;

public class GreeterWithConstructorDelay {
    private static int delay;

    public GreeterWithConstructorDelay() {
        delay = (delay + 250) % 500;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
