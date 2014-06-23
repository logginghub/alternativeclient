package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class TestLogger {

    @Test public void testSetLevels() {
        assertThat(Logger.getLoggerFor("a").getLevel(), is(Logger.deferToRoot));
        assertThat(Logger.getLoggerFor("").getLevel(), is(Logger.info));
    }
}
