package cn.qd.willie.qsocket;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "NettyClientHandler";
    private WeakReference<IQSocketClientListener> listener;
    private QSocketClient socketClient;static final int PACKET_SIZE = 1024;
    StringBuffer buffer;

    // 用来临时保留没有处理过的请求报文
    ByteBuf tempMsg = Unpooled.buffer();


    public NettyClientHandler(QSocketClient socketClient) {
        this.socketClient = socketClient;
        this.listener = new WeakReference<>(socketClient.getListener());
    }

    //每次给服务器发送的东西， 让服务器知道我们在连接中哎
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Log.e(TAG, "userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush("Heartbeat" + System.getProperty("line.separator"));
            }
        }
    }

    /**
     * 连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        super.channelActive(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "channelInactive");
        socketClient.reconnect();
    }

    //接收消息的地方， 接口调用返回到activity了
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg; byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        if(buffer == null){
            buffer = new StringBuffer();
        }
        if(body.contains("{*}")){
            buffer.append(body);
            String[] array = buffer.toString().split("\\{\\*\\}");
            if(!body.endsWith("{*}")){
                String lastStr = array[array.length - 1];
                array = Arrays.copyOfRange(array,0,array.length-1);
                buffer.delete(0,buffer.length());
                buffer.append(lastStr);
            }else{
                buffer = null;
            }
            for(String str:array){
                listener.get().onMessageReceived(str);
            }
        }else{
            buffer.append(body);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当引发异常时关闭连接。
        Log.e(TAG, "exceptionCaught");
//        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }

}
