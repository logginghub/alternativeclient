package com.logginghub.sof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.OutputStream;

public class SofStreamSerialiser {

    public static byte[] write(SerialisableObject serialisableObject, SofConfiguration configuration) throws SofException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos, serialisableObject, configuration);
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked") public static <T> T read(byte[] data, SofConfiguration configuration) throws SofException, EOFException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bais, data.length);
        return (T)SofSerialiser.read(reader, configuration);
    }

    public static void write(OutputStream stream, SerialisableObject serialisableObject, SofConfiguration configuration) throws SofException {
        WriterAbstraction writer = createWriterAbstraction(stream);
        SofSerialiser.write(writer, serialisableObject, configuration);
    }

    public static WriterAbstraction createWriterAbstraction(OutputStream stream) {
        StreamWriterAbstraction streamWriterAbstraction = new StreamWriterAbstraction(stream);
        return streamWriterAbstraction;
    }

}
