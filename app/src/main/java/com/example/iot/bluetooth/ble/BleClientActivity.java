package com.example.iot.bluetooth.ble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import com.example.iot.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BleClientActivity extends AppCompatActivity  implements DeviceCallback, DeviceAdapter.OnItemClickListener {
    private BLEManager bleManager;
    //搜索到的设备
    private Map<String, DeviceModel> map = new HashMap<>();
    private DeviceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_client);
        setTitle("客户端—扫描设备");
        initView();
        initBle();
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    public void initBle() {
        bleManager = BLEManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        bleManager.enableBluetooth();
    }

    public void scan(View view) {
        bleManager.startScan(this);
    }

    public void stopScan(View view) {
        if (bleManager == null) {
            return;
        }
        bleManager.stopScan();
    }

    /**
     * 扫描到的设备
     *
     * @param device
     * @param rssi
     */
    @Override
    public void result(BluetoothDevice device, int rssi) {
        //要去重复
        DeviceModel model = new DeviceModel(device, String.valueOf(rssi));

        map.put(device.getAddress(), model);
        ArrayList<DeviceModel> deviceModels = new ArrayList<>(map.values());
        adapter.addData(deviceModels);
    }

    @Override
    public void click(DeviceModel device) {

        Intent intent = new Intent(this, ClientChatActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.onDestroy();
    }
    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return true;
    }
}
