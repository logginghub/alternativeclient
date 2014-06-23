package com.logginghub.utils;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);
    private static final long SPIN_YIELD_PRECISION = TimeUnit.NANOSECONDS.toNanos(10000);

    private static Random random = new Random();

    public static void randomSleep(long greaterThatMillis, long lessThanMillis) {
        long time = greaterThatMillis + random.nextInt((int) (lessThanMillis - greaterThatMillis));
        sleep(time);
    }

    public static void sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void sleep(int milliseconds, int nanoseconds) {
        try {
            Thread.sleep(milliseconds, nanoseconds);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // http://stackoverflow.com/questions/5274619/investigation-of-optimal-sleep-time-calculation-in-game-loop
    public static void sleepNanos(long nanoDuration) throws InterruptedException {
        final long end = System.nanoTime() + nanoDuration;
        long timeLeft = nanoDuration;

        do {

            if (timeLeft > SLEEP_PRECISION) {
                Thread.sleep(1);
            }
            else if (timeLeft > SPIN_YIELD_PRECISION) {
                Thread.sleep(0);
            }

            timeLeft = end - System.nanoTime();

            if (Thread.interrupted()) throw new InterruptedException();

        }
        while (timeLeft > 0);

    }

    public static void untilTrue(String message, long amount, TimeUnit units, Callable<Boolean> callable) {
        long maxDuration = units.toMillis(amount);
        long start = System.currentTimeMillis();

        boolean value = false;
        while (!value && System.currentTimeMillis() - start < maxDuration) {
            try {
                value = callable.call();
            }
            catch (Exception e) {
                throw new RuntimeException(message + " : callable threw an exception", e);
            }

            ThreadUtils.sleep(50);
        }

        if (!value) {
            throw new RuntimeException(message + " : timed out wait for function to return true");
        }
    }

    public static void untilTrue(int amount, TimeUnit units, Callable<Boolean> callable) {
        untilTrue("", amount, units, callable);
    }

    public static void untilTrue(Callable<Boolean> callable) {
        untilTrue("", Timeout.getDefaultTimeout().getTime(), Timeout.getDefaultTimeout().getUnits(), callable);
    }

    public static void repeatUntilTrue(Callable<Boolean> callable) {
        untilTrue(callable);
    }

}
