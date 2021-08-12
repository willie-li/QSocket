package cn.qd.willie.qsocket.netty.codec;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.qd.willie.qsocket.IQSocketClientListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {
    private WeakReference<IQSocketClientListener> client;
    static final int PACKET_SIZE = 1024;

    // 用来临时保留没有处理过的请求报文
    ByteBuf tempMsg = Unpooled.buffer();
    public MessageDecoder(IQSocketClientListener client) {
        this.client = new WeakReference<>(client);
    }

//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        if (null != this.client && null != this.client.get()) {
//            ByteBuf buf = (ByteBuf)in;
//            byte[] req = new byte[buf.readableBytes()];
//            buf.readBytes(req);
//            String body = new String(req,"UTF-8");
//            Log.d("QQQQQQQQQQQQ",body);
//            SocketChannel channel1 = new SocketChannel() {
//            }
//            ByteBuffer buf1 = ByteBuffer.allocate(48);
//            int bytesRead = channel.read(buf);
//            while(bytesRead != -1) {
//                buf1.flip();
//                while(buf1.hasRemaining()) {
//                    System.out.print((char) buf1.get());
//                }
//                buf1.compact();  // 清除 buffer 的数据，这样才能继续被写入
//                bytesRead = channel.read(buf);
//                System.out.println("\n");
//            }
//        }
//    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println(Thread.currentThread() + "收到了一次数据包，长度是：" + in.readableBytes());

        // 合并报文
        ByteBuf message = null;
        int tmpMsgSize = tempMsg.readableBytes();
        // 如果暂存有上一次余下的请求报文，则合并
        if (tmpMsgSize > 0) {
            message = Unpooled.buffer();
            message.writeBytes(tempMsg);
            message.writeBytes(in);
            System.out.println("合并：上一数据包余下的长度为：" + tmpMsgSize + ",合并后长度为:" + message.readableBytes());
        } else {
            message = in;
        }

        int size = message.readableBytes();
        int counter = size / PACKET_SIZE;
        for (int i = 0; i < counter; i++) {
            byte[] request = new byte[PACKET_SIZE];
            // 每次从总的消息中读取220个字节的数据
            message.readBytes(request);

            // 将拆分后的结果放入out列表中，交由后面的业务逻辑去处理
            out.add(Unpooled.copiedBuffer(request));
            byte[] req = new byte[Unpooled.copiedBuffer(request).readableBytes()];
            Unpooled.copiedBuffer(request).readBytes(req);
            Log.e("taggggg",new String(req,"UTF-8"));
        }

        // 多余的报文存起来
        // 第一个报文： i+  暂存
        // 第二个报文： 1 与第一次
        size = message.readableBytes();
        if (size != 0) {
            System.out.println("多余的数据长度：" + size);
            // 剩下来的数据放到tempMsg暂存
            tempMsg.clear();
            tempMsg.writeBytes(message.readBytes(size));
        }
    }
}