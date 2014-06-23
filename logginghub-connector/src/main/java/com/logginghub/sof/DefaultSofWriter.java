package com.logginghub.sof;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

import com.logginghub.utils.Logger;

public class DefaultSofWriter implements SofWriter {

    private SofConfiguration configuration;

    private int fieldCount;
    private int lastIndex = Integer.MIN_VALUE;

    private WriterAbstraction writer;
    public static final byte NOT_NULL = 1;
    public static final byte NULL = 0;
    public static final int TYPE_INT = -1;
    public static final int TYPE_LONG = -2;
    public static final int TYPE_UTF8_ARRAY = -3;
    public static final int TYPE_BYTE_ARRAY = -4;
    public static final int TYPE_DOUBLE = -5;
    public static final int TYPE_UTF8 = -6;
    public static final int TYPE_BYTE = -7;
    public static final int TYPE_FLOAT = -9;
    public static final int TYPE_SHORT = -8;
    public static final int TYPE_BOOLEAN = -10;
    public static final int TYPE_CHAR = -11;
    public static final int TYPE_NULL_USER_TYPE = -12;
    public static final int TYPE_INT_OBJECT = -13;
    public static final int TYPE_LONG_OBJECT = -14;
    public static final int TYPE_DOUBLE_OBJECT = -15;
    public static final int TYPE_BYTE_OBJECT = -16;
    public static final int TYPE_SHORT_OBJECT = -17;
    public static final int TYPE_FLOAT_OBJECT = -18;
    public static final int TYPE_BOOLEAN_OBJECT = -19;
    public static final int TYPE_CHARACTER_OBJECT = -20;
    public static final int TYPE_DATE_OBJECT = -21;
    public static final int TYPE_BIGDECIMAL_OBJECT = -22;
    public static final int TYPE_UNIFORM_OBJECT_ARRAY = -23;
    public static final int TYPE_NON_UNIFORM_OBJECT_ARRAY = -24;

    private static final Logger logger = Logger.getLoggerFor(DefaultSofWriter.class); 
    
    public static String resolveField(int value) {
        // TODO : jshaw: this method looks dodgy

        String resolved = null;

        try {
            Field[] fields = DefaultSofWriter.class.getFields();
            for (Field field : fields) {
                Object object = field.get(null);
                if (object instanceof Number) {
                    Number number = (Number) object;
                    if (number.equals(value)) {
                        resolved = field.getName();
                        break;
                    }
                }
            }
        }
        catch (IllegalAccessException e) {
            logger.fine(e, "Failed to resolve field '{}'", value);
            resolved = Integer.toString(value);
        }

        if (resolved == null) {
            resolved = Integer.toString(value);
        }

        return resolved;
    }

    public DefaultSofWriter(WriterAbstraction writer, SofConfiguration configuration) {
        this.writer = writer;
        this.configuration = configuration;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void write(int field, BigDecimal b) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BIGDECIMAL_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeLong(b.unscaledValue().longValue());
                writer.writeInt(b.scale());
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, boolean b) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BOOLEAN);
            writer.writeBoolean(b);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Boolean b) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BOOLEAN_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeBoolean(b);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte b) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE);
            writer.writeByte(b);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Byte b) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeByte(b);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte[] array, int position, int length) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE_ARRAY);
            if (array == null) {
                writer.writeInt(-1);
            }
            else {
                writer.writeInt(length);
                writer.write(array, position, length);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte[] array) throws SofException {
        int arrayLength = 0;
        if (array != null) {
            arrayLength = array.length;
        }

        write(field, array, 0, arrayLength);
    }

    public void write(int field, char c) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_CHAR);
            SofSerialiser.writeChar(writer, c);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Character c) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_CHARACTER_OBJECT);
            if (c != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeChar(writer, c);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Date d) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DATE_OBJECT);
            if (d != null) {
                writer.writeByte(NOT_NULL);
                writer.writeLong(d.getTime());
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, double d) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DOUBLE);
            writer.writeDouble(d);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Double d) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DOUBLE_OBJECT);
            if (d != null) {
                writer.writeByte(NOT_NULL);
                writer.writeDouble(d);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, float f) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_FLOAT);
            writer.writeFloat(f);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Float f) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_FLOAT_OBJECT);
            if (f != null) {
                writer.writeByte(NOT_NULL);
                writer.writeFloat(f);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, int i) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_INT);
            SofSerialiser.writeInt(writer, i);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Integer i) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_INT_OBJECT);
            if (i != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeInt(writer, i);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, long l) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_LONG);
            SofSerialiser.writeLong(writer, l);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Long l) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_LONG_OBJECT);
            if (l != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeLong(writer, l);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, SerialisableObject serialisableObject) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObject == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            // jshaw - hopefully all of the base types (Integer, Long etc) have been covered, so we
            // should just be left with user types.
            Integer userType = configuration.resolve(serialisableObject.getClass());
            if (userType == null) {
                throw new SofException("Sub-object class '{}' has not been registered", serialisableObject.getClass().getName());
            }

            try {
                writeFieldHeaderAlways(field, userType.intValue());

                // Work out the encoded length
                CountingWriterAbstraction countingWriter = new CountingWriterAbstraction();
                DefaultSofWriter countingSofWriter = new DefaultSofWriter(countingWriter, configuration);

                // First pass
                serialisableObject.write(countingSofWriter);

                // TODO : at this stage shouldn't we be going back to the top most entry point and
                // writing out a full object header again? That way we could compress/encrypt object
                // internals?

                // Write the object length
                int length = countingWriter.getLength();
                SofSerialiser.writeInt(writer, length);

                // Second pass
                serialisableObject.write(this);

            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, SerialisableObject[] serialisableObjectArray) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObjectArray == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            try {
                writeFieldHeaderAlways(field, TYPE_NON_UNIFORM_OBJECT_ARRAY);
                TypeCodex.writeNonUniformObjectArray(writer, serialisableObjectArray, configuration);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, SerialisableObject[] serialisableObjectArray, Class<? extends SerialisableObject> clazz) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObjectArray == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            try {
                writeFieldHeaderAlways(field, TYPE_UNIFORM_OBJECT_ARRAY);
                TypeCodex.writeUniformObjectArray(writer, clazz, serialisableObjectArray, configuration);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, short s) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_SHORT);
            SofSerialiser.writeInt(writer, s);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Short s) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_SHORT_OBJECT);
            if (s != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeInt(writer, s);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, String string) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_UTF8);
            TypeCodex.writeString(writer, string);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, String[] array) throws SofException {
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_UTF8_ARRAY);
            TypeCodex.writeStringArray(writer, array);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    private void writeFieldHeader(int field, int type) throws IOException, SofException {
        if (!configuration.isMicroFormat()) {
            writeFieldHeaderAlways(field, type);
        }
    }

    private void writeFieldHeaderAlways(int field, int type) throws IOException, SofException {
        SofSerialiser.writeInt(writer, field);
        SofSerialiser.writeInt(writer, type);
        fieldCount++;
    }

    @Override public String toString() {
        return "Position : " + writer.getPosition();
    }

}
