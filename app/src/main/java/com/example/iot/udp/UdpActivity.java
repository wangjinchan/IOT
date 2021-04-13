package com.example.iot.udp;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.iot.R;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class UdpActivity extends AppCompatActivity {
    private static final String TAG = "SocketAutoConnectServer";
    private static String IP;
    private InetAddress inetAddress = null;
    private BroadcastThread broadcastThread;
    private DatagramSocket sendSocket = null;
    private DatagramSocket receiveSocket = null;
    private Button sendUDPBrocast;
    private volatile boolean isRuning = false;
    private EditText localIP,localPort;
    private Button btn_send;
    private EditText et_sendInfo,ip,port;
    private String sendContent;
    private TextView tv_receive;
    private List<String> ipList = new ArrayList<>();
    private Button btnClear;
    private ReceiveThread receiveThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);
        setTitle("UDP调试助手");
        initView();
        initIp();
        initThread();
        try {
            inetAddress = InetAddress.getByName(ip.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initThread() {
        broadcastThread = new BroadcastThread();
        broadcastThread.start();
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    private void initIp() {
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            IP = getIpString(wifiInfo.getIpAddress());
            localIP.setText(IP);
            localIP.setEnabled(false);
        }
    }

    private void initView() {
        localIP = findViewById(R.id.localIP);
        localPort=findViewById(R.id.localPort);
        ip=findViewById(R.id.ip);
        port=findViewById(R.id.port);
        sendUDPBrocast = findViewById(R.id.sendUDPBrocast);
        tv_receive = findViewById(R.id.tv_receive);
        tv_receive.setMovementMethod(ScrollingMovementMethod.getInstance());
        et_sendInfo = findViewById(R.id.et_sendContent);
        btn_send = findViewById(R.id.btn_sendInfo);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRuning){
                    sendMessageToThread(broadcastThread.mhandler);

                    tv_receive.setTextColor(Color.BLUE);
                    tv_receive.append("已发送："+et_sendInfo.getText().toString().trim()+"\n");
                }else{
                    tv_receive.setTextColor(Color.RED);
                    tv_receive.setText("发送失败，请先启动UDP！");
                }


            }
        });
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                tv_receive.setText("");
            }
        });
        try {
            receiveSocket = new DatagramSocket(Integer.parseInt(localPort.getText().toString().trim()));
        } catch (SocketException e) {
            e.printStackTrace();
        }
        sendUDPBrocast.setOnClickListener(new SendUDPBrocastListener());
    }

    private void sendMessageToThread(Handler mhandler) {
        Message msg = Message.obtain();
        sendContent = et_sendInfo.getText().toString();
        msg.obj = sendContent;
        msg.what =1;
        mhandler.sendMessage(msg);
    }


    /**
     * 将获取到的int型ip转成string类型
     */
    private String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
                    if (!msg.obj.equals(IP)) {
                        if (!isExistIp(msg.obj.toString())) {
                            ipList.add(msg.obj.toString());
                        }
                        tv_receive.append(msg.obj.toString() + "\n");
                    }
                }
                break;
                default:
                    break;
            }
        }

    };

    public class SendUDPBrocastListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!localPort.getText().toString().trim().isEmpty()){
                if (isRuning) {
                    isRuning = false;
                    sendUDPBrocast.setText("启动");
                    tv_receive.setTextColor(Color.RED);
                    tv_receive.setText("已停止UDP\n");
                } else {
                    tv_receive.setTextColor(Color.BLUE);
                    tv_receive.setText("已启动UDP\n");
                    isRuning = true;
                    sendUDPBrocast.setText("停止");
                    localPort.setEnabled(false);

                }
            }else{
                tv_receive.setText("请输入本地端口号！！！（8080~65535）");
                tv_receive.setTextColor(Color.RED);
            }

        }
    }

    public class BroadcastThread extends Thread {
        private Handler mhandler = null;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            mhandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (isRuning){
                        String message = (String) msg.obj;
                        byte[] data = message.getBytes();
                        DatagramPacket dpSend = null;
                        dpSend = new DatagramPacket(data, data.length, inetAddress, Integer.parseInt(port.getText().toString().trim()));
                        try {
                            double start = System.currentTimeMillis();
                            for (int i = 0 ; i < 1; i ++) {
                                sendSocket = new DatagramSocket();
                                sendSocket.send(dpSend);
                                sendSocket.close();
                                Thread.sleep(80);
                                Log.i(TAG, "sendMessage: data " + new String(data));
                            }
                            double end = System.currentTimeMillis();
                            double times = end - start;
                            Log.i(TAG, "receive: executed time is : "+ times +"ms");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            };
            Looper.loop();
        }
    }

    private boolean isExistIp(String revIp) {
        if (ipList != null && ipList.size() > 0) {
            for (String ip : ipList) {
                if (ip != revIp) {
                    return false;
                }
            }
        }
        return false;
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (isRuning) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket dpReceive = null;
                    ipList.clear();
                    dpReceive = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        receiveSocket.receive(dpReceive);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String recIp = dpReceive.getAddress().toString().substring(1);
                    String port=String.valueOf(dpReceive.getPort());
                    String content=  new String(receiveData, 0, dpReceive.getLength());
                    if (dpReceive != null) {
                        Message revMessage = Message.obtain();
                        revMessage.what = 1;
                        revMessage.obj ="收到来自" +recIp+":"+port+"的信息："+content;
                        Log.i(TAG, "handleMessage: receive ip" + recIp);
                        myHandler.sendMessage(revMessage);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRuning = false;
        receiveSocket.close();
        System.out.println("UDP Server程序退出,关掉socket,停止广播");
        finish();
    }
}
