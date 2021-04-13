package com.example.iot.bluetooth.classic;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iot.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者 ：WangJinchan
 * 邮箱 ：945750315@qq.com
 * 时间 ：2021/4/13
 * 说明 ：
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.MyViewHolder>  {
    private OnItemClickListener monItemClickListener;
    private List<BluetoothDevice> bluetoothDevices=new ArrayList<>();
    Context context;

    public DeviceAdapter(Context context,List<BluetoothDevice> bluetoothDevices){
        this.context=context;
        this.bluetoothDevices=bluetoothDevices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= View.inflate(context, R.layout.dev_item,null);
        return new MyViewHolder(v);
    }
    @Override
    public int getItemCount(){
        return bluetoothDevices.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        //如果能获取蓝牙设备名称，则显示蓝牙名称，否则显示Mac地址
        if(bluetoothDevices.get(position).getName()!=null && !bluetoothDevices.get(position).getName().equals(" ")){
            holder.name.setText(bluetoothDevices.get(position).getName());
            Log.i("lcc check","the name is "+bluetoothDevices.get(position).getName());
        }
        else{
            holder.name.setText(bluetoothDevices.get(position).getAddress());
            Log.i("lcc check","the address is "+bluetoothDevices.get(position).getAddress());
        }
        int BondState=bluetoothDevices.get(position).getBondState();
        if(BondState==BluetoothDevice.BOND_BONDED){
            holder.bondState.setText("    （已配对）");
        }
        else{
            holder.bondState.setText("    （未配对）");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monItemClickListener.onClick(position);
            }
        });
    }
    public void addDev(BluetoothDevice device){
        if (bluetoothDevices.contains(device))
            return;
        bluetoothDevices.add(device);
        notifyDataSetChanged();
    }

    public void removeDev(BluetoothDevice device){
        if(bluetoothDevices.contains(device)){
            bluetoothDevices.remove(device);
            notifyDataSetChanged();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView bondState;
        public MyViewHolder(View v){
            super(v);
            name=v.findViewById(R.id.name);
            bondState=v.findViewById(R.id.bondstate);
        }
    }

    public  void setOnItemClickListener(OnItemClickListener onItemClickListener){
        monItemClickListener=onItemClickListener;
    }


    public interface  OnItemClickListener{
        void onClick(int position);
    }
}

