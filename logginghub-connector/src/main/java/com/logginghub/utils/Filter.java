package com.logginghub.utils;

public interface Filter<T> {
    boolean passes(T t);
}
