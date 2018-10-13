package com.example.android.chattingapp;

public class Friends {

    public Long became_friend_on ;

    public Friends(){

    }

    public Friends(Long became_friend_on) {
        this.became_friend_on = became_friend_on;
    }

    public String getBecame_friends_on() {
        return became_friend_on.toString();
    }

    public void setBecame_friends_on(Long became_friend_on) {
        this.became_friend_on = became_friend_on;
    }
}
