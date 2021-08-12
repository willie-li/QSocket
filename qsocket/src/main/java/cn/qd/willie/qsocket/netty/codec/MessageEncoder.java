package cn.qd.willie.qsocket.netty.codec;

import java.lang.ref.WeakReference;

import cn.qd.willie.qsocket.IQSocketClientListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Object> {
    private WeakReference<IQSocketClientListener> client;

    public MessageEncoder(IQSocketClientListener client) {
        this.client = new WeakReference<>(client);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (null != this.client && null != this.client.get()) {


        }
    }
}

