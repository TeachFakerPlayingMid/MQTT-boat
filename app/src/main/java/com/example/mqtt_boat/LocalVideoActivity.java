package com.example.mqtt_boat;

import android.net.Uri;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LocalVideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_local_video);
        VideoView video =(VideoView)findViewById(R.id.video);
        RadioGroup rg_videos = (RadioGroup) findViewById(R.id.rg_trials);    // 创建单选按钮组对象
        String uri1 = "android.resource://" + getPackageName() + "/" + R.raw.trial1;
        String uri2 = "android.resource://" + getPackageName() + "/" + R.raw.trial2;
        String uri3 = "android.resource://" + getPackageName() + "/" + R.raw.trial3;
        if(video!=null){
            video.setVideoURI(Uri.parse(uri1));
            video.requestFocus();
            video.start();
        }else {
            Toast.makeText(LocalVideoActivity.this,"不存在视频资源",Toast.LENGTH_SHORT).show();
        }
        setTitle("无人艇试航");

        //选择用艇指令事件【单选】
        rg_videos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.rb_trial1 == checkedId) {
                    video.setVideoURI(Uri.parse(uri1));
                } else if (R.id.rb_trial2 == checkedId) {
                    video.setVideoURI(Uri.parse(uri2));
                } else if (R.id.rb_trial3 == checkedId) {
                    video.setVideoURI(Uri.parse(uri3));
                }
                video.requestFocus();
                video.start();
            }
        });

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