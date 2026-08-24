package com.project.Controller;

public class ThreadSleeper implements Sleeper {
    public void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
