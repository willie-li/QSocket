package cn.qd.willie.qsocket;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.qd.willie.qsocket.timer.TimerSchedule;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

public class QSocketClient {
    public static String TAG = "QSocketClient";
    private volatile static QSocketClient client;
    public Context context;
    /**
     * 长连接 通道
     */
    protected Channel channel;
    /**
     *长连接 NIO操作
     */
    protected ChannelFuture future;

    /**
     *netty 启动器
     */
    protected Bootstrap clientBootstrap;//netty长连接启动器

    /**
     * Socket ip
     */
    public String HOST;

    /**
     * Socket port
     */
    public int PORT=-1;
    /**
     * Socket 心跳 default 10s
     */
    public long hear_beat_time = 10 * 1000;

    public void setHOST(String HOST) {
        this.HOST = HOST;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }

    public void setHear_beat_time(long hear_beat_time) {
        this.hear_beat_time = hear_beat_time;
    }
    public void setContext(Context context) {
        this.context = context;
    }
    /**
     * 长连接消息回调 需要自定义
     */
    protected WeakReference<IQSocketClientListener> listener;

//    /**
//     * 服务端解码器 需要自定义
//     */
//    protected FrameDecoder nettyDecoder =new NettyClientDecoder();

    /**
     * 本地记录是否连接，不可靠，用于记录
     */
    protected boolean isConnect = false;

    /**
     * 单线程池 启动长连接
     */
    protected ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * 心跳定时器 default hear_beat_time 10s
     */
    protected TimerSchedule mRingTimerSchedule;

    private EventLoopGroup group;

    public static QSocketClient getInstance(){
        if(client == null){
            synchronized (QSocketClient.class){
                if(client == null){
                    client = new QSocketClient();
                }
            }
        }
        return client;
    }

    /**
     * 启动长连接
     * @throws Exception 参数异常检测
     */
    public void start() throws IllegalArgumentException {
        Log.i(TAG, "run-" + "***********start**********");

        if(HOST==null){
            throw new IllegalArgumentException("please set the HOST before start()!");
        }

        if(PORT==-1){
            throw new IllegalArgumentException("please set the PORT before start()!");
        }
        if(listener.get() ==null){
            throw new IllegalArgumentException("please set the IQSocketClientListener before start()!");
        }


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run-" + System.currentTimeMillis());
                startProcess();
            }
        };

        singleThreadExecutor.execute(runnable);
    }

    /**
     * 关闭长连接
     */
    public void close() {
        Log.i(TAG, "run-" + "************close**********");
        try {
            shutdown(future);
            stopHeartBeat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务器发送消息
     * 1.验证
     * 2.心跳
     * 3.业务消息
     * 都通过此方法发送
     * @param data
     * @param listener
     * @return
     */
    public boolean sendMessage(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag){
            ByteBuf byteBuf = Unpooled.copiedBuffer(data+"{*}", CharsetUtil.UTF_8);
            channel.writeAndFlush(byteBuf).addListener(listener);
        }
        return flag;
    }

    /**
     * 启动流程
     * 1.关闭上一个连接
     * 2.连接
     */
    private void startProcess() {
        try {
            //重启前关闭强停前一个连接
            if(future!=null) {
                //关闭
                shutdown(future);

                //避免同步操作future导致异常case
                Thread.sleep(500);
            }

            //channel 工厂
            group = new NioEventLoopGroup();//设置的连接group

            //client 启动器
            clientBootstrap = new Bootstrap().group(group)
                    .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() { // 5
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
//                                ch.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));//服务端最后以"\n"作为结束标识
//                                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));//解码
//                                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));//解码
//                            channel.pipeline().addLast(new MessageEncoder(listener.get()));
//                            channel.pipeline().addLast(new MessageDecoder(listener.get()));
                            ch.pipeline().addLast(new NettyClientHandler(getInstance()));//需要的handlerAdapter
                        }
                    });
            //执行connect
            future = clientBootstrap.connect(new InetSocketAddress(HOST, PORT)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        Log.e(TAG, "连接成功");
                        isConnect = true;
                        channel = channelFuture.channel();
                        listener.get().onConnectSuccess();
                        startHeartBeat();
                    } else {
                        Log.e(TAG, "连接失败");
                        reconnect();
                        listener.get().onConnectFail();
                        isConnect = false;
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reconnect(){
        group.schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    start();
                }catch (IllegalArgumentException e){
                    Log.e(TAG, "reconnect-重连失败");
                }
            }
        },5, TimeUnit.SECONDS);
    }

    /**
     * 关闭客户端
     *
     * @param future
     * @throws Exception
     */
    private void shutdown(ChannelFuture future) throws Exception {
        try {
            if (future.channel() != null && future.channel().isOpen()) {
                future.channel().close();
            }
            group.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        } finally {
            Log.i(TAG, "client is shutdown to server " + HOST + ":" + PORT);
        }
    }


    public boolean isConnect() {
        if (channel == null) {
            return false;
        }
        return channel.isOpen();
    }


    public void stopHeartBeat() {
        mRingTimerSchedule.stop();
    }

    public void startHeartBeat() {
        if(mRingTimerSchedule==null) {
            mRingTimerSchedule = new TimerSchedule(context, mTimerScheduleCallback);
        }
        mRingTimerSchedule.start(5 * 1000, hear_beat_time, hear_beat_time, hear_beat_time);
    }

    private TimerSchedule.TimerScheduleCallback mTimerScheduleCallback = new TimerSchedule.TimerScheduleCallback() {
        @Override
        public void doSchedule() {
            if(!isConnect()) return;
            sendMessage("Live",new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        Log.d(TAG,"heart>>>>>>>>>>>");
                    }else{
                    }
                }
            });
        }
    };

    public void setListener(IQSocketClientListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    public IQSocketClientListener getListener() {
        return listener.get();
    }
}
