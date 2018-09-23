package com.example.android.chattingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView mUserName, mStatus, mFriendsDetails, mButtonState, mDeclineText;

    private ImageButton mRequest, mDecline;

    private ImageView mProfileImage;

    private CircleImageView mProfileImageThumbnail;

    private DatabaseReference mDatabase, mFriendRequest, mFriends;

    private FirebaseUser mCurrentUser;

    private String current_friend_state, timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent o = getIntent();
        final String userUID = o.getStringExtra("userUID");


        mUserName = findViewById(R.id.user_name);
        mStatus = findViewById(R.id.user_status);
        mFriendsDetails = findViewById(R.id.friends);
        mButtonState = findViewById(R.id.send_friend_request);
        mDeclineText = findViewById(R.id.decline_friend_request);
        mRequest = findViewById(R.id.request_button);
        mDecline = findViewById(R.id.decline_button);
        mProfileImage = findViewById(R.id.profile_image);
        mProfileImageThumbnail = findViewById(R.id.profile_image_circle);

        mDecline.setEnabled(false);
        mDecline.setVisibility(View.INVISIBLE);
        mDeclineText.setVisibility(View.INVISIBLE);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userUID);
        mFriendRequest = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mFriends = FirebaseDatabase.getInstance().getReference().child("friends");

        mDatabase.keepSynced(true);
        mFriendRequest.keepSynced(true);
        mFriends.keepSynced(true);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    final String userName = dataSnapshot.child("name").getValue().toString();
                    final String userStatus = dataSnapshot.child("status").getValue().toString();
                    final String userImage = dataSnapshot.child("image").getValue().toString();
                    final String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();


                    if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userStatus) && !TextUtils.isEmpty(userImage) && !TextUtils.isEmpty(userThumbImage)){

                        mUserName.setText(userName);
                        mStatus.setText(userStatus);
                        Picasso.get().load(userImage)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                Picasso.get().load(userThumbImage)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mProfileImageThumbnail, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get().load(userThumbImage).into(mProfileImageThumbnail);

                                            }
                                        });

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(userImage).into(mProfileImage);
                                Picasso.get().load(userThumbImage).into(mProfileImageThumbnail);

                            }
                        });

                    }else{

                        mUserName.setText("user");
                        mStatus.setText(R.string.default_status);
                        Picasso.get().load(R.drawable.ic_person_gray_256dp).into(mProfileImage);
                        Picasso.get().load(R.drawable.ic_person_gray_256dp).into(mProfileImageThumbnail);
                    }
                }catch (NullPointerException e){

                    e.getStackTrace();

                }

                mFriendRequest.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(userUID)){

                            String get_state_type = dataSnapshot.child(userUID).child("current_state").getValue().toString();

                            if(get_state_type.equals("request_received")){

                                current_friend_state = "request_received";
                                mButtonState.setText("Confirm Friend Request");

                                mDecline.setEnabled(true);
                                mDecline.setVisibility(View.VISIBLE);
                                mDeclineText.setVisibility(View.VISIBLE);

                            }else if(get_state_type.equals("request_sent")){

                                mButtonState.setText("Cancel Friend Request");
                                current_friend_state = "request_sent";


                            }
                        }else{

                            mFriends.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(userUID)){
                                        current_friend_state = "friend";
                                        mButtonState.setText("Unfriend");
                                        timestamp = dataSnapshot.child(userUID).child("became_friend_on").getValue().toString();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    Toast.makeText(UserProfileActivity.this, "Unable to process request", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toast.makeText(UserProfileActivity.this, "Unable to process request", Toast.LENGTH_SHORT).show();


                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e("UserProfileActivity: ",databaseError.getMessage());
                //Toast.makeText(UserProfileActivity.this, "Some unexpected error occurred!", Toast.LENGTH_LONG).show();

            }
        });

        current_friend_state = "not_friend";

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRequest.setEnabled(false);

                //----------------- NOT FRIENDS ----------------

                if(current_friend_state.equals("not_friend")){

                    mFriendRequest.child(mCurrentUser.getUid()).child(userUID).child("current_state").setValue("request_sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mFriendRequest.child(userUID).child(mCurrentUser.getUid()).child("current_state").setValue("request_received")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            current_friend_state = "request_sent";
                                                            mButtonState.setText("Cancel Request");
                                                            mRequest.setEnabled(true);

                                                        }else{

                                                            mRequest.setEnabled(true);
                                                            Toast.makeText(UserProfileActivity.this, "Unable to send Request", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                    else{

                                        mRequest.setEnabled(true);
                                        Toast.makeText(UserProfileActivity.this, "Unable to send Request", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }

                //---------------- IF FRIEND REQUEST ALREADY SENT ----------------

                if(current_friend_state.equals("request_sent")){

                    mRequest.setEnabled(false);

                    mFriendRequest.child(mCurrentUser.getUid()).child(userUID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mFriendRequest.child(userUID).child(mCurrentUser.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            current_friend_state = "not_friend";
                                                            mButtonState.setText("Send Friend Request");
                                                            mRequest.setEnabled(true);

                                                        }else{

                                                            mRequest.setEnabled(true);
                                                            Toast.makeText(UserProfileActivity.this
                                                                    , "Unable to cancel the friend request. Try again!"
                                                                    , Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }else{

                                        mRequest.setEnabled(true);
                                        Toast.makeText(UserProfileActivity.this
                                                , "Unable to cancel the friend request. Try again!"
                                                , Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }

                //--------------------- IF A REQUEST IS RECEIVED ----------------

                if(current_friend_state.equals("request_received")){

                    mRequest.setEnabled(false);

                    mFriends.child(mCurrentUser.getUid()).child(userUID).child("became_friend_on").setValue(ServerValue.TIMESTAMP)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mFriends.child(userUID).child(mCurrentUser.getUid()).child("became_friend_on").setValue(ServerValue.TIMESTAMP)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            mFriendRequest.child(mCurrentUser.getUid()).child(userUID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                mFriendRequest.child(userUID).child(mCurrentUser.getUid()).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if(task.isSuccessful()){

                                                                                                    current_friend_state = "friend";
                                                                                                    mButtonState.setText("Unfriend");
                                                                                                    mRequest.setEnabled(true);

                                                                                                    mDecline.setEnabled(false);
                                                                                                    mDecline.setVisibility(View.INVISIBLE);
                                                                                                    mDeclineText.setVisibility(View.INVISIBLE);


                                                                                                }else{

                                                                                                    mFriendRequest.child(mCurrentUser.getUid())
                                                                                                            .child(userUID)
                                                                                                            .child("current_state")
                                                                                                            .setValue("request_received")
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                                    if(task.isSuccessful()){

                                                                                                                        Toast.makeText(UserProfileActivity.this
                                                                                                                                ,"Request could not be excepted. Try again!"
                                                                                                                                , Toast.LENGTH_SHORT
                                                                                                                        ).show();

                                                                                                                        mRequest.setEnabled(true);

                                                                                                                    }else{

                                                                                                                        Toast.makeText(UserProfileActivity.this
                                                                                                                                ,"Request could not be excepted. Try again!"
                                                                                                                                , Toast.LENGTH_SHORT
                                                                                                                        ).show();

                                                                                                                        mRequest.setEnabled(true);

                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }else{

                                                                                Toast.makeText(UserProfileActivity.this
                                                                                        ,"Request could not be excepted. Try again!"
                                                                                        , Toast.LENGTH_SHORT
                                                                                ).show();

                                                                                mRequest.setEnabled(true);
                                                                            }
                                                                        }
                                                                    });

                                                        }else{

                                                            Toast.makeText(UserProfileActivity.this
                                                                    ,"Request could not be excepted. Try again!"
                                                                    , Toast.LENGTH_SHORT
                                                            ).show();

                                                            mRequest.setEnabled(true);
                                                        }
                                                    }
                                                });
                                    }else{

                                        Toast.makeText(UserProfileActivity.this
                                                ,"Request could not be excepted. Try again!"
                                                , Toast.LENGTH_SHORT
                                        ).show();

                                        mRequest.setEnabled(true);
                                    }
                                }
                            });

                }



                // -------------------- IF ALREADY FRIENDS -----------------

                if(current_friend_state.equals("friend")){

                    mRequest.setEnabled(false);

                    mFriends.child(mCurrentUser.getUid()).child(userUID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mFriends.child(userUID).child(mCurrentUser.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            current_friend_state = "not_friend";
                                                            mRequest.setEnabled(true);
                                                            mButtonState.setText("Send Friend Request");

                                                        }else{

                                                            mFriends.child(mCurrentUser.getUid()).child(userUID).child("became_friend_on").setValue(timestamp)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                mRequest.setEnabled(true);
                                                                                Toast.makeText(UserProfileActivity.this
                                                                                        , "Ran into a problem. Try again!"
                                                                                        , Toast.LENGTH_SHORT).show();
                                                                            }else{

                                                                                mRequest.setEnabled(true);
                                                                                Toast.makeText(UserProfileActivity.this
                                                                                        , "Ran into a problem. Try again!"
                                                                                        , Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }else{

                                        mRequest.setEnabled(true);
                                        Toast.makeText(UserProfileActivity.this
                                                , "Ran into a problem. Try again!"
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }


            }
        });


        mDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDecline.setEnabled(false);

                if(current_friend_state.equals("request_received")){

                    mFriendRequest.child(mCurrentUser.getUid()).child(userUID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        mFriendRequest.child(userUID).child(mCurrentUser.getUid()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            mButtonState.setText("Send Friend Request");
                                                            current_friend_state = "not_friend";
                                                            mDecline.setVisibility(View.INVISIBLE);
                                                            mDeclineText.setVisibility(View.INVISIBLE);


                                                        }else{

                                                            Toast.makeText(UserProfileActivity.this,
                                                                    "Unable to process request. Try again!",
                                                                    Toast.LENGTH_SHORT).show();
                                                            mDecline.setEnabled(true);
                                                        }
                                                    }
                                                });
                                    }else{

                                        Toast.makeText(UserProfileActivity.this,
                                                "Unable to process request. Try again!",
                                                Toast.LENGTH_SHORT).show();
                                        mDecline.setEnabled(true);
                                    }
                                }
                            });
                }
            }
        });


    }

}
