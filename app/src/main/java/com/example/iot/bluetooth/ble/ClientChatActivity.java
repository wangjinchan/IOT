package com.example.iot.bluetooth.ble;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.iot.R;

import java.util.List;
import java.util.UUID;

public class ClientChatActivity extends AppCompatActivity {
    private static final String TAG = "ClientChatActivity";
    private DeviceModel device;
    private BluetoothGatt bluetoothGatt;
    private TextView tvLog;
    BluetoothGattCharacteristic characteristic2 = null;

//    //服务uuid
//    public  UUID UUID_SERVER = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
//    //读的特征值¸
//    public  UUID UUID_CHAR_READ = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
//    //写的特征值
//    public  UUID UUID_CHAR_WRITE = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");



    //服务uuid
    public UUID UUID_SERVER;// = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");
    //读的特征值¸
    public  UUID UUID_CHAR_READ = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写的特征值
    public  UUID UUID_CHAR_WRITE = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_chat);
        device = getIntent().getParcelableExtra("device");
        setTitle(device.getName());
        initView();
        //发起连接
        bluetoothGatt = device.getDevice().connectGatt(this, false, bluetoothGattCallback);
    }
    private void initView() {
        tvLog = findViewById(R.id.tv_log);
        final EditText etContent = findViewById(R.id.et_content);
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = etContent.getText().toString();
                if (TextUtils.isEmpty(s)) return;
                sendData(s);
                etContent.setText("");
            }
        });

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(TAG, "onConnectionStateChange 连接成功");

                handler.sendMessage(handler.obtainMessage(0, "连接成功"));
                //查找服务
                gatt.discoverServices();


            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.e(TAG, "onConnectionStateChange 连接中......");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange 连接断开");
                handler.sendMessage(handler.obtainMessage(0, "连接断开"));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.e(TAG, "onConnectionStateChange 连接断开中......");
            }
        }



        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //设置读特征值的监听，接收服务端发送的数据
//           BluetoothGattService service = bluetoothGatt.getService(UUID_SERVER);
//           BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ);
//            boolean b = bluetoothGatt.setCharacteristicNotification(characteristic, true);
//            Log.e(TAG, "onServicesDiscovered 设置通知 " + b);


            List<BluetoothGattService> gattServiceList = gatt.getServices();
            Log.e("ard", "蓝牙模块服务开放状态：" + status + "，GATT：" + gatt.hashCode() + "，服务数量：" + gattServiceList.size());

            // 遍历Service
            for (int i = 0; i < gattServiceList.size(); i++) {
                BluetoothGattService gattService = gattServiceList.get(i);
                String serviceUUID = gattService.getUuid().toString();
                List<BluetoothGattCharacteristic> characteristicList = gattService.getCharacteristics();
                Log.i("ard", "服务UUID：" + serviceUUID + "，其下特征码数量： " + (null == characteristicList ? 0 : characteristicList.size()));
                UUID_SERVER=UUID.fromString(serviceUUID);

                // 遍历Characteristic
                for (int j = 0; j < characteristicList.size(); j++) {
                    BluetoothGattCharacteristic characteristic = characteristicList.get(j);
                    boolean b= bluetoothGatt.setCharacteristicNotification(characteristic, true); // 设置可接收回调消息
                    Log.e(TAG, "开启通知"+b );
                    String characteristicUUID = characteristic.getUuid().toString();
                    int properties = characteristic.getProperties();
                    Log.i("ard", "\t特征码UUID：" + characteristicUUID + "，属性：" + properties);

                    // 这个特征码是手机向蓝牙发数据的。根据蓝牙模块型号不同特征码uuid也不同，可以都尝试一下
                    // if (characteristicUUID.startsWith("0000ffe2")) {
                    characteristic2 = characteristic; // 设为全局的目标蓝牙模块写数据的控制器
                    //  }

                    // --- 以下if块没有实质操作 ---
                    if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) { // Characteristic可以接收回调消息
                        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                        if (null != descriptorList && descriptorList.size() > 0) {

                            // 遍历Descriptor
                            for (int k = 0; k < descriptorList.size(); k++) {
                                BluetoothGattDescriptor descriptor = descriptorList.get(k);
                                UUID descriptorUUID = descriptor.getUuid();
                                byte[] descriptorValue = descriptor.getValue();
                                Log.i("ard", "\t\t描述符UUID：" + descriptorUUID + "，" + String.valueOf(descriptorValue));
                            }
                        }
                    }
                }
            }


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());


            Log.e(TAG, "onCharacteristicChanged 接收到了数据 " + data);
            handler.sendMessage(handler.obtainMessage(1, data));

            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
            Log.e(TAG, "通知Characteristic: "+ uuid + "]:\n" + valueStr);

        }
    };

    /**
     * 发送数据
     *
     * @param msg
     */
    public void sendData(String msg) {
        if (bluetoothGatt == null) {
            return;
        }
        //找到服务
        BluetoothGattService service = bluetoothGatt.getService(UUID_SERVER);
        // Log.i(TAG, "服务UUID" + UUID_SERVER.toString());
        if (service == null) {
            return;
        }
        //拿到写的特征值
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE);
//        bluetoothGatt.setCharacteristicNotification(characteristic, true);
//        characteristic.setValue(msg.getBytes());
//        bluetoothGatt.writeCharacteristic(characteristic);
        bluetoothGatt.setCharacteristicNotification(characteristic2, true);
        characteristic2.setValue(msg.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic2);

        Log.e(TAG, "sendData 发送数据成功");
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    setTitle(device.getName() + "（" + msg.obj + "）");
                    break;
                case 1:
                    tvLog.append(msg.obj + "\n");
                    break;
                default:
                    break;
            }
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }
    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return true;
    }
}
