package com.example.youtubeclone.Models;

public class ContentModel {
    String id, publisher, playlist, type, videoid, video_description, video_url, video_tag, video_title, date;
    long views;

    public ContentModel(String id, String publisher, String playlist, String type, String videoid, String video_description, String video_url, String video_tag, long views, String video_title, String date) {
        this.id = id;
        this.publisher = publisher;
        this.playlist = playlist;
        this.type = type;
        this.videoid = videoid;
        this.video_description = video_description;
        this.video_url = video_url;
        this.video_tag = video_tag;
        this.views = views;
        this.video_title = video_title;
        this.date = date;
    }

    public ContentModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

    public String getVideo_description() {
        return video_description;
    }

    public void setVideo_description(String video_description) {
        this.video_description = video_description;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_tag() {
        return video_tag;
    }

    public void setVideo_tag(String video_tag) {
        this.video_tag = video_tag;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}