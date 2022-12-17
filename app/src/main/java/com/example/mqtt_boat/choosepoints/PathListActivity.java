package com.example.mqtt_boat.choosepoints;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt_boat.R;

import java.util.List;
import java.util.Map;

public class PathListActivity extends AppCompatActivity {

    private ListView pathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_list);
        pathList=findViewById(R.id.pathList);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        List<Map<String, String>> listData = (List<Map<String, String>>) bundle.getSerializable("simpledata");    //???
        SimpleAdapter adapter = new SimpleAdapter(PathListActivity.this,
                listData,
                R.layout.layout_path_item,
                new String[]{"P_id","P_lon","P_lat"},
                new int[]{R.id.tv_ID,R.id.tv_lon,R.id.tv_lat});
        pathList.setAdapter(adapter);

        setTitle("规划数据表");
    }

    /*
     * 设置顶部工具条的标题
     */
    protected void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
}