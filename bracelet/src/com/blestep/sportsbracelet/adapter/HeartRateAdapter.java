package com.blestep.sportsbracelet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.entity.HeartRate;

import java.util.ArrayList;

/**
 * @Date 2017/1/7
 * @Author wenzheng.liu
 * @Description 心率适配器
 * @ClassPath com.blestep.sportsbracelet.adapter.HeartRateAdapter
 */

public class HeartRateAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HeartRate> mList;

    public HeartRateAdapter(Context context, ArrayList<HeartRate> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        HeartRate heartRate = mList.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.heart_rate_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_heart_rate_time);
            holder.tvValue = (TextView) convertView.findViewById(R.id.tv_heart_rate_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvTime.setText(heartRate.time);
        holder.tvValue.setText(heartRate.value);
        return convertView;
    }

    class ViewHolder {
        private TextView tvTime;
        private TextView tvValue;
    }
}
