package com.example.iot.bluetooth.classic;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.iot.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothServerActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView connectedState;
    private int CONNECTED=0;
    private int DISCONNECTED=1;
    private int READMSG=2;
    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothSocket socket;
    private EditText sendMsg;
    private Button sendBtn;
    private TextView communication;
    private ReadThread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_server);
        setTitle("经典蓝牙---服务端");

        //设置蓝牙设备可见性
        setDiscoverableTimeout(300);
        initView();

    }
    private void initView(){

        sendBtn= findViewById(R.id.SendBtnByServer);
        sendMsg= findViewById(R.id.SendMsgByServer);
        connectedState= findViewById(R.id.connectState);
        communication= findViewById(R.id.communicationMsginServer);
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter!=null){
            new Thread(new myThread()).start();
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String send=sendMsg.getText().toString().trim();
                if(!send.isEmpty()){
                    sendMessage(send);
                }
            }
        });
    }
    private class myThread extends Thread{
        @Override
        public void run(){
            try{
                bluetoothServerSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord("lcc",SPP_UUID);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Log.i("lcc check","等待连接...");

            while(true) {
                try{
                    socket=bluetoothServerSocket.accept();
                    BluetoothDevice device=socket.getRemoteDevice();
                    Log.i("lcc check","客户端名字为："+device.getName()+device.getAddress());
                    if(socket.isConnected()){
                        Log.i("lcc check","已经建立连接");
                        Message message=Message.obtain();
                        message.what=CONNECTED;
                        myhandler.sendMessage(message);
                        readThread=new ReadThread();
                        readThread.start();

                    }
                    else{
                        Message message=Message.obtain();
                        message.what=DISCONNECTED;
                        myhandler.sendMessage(message);
                    }
                }
                catch (Exception e){
                    Log.i("lcc check","未能成功建立连接");
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=DISCONNECTED;
                    myhandler.sendMessage(message);
                }
            }
        }
    }
    /**
     * 读取数据
     */
    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = socket.getInputStream();
                while (true) {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Log.i("lcc check","the msg is "+s);

                        Message msg=new Message();
                        msg.what=READMSG;
                        Bundle bundle=new Bundle();
                        bundle.putString("msg",s);
                        msg.setData(bundle);
                        myhandler.sendMessage(msg);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }
    @SuppressLint("HandlerLeak")
    private Handler myhandler=new Handler(){
        @Override
        public  void handleMessage(Message msg){
            if(msg.what==CONNECTED){
                connectedState.setText("成功连接客户端");
            }
            else if(msg.what==DISCONNECTED){
                connectedState.setText("连接断开，正字重新监听连接");
            }
            else if(msg.what==READMSG){
                String s=msg.getData().getString("msg");
                String info=String.format("\n客户端：%s",s);
                communication.append(info);
            }
        }
    };
    //设置该蓝牙设备一直可见
    public void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,timeout);
        } catch (Exception e) {
            e.printStackTrace(); }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try{
            socket.close();
            bluetoothServerSocket.close();
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }


    private void sendMessage(String msg) {
        if (socket == null) {
            Toast.makeText(BluetoothServerActivity.this,"没有连接",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Log.i("lcc check","the msg is "+msg);
            Toast.makeText(BluetoothServerActivity.this,"服务器："+msg,Toast.LENGTH_SHORT).show();
            //输出流输出信息
            OutputStream os = socket.getOutputStream();
            os.write(msg.getBytes());
            os.flush();
            String info=String.format("\n服务器：%s",msg);
            communication.append(info);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
