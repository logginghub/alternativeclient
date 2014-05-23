package com.logginghub.sof;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferWriterAbstraction implements WriterAbstraction {

    private ByteBuffer buffer;

    public ByteBufferWriterAbstraction(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void writeShort(short value) {
        buffer.putShort(value);
    }

    public void writeLong(long l) {
        buffer.putLong(l);
    }

    public void writeFloat(float f) {
        buffer.putFloat(f);
    }

    public void writeDouble(double d) {
        buffer.putDouble(d);
    }

    public void writeChar(char c) {
        buffer.putChar(c);
    }

    public void writeByte(byte type) {
        buffer.put(type);
    }

    public void writeBoolean(boolean b) {
        buffer.put((byte) (b ? 1 : 0));
    }

    public void write(byte[] bytes) {
        buffer.put(bytes);
    }

    public void write(byte[] value, int position, int length) throws IOException {
        buffer.put(value, position, length);
    }

    public void writeInt(int value) {
        buffer.putInt(value);
    }

    public void writeUnsignedByte(int value) throws IOException {
        buffer.put((byte) (0x000000ff & value));
    }

    public void writeUnsignedShort(int value) throws IOException {
        // int i = value >>> 8;
        // int j = value >>> 8;
        //
        //
        // int x = i & 0xFF;
        // int y = j & 0xFF;
        //
        // buffer.put((byte) x);
        // buffer.put((byte) y);

        buffer.putShort((short) value);
    }

    public int getPosition() {
        return buffer.position();
    }

    public void setInt(int index, int value) {
        buffer.putInt(index, value);
    }

    public void writeBuffer(ByteBuffer tempBuffer) {
        buffer.put(tempBuffer);
    }

}
