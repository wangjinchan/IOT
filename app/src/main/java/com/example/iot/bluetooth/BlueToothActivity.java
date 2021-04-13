package com.example.iot.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.iot.R;
import com.example.iot.bluetooth.ble.BleActivity;
import com.example.iot.bluetooth.classic.ClassicBlueToothActivity;

public class BlueToothActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        setTitle("蓝牙调试助手");
        mContext=this;
        initView();
    }
    private void initView(){
        Button bleButton=findViewById(R.id.bleButton);
        bleButton.setOnClickListener(this);
        Button classicButton=findViewById(R.id.classicButton);
        classicButton.setOnClickListener(this);

    }
    private void goActivity(Class activity){
        Intent intent = new Intent(mContext,activity);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bleButton:
                goActivity(BleActivity.class);
                break;
            case R.id.classicButton:
                goActivity(ClassicBlueToothActivity.class);
                break;
        }
    }
}
