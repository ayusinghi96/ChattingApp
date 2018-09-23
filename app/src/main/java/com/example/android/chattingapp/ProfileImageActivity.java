package com.example.android.chattingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ProfileImageActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageView mImageView;

    private String name, downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);


        Intent o = getIntent();
        name = o.getStringExtra("userName");
        downloadUrl = o.getStringExtra("downloadUrl");

        mToolbar = findViewById(R.id.profile_image_expand);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageView = findViewById(R.id.expanded_image);
        Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_person_gray_256dp).into(mImageView);

    }
}
