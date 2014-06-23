package com.logginghub.connector.common.serialisation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.connector.common.serialisation.SnappyCompressionStrategy;

public class TestSnappyCompressionStrategy {
    @Test public void test() throws Exception {
        SnappyCompressionStrategy compressionStrategy = new SnappyCompressionStrategy();
        
        ByteBuffer input = ByteBuffer.wrap("Hello world".getBytes());
        input.compact();
        
        ByteBuffer compressed = compressionStrategy.compress(input);
        
        ByteBuffer output = compressionStrategy.decompress(compressed);
        byte[] decompressedArray = output.array();
        assertThat(new String(decompressedArray), is("Hello world"));
    }
}
