package com.logginghub.connector.common.serialisation;

import java.nio.ByteBuffer;

public interface CompressionStrategy {

    ByteBuffer compress(ByteBuffer currentBlock);
    ByteBuffer decompress(ByteBuffer currentBlock);

}
