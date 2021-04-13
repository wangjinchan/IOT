package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.iot.bluetooth.BlueToothActivity;
import com.example.iot.tcp.TcpActivity;
import com.example.iot.udp.UdpActivity;

/**
 * 作者 ： WangJinchan
 * 邮箱 ： 945750315@qq.com
 * 日期 ： 2021/4/8.
 * 说明 ：
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("网络调试助手");
        mContext=this;
        initView();
    }
    private void initView(){
        Button tcpButton = findViewById(R.id.tcpButton);
        tcpButton.setOnClickListener(this);

        Button blueToothButton=findViewById(R.id.blueToothButton);
        blueToothButton.setOnClickListener(this);

        Button udpButton=findViewById(R.id.udpButton);
        udpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tcpButton:
                goActivity(TcpActivity.class);
                break;
            case R.id.blueToothButton:
                goActivity(BlueToothActivity.class);
                break;
            case R.id.udpButton:
                goActivity(UdpActivity.class);
                break;
        }
    }
    private void goActivity(Class activity){
        Intent intent = new Intent(mContext,activity);
        startActivity(intent);
    }
}
