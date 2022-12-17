package com.example.mqtt_boat.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mqtt_boat.R;

import java.util.List;
import java.util.Map;
/**
 * 重写的列表适配器，程序未引用，学习用，自定义程度高！！！
 */
public class DataListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    public DataListAdapter(Context context, List<Map<String, String>> listData, int layout_list_item, String[] strings, int[] ints){
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {   //列表长度
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder{
        public ImageView imageView;
        public TextView tvTitle,tvTime,tvData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = mLayoutInflater.inflate(R.layout.layout_list_item,null);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.iv);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvData = convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        //给控件赋值
        holder.tvTitle.setText("采样点①");
        holder.tvTime.setText("2021-09-05 09:48:45");
        holder.tvData.setText("盐度：21.1‰");
        holder.imageView.setImageResource(R.drawable.log);
        return convertView;
    }
}
