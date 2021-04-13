package com.example.iot.bluetooth.classic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iot.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothClientActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    private LinearLayout linearLayout;
    private Button mSearch;
    //private Button bt2;
    private EditText sendMsg;
    private Button sendBtn;
    private TextView communicationMsg,t1,t2;
    private ReadThread readThread;
    private int READMSG=1;
    //uuid号，蓝牙设备之间传输数据必须知道的。只有知道uuid号，客户端和服务器端才能创建连接，传输数据
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //未配对设备列表
    private RecyclerView recyclerView_unpaired;
    private List<BluetoothDevice> bluetoothDevicesunpaired=new ArrayList<>();
    //已配对设备列表
    private RecyclerView recyclerView_paired;
    private List<BluetoothDevice> bluetoothDevicespaired=new ArrayList<>();
    private DeviceAdapter unpaired_adapter;
    private DeviceAdapter paired_adapter;
    //接收其他蓝牙设备发送的广播 begin
    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(action ==null){
                return;
            }
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    int state=device.getBondState();
                    if(state==BluetoothDevice.BOND_BONDED){
                        paired_adapter.addDev(device);
                    }
                    else{
                        unpaired_adapter.addDev(device);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int newState=device.getBondState();
                    if(newState==BluetoothDevice.BOND_BONDED){
                        Log.i("lcc check","the bond state is bonded");
                        unpaired_adapter.removeDev(device);
                        paired_adapter.addDev(device);
                        Toast.makeText(BluetoothClientActivity.this,"与设备"+device.getName()+"配对成功",Toast.LENGTH_LONG).show();
                    }
                    else if(newState==BluetoothDevice.BOND_BONDING){
                        Log.i("lcc check","the bond state is bonding");
                    }
                    else{
                        Log.i("lcc check","the bond state is nobond");
                        unpaired_adapter.addDev(device);
                        paired_adapter.removeDev(device);
                        Toast.makeText(BluetoothClientActivity.this,"与设备"+device.getName()+"配对失败",Toast.LENGTH_LONG).show();
                    }
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.i("lcc check","the connection state changed");
            }
        }
    };

    /**
     * 读取数据
     */
    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = bluetoothSocket.getInputStream();
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

            if(msg.what==READMSG){
                String s=msg.getData().getString("msg");
                String info=String.format("\n服务端：%s",s);
                communicationMsg.append(info);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_client);
        setTitle("经典蓝牙---客户端");
        //动态申请定位权限
        if(ContextCompat.checkSelfPermission(BluetoothClientActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(BluetoothClientActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},200);
        }
        else{
            Log.i("lcc check","the location permission has add");
        }

        mSearch= findViewById(R.id.btn1);
        recyclerView_unpaired=findViewById(R.id.device_list2);
        recyclerView_paired=findViewById(R.id.device_list);
        sendMsg= findViewById(R.id.SendMsg);
        sendBtn= findViewById(R.id.SendBtn);
        communicationMsg= findViewById(R.id.communicationMsg);
        linearLayout=findViewById(R.id.chat);
        t1=findViewById(R.id.t1);
        t2=findViewById(R.id.t2);
        //过滤蓝牙相关广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//蓝牙搜索结束
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//设备配对状态改变
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED); //BluetoothAdapter连接状态
        registerReceiver(broadcastReceiver, filter);

        //开启蓝牙
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //弹出对话框提示用户开启蓝牙
        if(bluetoothAdapter==null){
            Toast.makeText(BluetoothClientActivity.this,"此设备不支持蓝牙",Toast.LENGTH_LONG).show();
            finish();
        }
        if(bluetoothAdapter!=null && !bluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
        //设置蓝牙设备可见性
        setDiscoverableTimeout(300);

        unpaired_adapter=new DeviceAdapter(this,bluetoothDevicesunpaired);
        paired_adapter=new DeviceAdapter(this,bluetoothDevicespaired);

        //点击搜索按钮，开始搜索周围设备
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothDevicesunpaired.clear();
                bluetoothDevicespaired.clear();
                bluetoothAdapter.startDiscovery();
            }
        });

        //为未配对蓝牙列表设置点击事件，我们暂定为点击一次之后，进行蓝牙配对
        unpaired_adapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Log.i("lcc check","the position is "+position);
                bluetoothDevicesunpaired.get(position).createBond();
            }
        });
        //为已配对蓝牙列表创建点击事件，暂且设置为：点击一次，与服务端创建连接
        paired_adapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Log.i("lcc check","the position is "+position);
                try {
                    bluetoothSocket=bluetoothDevicespaired.get(position).createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    Log.i("lcc check","客户端和服务器尝试创建连接");
                    if(bluetoothSocket.isConnected()){
                        //do nothing
                        Log.i("lcc check","连接已经建立，无需再进行连接");
                    }
                    else{
                        bluetoothSocket.connect();
                    }
                    if(bluetoothSocket.isConnected()){
                        Log.i("lcc check","客户端和服务器已连接");
                        Toast.makeText(BluetoothClientActivity.this,"连接已经建立",Toast.LENGTH_LONG).show();
                        linearLayout.setVisibility(View.VISIBLE);
                        communicationMsg.setVisibility(View.VISIBLE);
                        readThread=new ReadThread();
                        readThread.start();
                        recyclerView_unpaired.setVisibility(View.GONE);
                        recyclerView_paired.setVisibility(View.GONE);
                        t1.setVisibility(View.GONE);
                        t2.setVisibility(View.GONE);
                        mSearch.setVisibility(View.GONE);
                    }
                }
                catch (Exception e){
                    Log.i("lcc check","the connection has not created");
                }
            }
        });

        //未配对设备列表
        recyclerView_unpaired.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView_unpaired.setAdapter(unpaired_adapter);
        recyclerView_unpaired.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        //已配对设备列表
        recyclerView_paired.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView_paired.setAdapter(paired_adapter);
        recyclerView_paired.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sendMsg.getText().toString().trim().isEmpty()){
                    String text=sendMsg.getText().toString();
                    sendMessage(text);
                }
            }
        });
    }

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


    //通过socket发送信息
    private void sendMessage(String msg) {
        if (bluetoothSocket == null) {
            //showToast("没有连接");
            Toast.makeText(BluetoothClientActivity.this,"没有连接",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Log.i("lcc check","the msg is "+msg);
            //showToast("发出的指令是" + msg);
            Toast.makeText(BluetoothClientActivity.this,"客户端："+msg,Toast.LENGTH_SHORT).show();
            //输出流输出信息
            OutputStream os = bluetoothSocket.getOutputStream();
            os.write(msg.getBytes());
            os.flush();
            String info=String.format("\n客户端：%s",msg);
            communicationMsg.append(info);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
