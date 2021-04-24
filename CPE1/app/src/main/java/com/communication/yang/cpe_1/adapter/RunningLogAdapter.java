package com.communication.yang.cpe_1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.communication.yang.cpe_1.R;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @Author : YangFan
 * @Date : 2020年11月19日 15:07
 * @effect :运行日志ListView布局适配器
 */
@Data
public class RunningLogAdapter extends BaseAdapter {
    private Context context;
    private Integer resourceId;
    private List<String> list = new ArrayList<String>();

    public RunningLogAdapter(Context context, Integer resourceId, List<String> list) {
        this.context = context;
        this.resourceId = resourceId;
        this.list = list;
    }

    //创建一个内部类，相当于一个枚举
    class ViewHold{
        //布局LOG日志
        TextView textView;
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

        //获取当前项的下标
        String data = list.get(position);

        /**
         * 1.初始化配置
         */
        //首次进入
        if(convertView == null){
            view = LayoutInflater.from(context).inflate
                    (resourceId,parent,false);

            viewHolder = new ViewHold();

            viewHolder.textView = view.findViewById(R.id.textView);

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
        if(data!=null){
            viewHolder.textView.setText(data);
        }

        return view;
    }
}
