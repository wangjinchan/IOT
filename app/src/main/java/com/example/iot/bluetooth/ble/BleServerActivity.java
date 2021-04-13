package com.example.iot.bluetooth.ble;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iot.R;

import java.util.UUID;

public class BleServerActivity extends AppCompatActivity {
    private static final String TAG = "ServerActivity";
    public static final String BLE_NAME = "BleServer";
    private BLEManager bleManager;
    public UUID UUID_SERVER = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");
    //读的特征值¸
    public  UUID UUID_CHAR_READ = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写的特征值
    public  UUID UUID_CHAR_WRITE = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    private String uuid="服务UUID：\n"+UUID_SERVER+"\n"+"读的UUID:\n"+UUID_CHAR_READ+"\n"+"写的UUID: \n"+UUID_CHAR_WRITE+"\n";
    /**
     * 当前连接的设备
     */
    private BluetoothDevice device;
    /**
     * 客户端读取数据的特征值
     */
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattServer bluetoothGattServer;
    private TextView tvLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_server);
        setTitle("ble服务端—等待连接");
        initView();
        initBleServer();
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
        Button checkUuidButton=findViewById(R.id.checkUuid);
        checkUuidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLog.setText(uuid);
            }
        });

    }

    /**
     * 初始化Ble服务端
     */
    private void initBleServer() {
        bleManager = BLEManager.getInstance(this);
        if (!bleManager.isSupportBle()) {
            Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        bleManager.enableBluetooth();
        //开启ble服务
        bleManager.startAdvertising(BLE_NAME, advertiseCallback);
    }

    /**
     * Ble服务监听
     */
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.e(TAG, "服务开启成功 " + settingsInEffect.toString());
            addService();
        }
    };

    /**
     * 添加读写服务UUID，特征值等
     */
    private void addService() {
        BluetoothGattService gattService = new BluetoothGattService(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //只读的特征值
        characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //只写的特征值
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ
                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        //将特征值添加至服务里
        gattService.addCharacteristic(characteristicRead);
        gattService.addCharacteristic(characteristicWrite);
        //监听客户端的连接
        bluetoothGattServer = bleManager.getBluetoothManager().openGattServer(this, gattServerCallback);
        bluetoothGattServer.addService(gattService);
    }

    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            BleServerActivity.this.device = device;
            String state = "";
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                state = "连接成功";

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                state = "连接断开";
            }
            Log.e(TAG, "onConnectionStateChange device=" + device.toString() + " status=" + status + " newState=" + state);
            handler.sendMessage(handler.obtainMessage(0, device.getName() + "（" + state + "）"));
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            String data = new String(value);
            Log.e(TAG, "收到了客户端发过来的数据 " + data);
            //告诉客户端发送成功
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            handler.sendMessage(handler.obtainMessage(1, data));

        }
    };

    /**
     * 写入数据给客户端
     *
     * @param msg
     */
    public void sendData(String msg) {
        characteristicRead.setValue(msg.getBytes());
        bluetoothGattServer.notifyCharacteristicChanged(device, characteristicRead, false);
        Log.e(TAG, "sendData 数据发送成功");
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    setTitle("服务端—" + msg.obj);
                    break;
                case 1:
                    tvLog.append(msg.obj + "\n");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.stopAdvertising(advertiseCallback);
        bleManager.onDestroy();
    }
    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return true;
    }
}
