package com.example.iot.tcp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.iot.R;

/**
 * 作者 ： WangJinchan
 * 邮箱 ： 945750315@qq.com
 * 日期 ： 2021/4/8.
 * 说明 ：
 */
public class TcpActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);
        setTitle("tcp调试助手");
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
                goActivity(TcpClientActivity.class);
                break;
            case R.id.serverButton:
                goActivity(TcpServerActivity.class);
                break;
        }
    }
    private void goActivity(Class activity){
        Intent intent = new Intent(mContext,activity);
        startActivity(intent);
    }
}
