package com.example.android.chattingapp;

public class Users {

    public String name, thumb_image, status;

    public Users(){

    }

    public Users(String name, String thumb_image, String status){
        this.thumb_image = thumb_image;
        this.name = name;
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
