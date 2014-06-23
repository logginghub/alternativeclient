package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestThrottler {

    @Test public void testOkToFire() {
        Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        throttler.setTimeProvider(timeProvider);
        
        assertThat(throttler.isOkToFire(), is(true));
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(5);
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(9);
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(10);
        assertThat(throttler.isOkToFire(), is(true));
        assertThat(throttler.isOkToFire(), is(false));
        
    }

}
