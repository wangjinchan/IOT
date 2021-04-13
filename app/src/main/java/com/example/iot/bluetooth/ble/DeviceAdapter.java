package com.example.iot.bluetooth.ble;

import android.view.LayoutInflater;
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
 * 时间 ：2021/4/9
 * 说明 ：
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private List<DeviceModel> list = new ArrayList<>();
    private OnItemClickListener listener;

    public DeviceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvName.setText("设备名称：" + list.get(position).getName());
        holder.tvMac.setText("mac地址：" + list.get(position).getMac() + " 信号强度：" + list.get(position).getRssi());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.click(list.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvMac;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMac = itemView.findViewById(R.id.tv_mac);
        }
    }

    public void addData(List<DeviceModel> data) {
        list.clear();
        list.addAll(data);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {

        void click(DeviceModel device);
    }
}
