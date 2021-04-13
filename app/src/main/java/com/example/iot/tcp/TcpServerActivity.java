package com.example.iot.tcp;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
/**
 * 作者 ： WangJinchan
 * 邮箱 ： 945750315@qq.com
 * 日期 ： 2021/4/8.
 * 说明 ：
 */
public class TcpServerActivity extends AppCompatActivity implements View.OnClickListener {
    private Button CreateButton;
    private EditText editMsgText,editPort;
    private TextView recvText;
    private Context mContext;
    private boolean isConnecting=false;
    private ServerSocket serverSocket=null;//创建服务端ServerSocket对象
    private boolean serverRunning=false;
    private Thread mThreadServer=null;
    private Socket mSocketServer=null;
    static BufferedReader mBufferedReaderServer = null;
    static PrintWriter mPrintWriterServer = null;
    private String recvMessageServer = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_server);
        setTitle("tcp服务端");
        mContext = this;
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        initView();

    }
    private void initView(){
        Button sendButtonServer = findViewById(R.id.SendButtonServer);
        sendButtonServer.setOnClickListener(this);
        CreateButton= findViewById(R.id.createService);
        CreateButton.setOnClickListener(this);
        editMsgText= findViewById(R.id.MessageText);
        editMsgText.setText("hello,I'm Server!");
        recvText= findViewById(R.id.recvTv2);
        recvText.setMovementMethod(ScrollingMovementMethod.getInstance());
        editPort=findViewById(R.id.port);
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    recvText.setText(recvMessageServer);	// 刷新
                    break;
                case 1:
                    setTitle("tcp服务端-"+recvMessageServer);//创建成功，标题显示ip和端口号
                    recvText.setText("服务端已开启！");
                    break;
                case 2:
                    recvText.setText(recvMessageServer+"\n");
                    break;
                case 3:
                    recvText.setText("服务端"+recvMessageServer);
                    break;
                case 4:
                    recvText.setText("连接已断开");
                    break;
                case 5:
                    recvText.append("收到消息："+recvMessageServer);
                    break;
            }
        }
    };

    private String getInfoBuff(char[] buff, int count)
    {
        char[] temp = new char[count];
        System.arraycopy(buff, 0, temp, 0, count);
        return new String(temp);
    }

    //线程监听服务器发来的消息
    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                serverSocket = new ServerSocket(Integer.parseInt(editPort.getText().toString().trim()));
                SocketAddress address = null;
                if(!serverSocket.isBound())
                {
                    serverSocket.bind(address,0);
                }
                getLocalAddress();

            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true){
                try
                {
                    //方法用于等待客户端连接
                    mSocketServer=serverSocket.accept();
                    //接收客服端数据BufferedReader对象
                    mBufferedReaderServer=new BufferedReader(new InputStreamReader(mSocketServer.getInputStream()));
                    //给客服端发送数据
                    mPrintWriterServer=new PrintWriter(mSocketServer.getOutputStream(),true);

                    Message msg = new Message();
                    msg.what = 2;
                    String ip=mSocketServer.getInetAddress()+":"+mSocketServer.getPort();
                    recvMessageServer = "客户端"+ip+"已经连接成功!";
                    mHandler.sendMessage(msg);
                }catch (Exception e ) {
                    // TODO: handle exception
                    Message msg = new Message();
                    msg.what = 3;
                    recvMessageServer = "已关闭:" + e.getMessage()+":" + e.toString() + "\n";//消息换行
                    mHandler.sendMessage(msg);
                    return;
                }
                char[] buffer = new char[256];
                int count;
                while(serverRunning)
                {
                    try
                    {
                        if((count =mBufferedReaderServer.read(buffer))>0)
                        {
                            recvMessageServer = getInfoBuff(buffer, count) + "\n";//消息换行
                            if (getInfoBuff(buffer,count).equals("close")){
                                try {
                                    mSocketServer.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                recvMessageServer = "连接断开" + "\n";//消息换行
                                Message msg = new Message();
                                msg.what = 4;//收到断开连接请求
                                mHandler.sendMessage(msg);
                                break;
                            }
                            Message msg = new Message();
                            msg.what = 5;//收到消息
                            mHandler.sendMessage(msg);

                        }
                    }
                    catch (Exception e)
                    {
                        recvMessageServer = "接收异常:" + e.getMessage() + "\n";//消息换行
                        Message msg = new Message();
                        msg.what = 0;
                        mHandler.sendMessage(msg);

                    }
                }
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SendButtonServer:
                if(serverRunning&&mSocketServer!=null)
                {
                    String msgText=editMsgText.getText().toString();//取得编辑框中我们输入的内容
                    if(msgText.length()<=0)
                    {
                        Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        try
                        {
                            mPrintWriterServer.print(msgText);//发送给服务器
                            mPrintWriterServer.flush();
                            recvText.append("发送消息:"+msgText+"\n");
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

            case R.id.createService:
                if (!editPort.getText().toString().trim().isEmpty()){

                    if(serverRunning)
                    {
                        serverRunning=false;
                        try
                        {
                            if(serverSocket!=null)
                            {
                                serverSocket.close();
                                serverSocket=null;
                            }
                            if(mSocketServer!=null)
                            {
                                mSocketServer.close();
                                mSocketServer=null;
                            }
                        }catch (IOException e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                        mThreadServer.interrupt();
                        CreateButton.setText("创建服务");
                        editPort.setVisibility(View.VISIBLE);
                    }else
                    {
                        serverRunning=true;
                        mThreadServer=new Thread(runnable);
                        mThreadServer.start();
                        CreateButton.setText("停止服务");
                        editPort.setVisibility(View.GONE);
                    }
                }else{
                    recvText.setText("端口号不能为空！");
                }

                break;
        }
    }

    /**
     * 获取服务端局域网ip地址和端口号
     */
    public void getLocalAddress()
    {
        try
        {
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for(Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    if (inetAddress.getHostAddress().startsWith("192.168.1"))
                        recvMessageServer = inetAddress.getHostAddress()+":"
                                + serverSocket.getLocalPort();

                }
            }
        }catch (SocketException ex) {
            // TODO: handle exception
            recvMessageServer = "获取IP地址异常:" + ex.getMessage() + "\n";//消息换行
            Message msg = new Message();
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
        Message msg = new Message();
        msg.what = 1;//获取ip和端口号成功
        mHandler.sendMessage(msg);

    }


}
