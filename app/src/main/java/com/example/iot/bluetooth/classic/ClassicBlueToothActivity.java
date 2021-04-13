package com.example.iot.bluetooth.classic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.iot.R;

public class ClassicBlueToothActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classic_blue_tooth);
        mContext=this;
        setTitle("经典蓝牙");
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
                goActivity(BluetoothClientActivity.class);
                break;
            case R.id.serverButton:
                goActivity(BluetoothServerActivity.class);
                break;
        }

    }

    private void goActivity(Class activity){
        Intent intent = new Intent(mContext,activity);
        startActivity(intent);
    }
}
