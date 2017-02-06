package com.example.linson.glidedemo;

import org.litepal.crud.DataSupport;

/**
 * Created by linson on 2017/2/4.
 */

public class ImageBean extends DataSupport {
    long id;
    String imageUrl;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
