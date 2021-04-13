package com.example.iot.bluetooth.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者 ：WangJinchan
 * 邮箱 ：945750315@qq.com
 * 时间 ：2021/4/9
 * 说明 ：
 */
public class DeviceModel implements Parcelable {

    private BluetoothDevice device;
    private String name;
    private String mac;
    private String rssi;

    public DeviceModel(BluetoothDevice device, String rssi) {
        this.device = device;
        this.name = device.getName();
        this.mac = device.getAddress();
        this.rssi = rssi;
    }

    protected DeviceModel(Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        name = in.readString();
        mac = in.readString();
        rssi = in.readString();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceModel> CREATOR = new Creator<DeviceModel>() {
        @Override
        public DeviceModel createFromParcel(Parcel in) {
            return new DeviceModel(in);
        }

        @Override
        public DeviceModel[] newArray(int size) {
            return new DeviceModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeString(name);
        dest.writeString(mac);
        dest.writeString(rssi);
    }
}

