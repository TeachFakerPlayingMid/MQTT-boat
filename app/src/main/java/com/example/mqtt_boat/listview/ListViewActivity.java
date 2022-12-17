package com.example.mqtt_boat.listview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;

import com.example.mqtt_boat.R;

import java.util.List;
import java.util.Map;

public class ListViewActivity extends Activity {

    private ListView dataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalistview);
        dataList=findViewById(R.id.lv_1);
//        dataList.setAdapter(new DataListAdapter(ListViewActivity.this));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        List<Map<String, String>>listData = (List<Map<String, String>>) bundle.getSerializable("data");
        SimpleAdapter adapter = new SimpleAdapter(ListViewActivity.this,
                listData,
                R.layout.layout_list_item,
                new String[]{"point","time","nacl"},
                new int[]{R.id.tv_title,R.id.tv_time,R.id.tv_text});
//        DataListAdapter adapter = new DataListAdapter(ListViewActivity.this,
//                listData,
//                R.layout.layout_list_item,
//                new String[]{"point","time","nacl"},
//                new int[]{R.id.tv_title,R.id.tv_time,R.id.tv_text});
        dataList.setAdapter(adapter);
    }
}
