package org.eclipse.jetty.websocket.parser;

import static org.hamcrest.Matchers.*;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.protocol.OpCode;
import org.eclipse.jetty.websocket.protocol.WebSocketFrame;
import org.junit.Assert;
import org.junit.Test;

/**
 * Collection of Example packets as found in <a href="https://tools.ietf.org/html/rfc6455#section-5.7">RFC 6455 Examples section</a>
 */
public class RFC6455ExamplesParserTest
{
    @Test
    public void testFragmentedUnmaskedTextMessage()
    {
        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);

        ByteBuffer buf = ByteBuffer.allocate(16);

        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // A fragmented unmasked text message (part 1 of 2 "Hel")
        buf.put(new byte[]
                { (byte)0x01, (byte)0x03, 0x48, (byte)0x65, 0x6c });
        buf.flip();

        // Parse #1
        parser.parse(buf);

        // part 2 of 2 "lo" (A continuation frame of the prior text message)
        buf.flip();
        buf.put(new byte[]
                { (byte)0x80, 0x02, 0x6c, 0x6f });
        buf.flip();

        // Parse #2
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.TEXT,2);

        WebSocketFrame txt = capture.getFrames().get(0);
        Assert.assertThat("TextFrame[0].data",txt.getPayloadAsUTF8(),is("Hel"));
        txt = capture.getFrames().get(1);
        Assert.assertThat("TextFrame[1].data",txt.getPayloadAsUTF8(),is("lo"));
    }

    @Test
    public void testSingleMaskedPongRequest()
    {
        ByteBuffer buf = ByteBuffer.allocate(16);
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // Unmasked Pong request
        buf.put(new byte[]
                { (byte)0x8a, (byte)0x85, 0x37, (byte)0xfa, 0x21, 0x3d, 0x7f, (byte)0x9f, 0x4d, 0x51, 0x58 });
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.PONG,1);

        WebSocketFrame pong = capture.getFrames().get(0);
        Assert.assertThat("PongFrame.payload",pong.getPayloadAsUTF8(),is("Hello"));
    }

    @Test
    public void testSingleMaskedTextMessage()
    {
        ByteBuffer buf = ByteBuffer.allocate(16);
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // A single-frame masked text message
        buf.put(new byte[]
                { (byte)0x81, (byte)0x85, 0x37, (byte)0xfa, 0x21, 0x3d, 0x7f, (byte)0x9f, 0x4d, 0x51, 0x58 });
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.TEXT,1);

        WebSocketFrame txt = capture.getFrames().get(0);
        Assert.assertThat("TextFrame.payload",txt.getPayloadAsUTF8(),is("Hello"));
    }

    @Test
    public void testSingleUnmasked256ByteBinaryMessage()
    {
        int dataSize = 256;

        ByteBuffer buf = ByteBuffer.allocate(dataSize + 10);
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // 256 bytes binary message in a single unmasked frame
        buf.put(new byte[]
                { (byte)0x82, 0x7E });
        buf.putShort((short)0x01_00); // 16 bit size
        for (int i = 0; i < dataSize; i++)
        {
            buf.put((byte)0x44);
        }
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.BINARY,1);

        WebSocketFrame bin = capture.getFrames().get(0);

        Assert.assertThat("BinaryFrame.payloadLength",bin.getPayloadLength(),is(dataSize));
        Assert.assertThat("BinaryFrame.payload.length",bin.getPayloadData().length,is(dataSize));

        byte data[] = bin.getPayloadData();
        for (int i = 0; i < dataSize; i++)
        {
            Assert.assertThat("BinaryFrame.payload[" + i + "]",data[i],is((byte)0x44));
        }
    }

    @Test
    public void testSingleUnmasked64KByteBinaryMessage()
    {
        int dataSize = 1024 * 64;

        ByteBuffer buf = ByteBuffer.allocate((dataSize + 10));
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // 64 Kbytes binary message in a single unmasked frame
        buf.put(new byte[]
                { (byte)0x82, 0x7F });
        buf.putLong(dataSize); // 64bit size
        for (int i = 0; i < dataSize; i++)
        {
            buf.put((byte)0x77);
        }
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setBufferSize(80000);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.BINARY,1);

        WebSocketFrame bin = capture.getFrames().get(0);

        Assert.assertThat("BinaryFrame.payloadLength",bin.getPayloadLength(),is(dataSize));
        Assert.assertThat("BinaryFrame.payload.length",bin.getPayloadData().length,is(dataSize));

        byte data[] = bin.getPayloadData();
        for (int i = 0; i < dataSize; i++)
        {
            Assert.assertThat("BinaryFrame.payload[" + i + "]",data[i],is((byte)0x77));
        }
    }

    @Test
    public void testSingleUnmaskedPingRequest()
    {
        ByteBuffer buf = ByteBuffer.allocate(16);
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // Unmasked Ping request
        buf.put(new byte[]
                { (byte)0x89, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f });
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.PING,1);

        WebSocketFrame ping = capture.getFrames().get(0);
        Assert.assertThat("PingFrame.payload",ping.getPayloadAsUTF8(),is("Hello"));
    }

    @Test
    public void testSingleUnmaskedTextMessage()
    {
        ByteBuffer buf = ByteBuffer.allocate(16);
        // Raw bytes as found in RFC 6455, Section 5.7 - Examples
        // A single-frame unmasked text message
        buf.put(new byte[]
                { (byte)0x81, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f });
        buf.flip();

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        Parser parser = new Parser(policy);
        FrameParseCapture capture = new FrameParseCapture();
        parser.addListener(capture);
        parser.parse(buf);

        capture.assertNoErrors();
        capture.assertHasFrame(OpCode.TEXT,1);

        WebSocketFrame txt = capture.getFrames().get(0);
        Assert.assertThat("TextFrame.payload",txt.getPayloadAsUTF8(),is("Hello"));
    }
}