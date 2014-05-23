package com.logginghub.connector.common.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.sof.SerialisableObject;

public interface SerialisationStrategy {
    void serialise(ByteBuffer buffer, SerialisableObject t) throws IOException;
    SerialisableObject deserialise(ByteBuffer buffer) throws IOException;

}
