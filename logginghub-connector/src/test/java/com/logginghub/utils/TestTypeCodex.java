package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.sof.ByteBufferReaderAbstraction;
import com.logginghub.sof.ByteBufferWriterAbstraction;
import com.logginghub.sof.ReaderAbstraction;
import com.logginghub.sof.TypeCodex;
import com.logginghub.sof.WriterAbstraction;

public class TestTypeCodex {

    @Test public void test_string() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
        TypeCodex.writeString(writer, "Hello world");
        buffer.flip();
        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
        assertThat(TypeCodex.readString(reader), is("Hello world"));
    }
    
    // jshaw - this doesn't work because the fast string encoder relies on utf 8
    @Test public void test_string_utf16() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
        TypeCodex.writeString(writer, "æœ‰å­�æ›°ï¼šã€Œå…¶ç‚ºäººä¹Ÿå­�å¼Ÿï¼Œè€Œå¥½çŠ¯ä¸Šè€…ï¼Œé®®çŸ£");
        buffer.flip();
        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
        assertThat(TypeCodex.readString(reader), is("æœ‰å­�æ›°ï¼šã€Œå…¶ç‚ºäººä¹Ÿå­�å¼Ÿï¼Œè€Œå¥½çŠ¯ä¸Šè€…ï¼Œé®®çŸ£"));
    }
    
//    @Test public void test_string2_utf16_1() throws Exception {
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
//        TypeCodex.writeString2(writer, "æœ‰å­�æ›°ï¼šã€Œå…¶ç‚ºäººä¹Ÿå­�å¼Ÿï¼Œè€Œå¥½çŠ¯ä¸Šè€…ï¼Œé®®çŸ£");
//        buffer.flip();
//        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
//        assertThat(TypeCodex.readString2(reader), is("æœ‰å­�æ›°ï¼šã€Œå…¶ç‚ºäººä¹Ÿå­�å¼Ÿï¼Œè€Œå¥½çŠ¯ä¸Šè€…ï¼Œé®®çŸ£"));
//    }
//    
//    @Test public void test_string2_utf16_2() throws Exception {
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
//        TypeCodex.writeString2(writer, "à¤ªà¤¶à¥�à¤ªà¤¤à¤¿à¤°à¤ªà¤¿ à¤¤à¤¾à¤¨à¥�à¤¯à¤¹à¤¾à¤¨à¤¿ à¤•à¥ƒà¤šà¥�à¤›à¥�à¤°à¤¾à¤¦à¥�");
//        buffer.flip();
//        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
//        assertThat(TypeCodex.readString2(reader), is("à¤ªà¤¶à¥�à¤ªà¤¤à¤¿à¤°à¤ªà¤¿ à¤¤à¤¾à¤¨à¥�à¤¯à¤¹à¤¾à¤¨à¤¿ à¤•à¥ƒà¤šà¥�à¤›à¥�à¤°à¤¾à¤¦à¥�"));
//    }

}
