package com.communication.yang.cpe_2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.communication.yang.cpe_2.R;

import java.util.ArrayList;
import java.util.List;

import db.analyticMethod.InitCpeAnalysis;
import db.log.HeartbeatLog;

/**
 * @Author : YangFan
 * @Date : 2020年11月18日 17:56
 * @effect :心跳布局适配器
 */
public class HeartLogAdapter extends BaseAdapter {

    private Context context;
    private Integer resourceId;
    private List<HeartbeatLog> list = new ArrayList<HeartbeatLog>();

    public HeartLogAdapter(Context context, Integer resourceId, List<HeartbeatLog> list) {
        this.context = context;
        this.resourceId = resourceId;
        this.list = list;
    }

    //创建内部类，心跳布局
    class ViewHold{
        //心跳LOG日志
        TextView type;
        TextView cpeType;
        TextView uid;
        TextView simId;
        TextView totalSize;
        TextView availableSize;
        TextView availableMemory;
        TextView phoneNum;
        TextView status;
        TextView timestamp;
    }

    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public Object getItem(int position) {
        return list.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled(){
        return false;
    }

    @Override
    public boolean isEnabled(int position){
        return false;
    }

    /**
     * @param position list下标
     * @param convertView view缓存
     * @param parent 返回view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //创建一个view用来存放缓存
        View view;

        //创建一个内部类
        ViewHold viewHolder;

        //消息类型
        HeartbeatLog heartbeatLog = list.get(position);

        /**
         * 1.初始化配置
         */
        //首次进入
        if(convertView == null){
            view = LayoutInflater.from(context).inflate
                    (resourceId,parent,false);

            viewHolder = new ViewHold();

            viewHolder.type = view.findViewById(R.id.type);
            viewHolder.cpeType = view.findViewById(R.id.cpeType);
            viewHolder.uid = view.findViewById(R.id.uid);
            viewHolder.simId = view.findViewById(R.id.simId);
            viewHolder.totalSize = view.findViewById(R.id.totalSize);
            viewHolder.availableSize = view.findViewById(R.id.availableSize);
            viewHolder.availableMemory = view.findViewById(R.id.availableMemory);
            viewHolder.phoneNum = view.findViewById(R.id.phoneNum);
            viewHolder.status = view.findViewById(R.id.status);
            viewHolder.timestamp = view.findViewById(R.id.timestamp);

            view.setTag(viewHolder);
        }

        //非首次进入
        else {
            //缓存放入view
            view = convertView;
            //重新获取ID
            viewHolder = (ViewHold)view.getTag();
        }

        /**
         * 2.传值
         */
        if(heartbeatLog!=null){
            viewHolder.type.setText(String.valueOf(heartbeatLog.getType()));
            viewHolder.cpeType.setText(String.valueOf(heartbeatLog.getCpeType()));
            viewHolder.uid.setText(String.valueOf(heartbeatLog.getUid()));
            viewHolder.simId.setText(String.valueOf(heartbeatLog.getSimId()));
            viewHolder.totalSize.setText(String.valueOf(heartbeatLog.getTotalSize()));
            viewHolder.availableSize.setText(String.valueOf(heartbeatLog.getAvailableSize()));
            viewHolder.availableMemory.setText(String.valueOf(heartbeatLog.getAvailableMemory()));
            viewHolder.phoneNum.setText(String.valueOf(heartbeatLog.getPhoneNum()));
            viewHolder.status.setText(String.valueOf(heartbeatLog.getStatus()));
            viewHolder.timestamp.setText(String.valueOf(heartbeatLog.getTimestamp()));
        }

        return view;

    }
}
















