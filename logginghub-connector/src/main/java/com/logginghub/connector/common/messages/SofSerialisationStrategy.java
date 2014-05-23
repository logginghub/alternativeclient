package com.logginghub.connector.common.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.connector.common.DefaultLogEvent;
import com.logginghub.sof.ByteBufferReaderAbstraction;
import com.logginghub.sof.ByteBufferSofSerialiser;
import com.logginghub.sof.ByteBufferWriterAbstraction;
import com.logginghub.sof.DefaultSofReader;
import com.logginghub.sof.DefaultSofWriter;
import com.logginghub.sof.SerialisableObject;
import com.logginghub.sof.SofConfiguration;
import com.logginghub.sof.SofException;
import com.logginghub.sof.SofRuntimeException;

public class SofSerialisationStrategy implements SerialisationStrategy {

    private SofConfiguration configuration = new SofConfiguration();
    private boolean encodeHeader;
    private boolean microEncoding;

    // jshaw - optimisations so we dont have to create new abstractions each time
    private ByteBufferWriterAbstraction current = null;
    private ByteBuffer lastBuffer = null;
    private DefaultSofWriter currentWriter = null;

    private ByteBufferSofSerialiser serialiser2 = null;

    public SofSerialisationStrategy(boolean encodeHeader, boolean microEncoding) {
        this.encodeHeader = encodeHeader;
        this.microEncoding = microEncoding;
        configuration.registerType(DefaultLogEvent.class, 0);
        configuration.setMicroFormat(microEncoding);
    }

    @Override public String toString() {
        return "SofSerialisationStrategy [encodeHeader=" + encodeHeader + ", microEncoding=" + microEncoding + "]";
    }

    public void serialise(ByteBuffer buffer, SerialisableObject event) throws IOException {

        if (current == null || buffer != lastBuffer) {
            current = new ByteBufferWriterAbstraction(buffer);
            lastBuffer = buffer;
            currentWriter = new DefaultSofWriter(current, configuration);
            serialiser2 = new ByteBufferSofSerialiser(current, configuration);
        }

        try {
            if (encodeHeader) {
                serialiser2.write(0, event);
            }
            else {
                event.write(currentWriter);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    public SerialisableObject deserialise(ByteBuffer buffer) throws IOException {
        ByteBufferReaderAbstraction bufferAbstraction = new ByteBufferReaderAbstraction(buffer);
        DefaultLogEvent event;

        try {
            if (encodeHeader) {       
                event = ByteBufferSofSerialiser.read(bufferAbstraction, configuration);
            }
            else {
                event = new DefaultLogEvent();
                DefaultSofReader reader = new DefaultSofReader(bufferAbstraction, configuration);
                event.read(reader);
            }
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }

        return event;

    }

}
