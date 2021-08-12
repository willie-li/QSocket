package cn.qd.willie.qsocket;

import android.content.Context;

public class QSocketBuilder {
    public String HOST;
    public int PORT;
    public IQSocketClientListener listener;
    public long hear_beat_time = 10 * 1000;
    public Context context;

    public QSocketBuilder() {

    }

    public QSocketBuilder setHOST(String HOST) {
        this.HOST = HOST;
        return this;
    }

    public QSocketBuilder setPORT(int PORT) {
        this.PORT = PORT;
        return this;
    }
    public QSocketBuilder setListener(IQSocketClientListener listener) {
        this.listener = listener;
        return this;
    }

    public QSocketBuilder setHear_beat_time(long hear_beat_time) {
        this.hear_beat_time = hear_beat_time;
        return this;
    }

    public QSocketBuilder setContext(Context context) {
        this.context = context;
        return this;
    }


    public void build() {
        QSocketClient.getInstance().setHOST(this.HOST);
        QSocketClient.getInstance().setPORT(this.PORT);
        QSocketClient.getInstance().setHear_beat_time(this.hear_beat_time);
        QSocketClient.getInstance().setListener(listener);
        QSocketClient.getInstance().setContext(this.context);
    }

}
