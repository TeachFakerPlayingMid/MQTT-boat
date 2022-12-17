package com.example.mqtt_boat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.PrepareView;
import com.dueeeke.videoplayer.player.VideoView;

import java.util.HashMap;

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoActivity";
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/fileimage/";
    private static final String IN_PATH = Environment.getExternalStorageDirectory().getPath() + "/fileimage/";

    private VideoView mVideoView;                   // 视频播放器
    private StandardVideoController controller;     // 播放控制器
    //视屏切换封面【网络地址】
    private String cover_url = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fimages%2F20200204%2F09078d00427944f2a09c5078aeefe947.jpeg&refer=http%3A%2F%2F5b0988e595225.cdn.sohucs.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1633788695&t=7fc5835a4fbad975a8dba2860bda404d";
    // 视频资源
    private HashMap<String, MyVideo> hm_videos = new HashMap<String, MyVideo>();                  // 键-视频id, 值-视频信息构成的对象
    private String selected_videoID = new String();                                 // 待播放视频的id
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //checkAccouts();

            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝


            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            }
        }


        // 支持的三种网络视频类型 http, hls(m3u8),rtmp
        // 备选：神奇女侠预告片 http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4
//        hm_videos.put("http999", new MyVideo("一号艇实况", "https://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4", false, "http999"));    // http类型
        hm_videos.put("http999", new MyVideo("一号艇实况", "http://443389w6t3.51vip.biz/yuchen/yuchen.m3u8", true, "http999"));    // http类型
        hm_videos.put("http998", new MyVideo("二号艇实况", "https://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4", false, "http998"));    // http类型
//        hm_videos.put("hls999", new MyVideo("CCTV1高清直播,HLS","https://ivi.bupt.edu.cn/hls/cctv1hd.m3u8", true, "hls999"));                                // hls类型
        hm_videos.put("rtmp999", new MyVideo("三号艇实况", "rtmp://media3.scctv.net/live/scctv_800", true, "rtmp999"));                                 // rtmp类型
//        hm_videos.put("http998", new MyVideo("2020电影回顾","https://vfx.mtime.cn/Video/2019/12/30/mp4/191230190451257319_1080.mp4", false, "http998"));    // http类型
//        hm_videos.put("hls999", new MyVideo("2021电影回顾","https://112.50.243.8/PLTV/88888888/224/3221225922/1.m3u8", true, "hls999"));    // http类型

        mVideoView = findViewById(R.id.player); // 视频播放器对象

        // 监听器：监听播放器的状态变化，共定义有8种状态，Ctrl+点击下面的字面值进入定义页面
        mVideoView.addOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                if (playState == VideoView.STATE_PREPARING) {                           // 正在准备播放中
                    Log.d("Output", "VideoView.STATE_PREPARING");
                } else if (playState == VideoView.STATE_PREPARED) {                     // 准备好播放时触发
                    Log.d("Output", "VideoView.STATE_PREPARED");
                } else if (playState == VideoView.STATE_PLAYING) {                       // 开始播放时触发
                    Log.d("Output", "VideoView.STATE_PLAYING");
                } else if (playState == VideoView.STATE_PAUSED) {                       // 暂停时触发
                    Log.d("Output", "VideoView.STATE_PAUSED");
                } else if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {           // 视频播放完成时触发
                    Log.d("Output", "VideoView.STATE_PLAYBACK_COMPLETED");
                } else if (playState == VideoView.STATE_BUFFERING) {                    // 跳转到某一位置，开始缓冲时触发
                    Log.d("Output", "VideoView.STATE_BUFFERING");
                } else if (playState == VideoView.STATE_BUFFERED) {                     // 跳转到某一位置，缓冲完成后触发
                    Log.d("Output", "VideoView.STATE_BUFFERED");
                }
            }
        });

        RadioGroup rg_videos = (RadioGroup) findViewById(R.id.rg_videos);    // 创建单选按钮组对象
        int checkedId = rg_videos.getCheckedRadioButtonId();                // 获取单选按钮组中被选中按钮的id
        playVideo(checkedId);                                               // 播放被选中按钮对应的视频

        // 单选按钮组中切换选中按钮时的监听事件
        rg_videos.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                playVideo(checkedId);    // 播放被选中按钮对应的视频
            }
        });
    }

    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(this)
                .setTitle("存储权限")
                .setMessage("无人艇客户端需要获取存储空间\n否则，您将无法正常获取水质实况画面")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    /*
     * 根据被选中按钮的id查询视频相关信息，启动视频播放
     */
    public void playVideo(int checkedId) {
        switch (checkedId) {
            case R.id.rb_http999:
                selected_videoID = "http999";
                break;
            case R.id.rb_hls999:
                selected_videoID = "http998";
                break;
            case R.id.rb_rtmp999:
                selected_videoID = "rtmp999";
                break;
        }

        // 获取视频对象，播放对应的视频
        MyVideo myVideo = hm_videos.get(selected_videoID);
        String videoTitle = myVideo.getVideoTitle();    // 选中视频的名称
        String videoUrl = myVideo.getVideoUrl();        // 选中视频的url
        Boolean isLive = myVideo.getLive();             // 选中视频是否为直播类型
        String videoID = myVideo.getVideoID();          // 选中视频的id
        initMyPlayer(cover_url, videoTitle, videoUrl, isLive, true);    // 切换到新视频并自动播放
    }


    /*
     * 切换到新视频并自动播放
     */
    public void initMyPlayer(String cover_url, String videoTitle, String videoUrl, Boolean isLive, Boolean autoStart) {
        // 设置标题栏标题
        setTitle(videoTitle);

        // 创建控制器并设置视频标题、是否为直播类型
        controller = new StandardVideoController(this);
        controller.addDefaultControlComponent(videoTitle, isLive);  // 菜单、标题等显示内容

        // 把封面添加到控制器
        PrepareView prepareView = new PrepareView(this);    // 准备播放界面
        ImageView thumb = prepareView.findViewById(R.id.thumb);     // 封面图
        Glide.with(this).load(cover_url).into(thumb);       // 把网上的一张图片作为封面
        controller.addControlComponent(prepareView);                // 把封面添加到控制器

        // 释放当前正在播放的视频
        mVideoView.setVideoController(null);                        // 移除Controller
        mVideoView.release();                                       // 释放当前播放的视频

        // 加载新的视频
        mVideoView.setVideoController(controller);                  // 把控制器添加到播放器
        mVideoView.setUrl(videoUrl);                                // 设置待播放视频的地址

        if (autoStart) mVideoView.start();                          // 播放视频
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


    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
            Log.d("Output", "onResume");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
            Log.d("Output", "onPause");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            Log.d("Output", "onDestroy");
        }
    }

    @Override
    public void onBackPressed() {
        if (mVideoView == null || !mVideoView.onBackPressed()) {
            super.onBackPressed();
            Log.d("Output", "onBackPressed");
        }
    }

    public void onButtonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.screen_shot:
                ImageView imageView = findViewById(R.id.iv_screen_shot);
                Bitmap bitmap = mVideoView.doScreenShot();                         //定义一个接收截图的类型
                imageView.setImageBitmap(bitmap);
                ImgUtils.saveImageToGallery2(VideoActivity.this, bitmap);  //调用ImgUtils类
                break;
        }
    }

}







