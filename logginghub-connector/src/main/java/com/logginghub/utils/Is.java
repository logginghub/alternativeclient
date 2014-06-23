package com.logginghub.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.swing.SwingUtilities;

public class Is {

    private static boolean exitOnViolation = false;

    private Is() {

    }

    public static void setExitOnViolation(boolean exitOnViolation) {
        Is.exitOnViolation = exitOnViolation;
    }

    public static boolean isExitOnViolation() {
        return exitOnViolation;
    }

    public static void equal(byte[] a, byte[] b, String message, Object... objects) {
        if (!Arrays.equals(a, b)) {
            throwI(message, objects);
        }
    }

    public static void equal(String actual, String expected, String message, Object... objects) {
        if (!actual.equals(expected)) {
            throwI(message, objects);
        }

    }

    public static void equal(Object actual, Object expected, String message, Object... objects) {
        if (!actual.equals(expected)) {
            throwI(message, objects);
        }

    }

    public static void falseStatement(boolean value, String message, Object... objects) {
        if (value) {
            throwI(message, objects);
        }
    }

    public static void greaterThan(double a, double b, String message, Object... objects) {
        if (a <= b) {
            throwI(message, objects);
        }
    }

    public static void lessThan(double a, double b, String message, Object... objects) {
        if (b < a) {
            throwI(message, objects);
        }
    }

    public static void greaterThanOrZero(int value, String message, Object... objects) {
        if (value < 0) {
            throwI(message, objects);
        }
    }

    public static void greaterThanZero(double value, String message, Object... objects) {
        if (value <= 0) {
            throwI(message, objects);
        }
    }

    public static void not(Object actual, Object expected, String message, Object... objects) {
        if (actual.equals(expected)) {
            throwI(message, objects);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection.isEmpty()) {
            throwI(message, collection);
        }
    }

    public static void notIn(Object object, Set<String> set, String message) {
        if (set.contains(object)) {
            throwI(message, object, set);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throwI(message, object);
        }
    }

    public static void notNull(Object object, String message, Object... objects) {
        if (object == null) {
            throwI(message, objects);
        }
    }

    public static void notNullOrEmpty(String string, String message) {
        if (string == null || string.length() == 0) {
            throwI(message, string);
        }
    }

    public static void notNullOrEmpty(String string, String message, Object... objects) {
        if (string == null || string.length() == 0) {
            throwI(message, objects);
        }
    }

    public static void nullOrEmpty(String string, String message) {
        if (string != null && string.length() != 0) {
            throwI(message, string);
        }
    }

    public static void swingEventThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalArgumentException("Call wasn't made from the swing event dispatch thread");
        }
    }

    public static void trueStatement(boolean value, String message, Object... objects) {
        if (!value) {
            throwI(message, objects);
        }
    }

    private static void throwI(String message, Object... objects) {
        String formattedMessage = StringUtils.format(message, objects);
        if (exitOnViolation) {
            System.err.println(formattedMessage);
            // NOSONAR
            System.exit(-1);
            // NOSONAR
        }
        else {
            throw new IllegalArgumentException(formattedMessage);
        }

    }

}
