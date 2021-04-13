package com.example.iot.tcp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iot.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * 作者 ： WangJinchan
 * 邮箱 ： 945750315@qq.com
 * 日期 ： 2021/4/8.
 * 说明 ：
 */
public class TcpClientActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startButton;
    private EditText IPText,editMsgTextClient;
    private Context mContext;
    private boolean isConnecting=false;
    private Thread mThreadClient=null;
    private Socket mSocketClient=null;
    static BufferedReader mBufferedReaderClient=null;
    static PrintWriter mPrintWriterClient=null;
    private String recvMessageClient="";
    private TextView recvText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_client);
        setTitle("tcp客户端");
        mContext=this;

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        );
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        initView();

    }
    private void initView(){
        IPText= findViewById(R.id.IPText);
        IPText.setText("192.168.1.145:8888");
        startButton= findViewById(R.id.StartConnect);
        startButton.setOnClickListener(this);
        editMsgTextClient= findViewById(R.id.clientMessageText);
        editMsgTextClient.setText("hello,I'm Client!");
        Button sendButtonClient = findViewById(R.id.SendButtonClient);
        sendButtonClient.setOnClickListener(this);
        recvText= findViewById(R.id.tv1);
        recvText.setMovementMethod(ScrollingMovementMethod.getInstance());
    }


    //线程：监听服务器发来的消息
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            String msgText = IPText.getText().toString();
            if(msgText.length()<=0)
            {
                recvMessageClient="IP不能为空！\n";//消息换行
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }
            int start = msgText.indexOf(":");
            if((start==-1)||(start+1>=msgText.length()))
            {
                recvMessageClient = "IP地址不合法\n";
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }
            String sIP= msgText.substring(0,start);
            String sPort = msgText.substring(start+1);
            int port = Integer.parseInt(sPort);
            Log.d("gjz", "IP"+sIP+":"+port);

            try
            {
                //连接服务器
                mSocketClient = new Socket(sIP,port);
                //取得输入、输出流
                mBufferedReaderClient=new BufferedReader(new InputStreamReader(mSocketClient.getInputStream()));
                mPrintWriterClient=new PrintWriter(mSocketClient.getOutputStream(),true);
                //recvMessageClient="已经连接server！\n";
                Message msg = new Message();
                msg.what = 4;//连接服务端成功
                mHandler.sendMessage(msg);
            }catch (Exception e) {
                // TODO: handle exception
                recvMessageClient = "连接IP异常:" + e.toString() + e.getMessage() + "\n";//消息换行
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            }

            char[] buffer = new char[256];
            int count = 0;
            while(isConnecting)
            {
                try
                {
                    if((count = mBufferedReaderClient.read(buffer))>0)
                    {
                        recvMessageClient = getInfoBuff(buffer,count)+"\n";
                        Message msg = new Message();
                        msg.what = 3;//收到消息
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e) {
                    // TODO: handle exception
                    recvMessageClient = "接收异常:" + e.getMessage() + "\n";//消息换行
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    recvText.append(recvMessageClient);//刷新
                    break;
                case 2:
                    recvText.setText("连接已断开！\n");
                    break;
                case 3:
                    recvText.append("收到消息:"+recvMessageClient);
                    break;
                case 4:
                    recvText.setText("连接服务端成功！\n");
                    break;
            }
        }
    };

    private String getInfoBuff(char[] buff,int count)
    {
        char[] temp = new char[count];
        for (int i = 0; i < count; i++) {
            temp[i]=buff[i];
        }
        return new String(temp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.StartConnect:
                if (isConnecting) {
                    isConnecting = false;
                    try {
                        mPrintWriterClient.print("close");
                        mPrintWriterClient.flush();
                        Message msg = new Message();
                        msg.what = 2;//连接已断开
                        mHandler.sendMessage(msg);
                        if (mSocketClient != null) {
                            mSocketClient.close();
                            mSocketClient = null;
                            mPrintWriterClient.close();
                            mPrintWriterClient = null;
                        }
                    } catch (IOException e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                    mThreadClient.interrupt();
                    startButton.setText("开始连接");
                    IPText.setEnabled(true);
                    recvText.setText("信息:\n");

                }else
                {
                    isConnecting=true;
                    startButton.setText("停止连接");
                    IPText.setEnabled(false);
                    mThreadClient = new Thread(mRunnable);
                    mThreadClient.start();
                }
                break;
            case R.id.SendButtonClient:
                if(isConnecting&&mSocketClient!=null)
                {
                    String msgText = editMsgTextClient.getText().toString();
                    if(msgText.length()<=0)
                    {
                        Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        try
                        {
                            if (msgText.equals("close")) {
                                isConnecting = false;
                                startButton.setText("开始连接");
                                IPText.setEnabled(true);
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                                mThreadClient.isInterrupted();
                                mPrintWriterClient.print(msgText);
                                mPrintWriterClient.flush();

                            }else{
                                mPrintWriterClient.print(msgText);
                                mPrintWriterClient.flush();
                            }

                        }catch (Exception e) {
                            // TODO: handle exception
                            Toast.makeText(mContext, "发送异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else
                {
                    Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
