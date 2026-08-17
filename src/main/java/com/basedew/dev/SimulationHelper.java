package com.basedew.dev;

public class SimulationHelper {

    public static void delay(long ms){
        sleep(ms);
    }

    public static void delayRand(long msUpperLimit){
        long delay = (long) Math.floor(Math.random() * msUpperLimit);
        sleep(delay);
    }

    public static void delayRand(long msLowerLimit, long msUpperLimit){
        long delay = msLowerLimit + (long) Math.floor(Math.random() * (msUpperLimit - msLowerLimit));
        sleep(delay);
    }

    private static void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
