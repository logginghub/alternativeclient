package com.logginghub.connector.common.serialisation;

import java.nio.ByteBuffer;

public class NoopCompressionStrategy implements CompressionStrategy {

    public ByteBuffer compress(ByteBuffer sourceBuffer) {
        return sourceBuffer;
    }

    public ByteBuffer decompress(ByteBuffer sourceBuffer) {
        return sourceBuffer;
    }

}
