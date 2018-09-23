package com.example.android.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton settingsBackBtn;

    private DatabaseReference mDatabase;

    private File profile_image;

    private String image, name, thumb_image;

    private TextView mUserStatus, mUserName;

    private ImageView mProfileImageBackground;

    private CircleImageView mProfileImage;

    private FirebaseUser mCurrentUser;

    private Button mChangeStatusButton, mChangeImageButton;

    private StorageReference mImageStorageRef;

    private Bitmap thumbImageBitmap;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        settingsBackBtn = findViewById(R.id.backButton);
        mUserName = findViewById(R.id.users_profile_name);
        mUserStatus = findViewById(R.id.profile_status);
        mProfileImage = findViewById(R.id.profile_image);
        mChangeStatusButton = findViewById(R.id.change_status);
        mChangeImageButton = findViewById(R.id.change_image);
        mProfileImageBackground = findViewById(R.id.profile_image_bg);
        /*ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        mProfileImageBackground.setColorFilter(filter);*/



        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        mImageStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(status) && !TextUtils.isEmpty(thumb_image)){
                    mUserName.setText(name);
                    mUserStatus.setText(status);

                    Picasso.get().load(thumb_image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                    Picasso.get().load(image)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .into(mProfileImageBackground, new Callback() {
                                                @Override
                                                public void onSuccess() {


                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                    Picasso.get().load(image)
                                                            .into(mProfileImageBackground);

                                                }
                                            });

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(thumb_image)
                                            .into(mProfileImage);

                                    Picasso.get().load(image)
                                            .into(mProfileImageBackground);

                                }
                            });


                }else{
                    mUserStatus.setText(R.string.default_status);
                    mUserName.setText("User");
                    Picasso.get().load(R.drawable.ic_person_gray_256dp).into(mProfileImage);
                    Picasso.get().load(R.drawable.ic_person_gray_256dp).into(mProfileImageBackground);

                }




            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        settingsBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent upIntent = new Intent(SettingsActivity.this, MainActivity.class);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(upIntent);
                finish();
            }
        });

        mChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = mUserStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, ChangeStatusActivity.class);
                statusIntent.putExtra("status", status);
                startActivity(statusIntent);

            }
        });

        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                */

            }
        });


        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileImageIntent = new Intent(SettingsActivity.this, ProfileImageActivity.class);
                profileImageIntent.putExtra("downloadUrl", image);
                profileImageIntent.putExtra("userName", name);
                startActivity(profileImageIntent);

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
                mProgressDialog.setMessage("Please wait while your image is being uploaded..");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                String current_user_id = mCurrentUser.getUid();

                File imageFilePath = new File(resultUri.getPath());

                try {
                    thumbImageBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToBitmap(imageFilePath);


                    profile_image = new Compressor(this)
                            .setMaxWidth(700)
                            .setMaxHeight(700)
                            .setQuality(90)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .compressToFile(imageFilePath);
                }catch (IOException e){

                    e.printStackTrace();
                    Toast.makeText(SettingsActivity.this, "App ran into a problem: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                }

                ByteArrayOutputStream thumbImageStream = new ByteArrayOutputStream();
                thumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbImageStream);
                final byte[] thumbImageData = thumbImageStream.toByteArray();

                final StorageReference filepath = mImageStorageRef.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thumbImageFilePath = mImageStorageRef.child("thumb_images").child(current_user_id+".jpg");


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
                            Uri downloadUri = task.getResult();

                            final String downloadUrl = downloadUri.toString();

                            UploadTask thumb_uploadTask = thumbImageFilePath.putBytes(thumbImageData);

                            Task<Uri> thumb_urlTask = thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) throws Exception {
                                    if(!thumb_task.isSuccessful()){
                                        throw thumb_task.getException();
                                    }
                                    return thumbImageFilePath.getDownloadUrl();
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
                                                    mProgressDialog.dismiss();
                                                }else{
                                                    Toast.makeText(SettingsActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingsActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SettingsActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}