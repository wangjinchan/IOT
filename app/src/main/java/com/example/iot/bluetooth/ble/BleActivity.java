package com.example.iot.bluetooth.ble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.iot.R;
import com.example.iot.tcp.TcpClientActivity;
import com.example.iot.tcp.TcpServerActivity;

public class BleActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        setTitle("低功耗蓝牙");
        //需要定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BleActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
        mContext=this;
        initView();
    }
    private void initView(){
        Button clientButton=findViewById(R.id.clientButton);
        clientButton.setOnClickListener(this);
        Button serverButton=findViewById(R.id.serverButton);
        serverButton.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clientButton:
                goActivity(BleClientActivity.class);
                break;
            case R.id.serverButton:
                goActivity(BleServerActivity.class);
                break;


        }
    }
    private void goActivity(Class activity){
        Intent intent = new Intent(mContext,activity);
        startActivity(intent);
    }
}
