package cn.qd.willie.qsocket;

public interface IQSocketClientListener {
    /**
     * 连接成功
     */
    void onConnectSuccess();

    /**
     * 连接失败
     */
    void onConnectFail();

   void onMessageReceived(String data);
}
