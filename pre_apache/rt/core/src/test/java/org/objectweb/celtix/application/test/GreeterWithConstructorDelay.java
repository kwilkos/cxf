package org.objectweb.celtix.application.test;

public class GreeterWithConstructorDelay {
    private static int delay;

    public GreeterWithConstructorDelay() {
        try {
            Thread.sleep(getDelay());
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    private synchronized int getDelay() {
        delay = (delay + 250) % 500;
        return delay;
    }
}
