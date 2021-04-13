package com.example.iot.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * 作者 ：WangJinchan
 * 邮箱 ：945750315@qq.com
 * 时间 ：2021/4/8
 * 说明 ：
 */
public class BLEManager {

    private static final String TAG = "BLEManager";
    private static BLEManager bleManager;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceCallback callback;
    private BluetoothManager bluetoothManager;

    private BLEManager(Context context) {
        init(context);
    }

    public static BLEManager getInstance(Context context) {
        if (bleManager == null) {
            synchronized (BLEManager.class) {
                if (bleManager == null) {
                    bleManager = new BLEManager(context);
                }
            }
        }
        return bleManager;
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * 打开蓝牙
     */
    public boolean enableBluetooth() {
        if (bluetoothAdapter == null) return false;
        if (!bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.enable();
        } else {
            return true;
        }
    }

    /**
     * 设备是否支持BLE
     *
     * @return
     */
    public boolean isSupportBle() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    /**
     * 扫描设备
     */
    public void startScan(DeviceCallback callback) {
        if (bluetoothAdapter == null) return;
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) {
                scanner.startScan(scanCallback);
            }
        } else {
            bluetoothAdapter.startLeScan(leScanCallback);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (bluetoothAdapter == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(scanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            if (callback != null) {
                callback.result(device, result.getRssi());
            }
        }
    };
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (callback != null) {

                callback.result(device, rssi);
            }
        }
    };
//==============================================以下服务端相关================================================================

    /**
     * 创建Ble服务端，接收连接
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising(String name, AdvertiseCallback callback) {
        //BLE广告设置
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        bluetoothAdapter.setName(name);
        //开启服务
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    /**
     * 停止服务
     *
     * @param callback
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertising(AdvertiseCallback callback) {
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.stopAdvertising(callback);
    }

    public void onDestroy() {
        if (bluetoothAdapter == null) return;
        stopScan();
    }
}
