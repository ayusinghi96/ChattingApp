package com.example.android.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddImageActivity extends AppCompatActivity {

    private Toolbar mAddImageToolbar;

    private Button mAddImageBtn;

    private File profile_image;

    private int success = 0;

    private ImageView mImageView;

    private Bitmap thumbImageBitmap;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgressDialog;

    private StorageReference mImageStorageRef;

    private DatabaseReference mDatabase;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        mAddImageToolbar = findViewById(R.id.addImageLayout);
        setSupportActionBar(mAddImageToolbar);
        getSupportActionBar().setTitle("Set Profile Image");

        mImageView = findViewById(R.id.addImageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();


        mImageStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);


        mAddImageBtn = findViewById(R.id.addImageButton);

        mAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(success == 0){

                    Toast.makeText(AddImageActivity.this, "Add a Profile Image by Clicking above!", Toast.LENGTH_SHORT).show();

                }else{

                    Intent mainIntent = new Intent(AddImageActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading Image..");
                mProgressDialog.setMessage("Please wait while your image is being uploaded");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File ImageFilePath = new File(resultUri.getPath());

                try {
                    thumbImageBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToBitmap(ImageFilePath);


                    profile_image = new Compressor(this)
                            .setMaxWidth(700)
                            .setMaxHeight(700)
                            .setQuality(90)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToFile(ImageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AddImageActivity.this, "App ran into a problem:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                ByteArrayOutputStream thumbImageStream = new ByteArrayOutputStream();
                thumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbImageStream);
                final byte[] thumbImageData = thumbImageStream.toByteArray();

                String current_user_id = mCurrentUser.getUid();

                final StorageReference filepath = mImageStorageRef.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thumbImgFilepath = mImageStorageRef.child("thumb_images").child(current_user_id+".jpg");

                UploadTask upLoadTask = filepath.putFile(Uri.fromFile(profile_image));

                Task<Uri> urlTask = upLoadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){

                            final Uri downloadUri = task.getResult();

                            final String downloadUrl = downloadUri.toString();

                            UploadTask thumb_uploadTask = thumbImgFilepath.putBytes(thumbImageData);

                            Task<Uri> thumb_urlTask = thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) throws Exception {
                                    if(!thumb_task.isSuccessful()){
                                        throw thumb_task.getException();
                                    }
                                    return thumbImgFilepath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> thumb_img_task) {

                                    Uri thumbImageDownloadUri = thumb_img_task.getResult();

                                    final String thumbImageDownloadUrl = thumbImageDownloadUri.toString();

                                    if(thumb_img_task.isSuccessful()){

                                        Map updateHashMap = new HashMap();
                                        updateHashMap.put("image", downloadUrl);
                                        updateHashMap.put("thumb_image", thumbImageDownloadUrl);


                                        mDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){
                                                    Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_person_gray_256dp).into(mImageView);
                                                    success = 1;
                                                    mProgressDialog.dismiss();
                                                }
                                                else{
                                                    Toast.makeText(AddImageActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }else{
                                        Toast.makeText(AddImageActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(AddImageActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
