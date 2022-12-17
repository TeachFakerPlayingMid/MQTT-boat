package com.example.mqtt_boat;
/**
 * 媒体数据类
 */
public class MyVideo {
    private String videoTitle;
    private String videoUrl;
    private Boolean isLive;
    private String  videoID;

    public MyVideo(String videoTitle, String videoUrl, Boolean isLive, String  videoID){
        this.videoTitle = videoTitle;
        this.videoUrl = videoUrl;
        this.isLive = isLive;
        this.videoID = videoID;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public Boolean getLive() {
        return isLive;
    }

    public String getVideoID() {
        return videoID;
    }
}
