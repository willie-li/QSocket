package cn.qd.willie.mysocket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import cn.qd.willie.qsocket.IQSocketClientListener;
import cn.qd.willie.qsocket.QSocketBuilder;
import cn.qd.willie.qsocket.QSocketClient;
import io.netty.channel.ChannelFutureListener;

public class MainActivity extends AppCompatActivity implements IQSocketClientListener {
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gson = new Gson();
        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.a, null);
                RequestModel requestModel = new RequestModel();
                String a = MD5Utils.imageToBase64("/sdcard/a.jpg");
                requestModel.setImage1(MD5Utils.imageToBase64("/sdcard/a.jpg"));
                requestModel.setImage2(MD5Utils.imageToBase64("/sdcard/a.jpg"));
                requestModel.setImage3(MD5Utils.imageToBase64("/sdcard/a.jpg"));
//                requestModel.setImage1(MD5Utils.bitmapToBase64(bitmap));
//                requestModel.setImage2(MD5Utils.bitmapToBase64(bitmap));
//                requestModel.setImage3(MD5Utils.bitmapToBase64(bitmap));
                QSocketClient.getInstance().sendMessage(gson.toJson(requestModel), new ChannelFutureListener() {
                    @Override
                    public void operationComplete(io.netty.channel.ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            Log.d("TAG", "write message successful");
                        }
                    }
                });
            }
        });

        QSocketBuilder builder=new QSocketBuilder();
        builder.setHOST("192.168.137.1")//设置IP
                .setPORT(8080)//设置端口
                .setHear_beat_time(15 * 1000)//设置心跳间隔
                .setContext(this)
                .setListener(this)
                .build();
        QSocketClient.getInstance().start();
    }

    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onConnectFail() {

    }

    @Override
    public void onMessageReceived(String data) {
        ResponseModel model = gson.fromJson(data,ResponseModel.class);
        model.setContentHtml(MD5Utils.decode(model.getContent()));
        Log.d("TAG","MainActivity:"+model.getContentHtml());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QSocketClient.getInstance().close();
    }
}