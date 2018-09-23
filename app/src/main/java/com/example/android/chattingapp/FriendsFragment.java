package com.example.android.chattingapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private static View mView;
    private DatabaseReference mFriendReference;
    private DatabaseReference mUserReference;
    private FirebaseAuth mAuth;


    private String online_uid;

    private View rootView;

    private RecyclerView mRecyclerView;

    public FriendsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mRecyclerView = rootView.findViewById(R.id.friendsList);
        mAuth = FirebaseAuth.getInstance();
        online_uid = mAuth.getCurrentUser().getUid();

        mFriendReference = FirebaseDatabase.getInstance().getReference().child("friends").child(online_uid);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users");

        mUserReference.keepSynced(true);
        mFriendReference.keepSynced(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final Query mDatabaseQuery = mFriendReference.orderByChild("became_friend_on");

        FirebaseRecyclerOptions<Friends> friends =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mDatabaseQuery, Friends.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friends) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Friends model) {

                FriendsViewHolder.setDate(model.getBecame_friends_on());
                Log.d("RESULT OF QUERY","onBindViewHolder() returned: " + model.getBecame_friends_on());

                final String userUID = getRef(position).getKey();

                mUserReference.child(userUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        FriendsViewHolder.setUserName(userName);
                        FriendsViewHolder.setStatus(userStatus);
                        FriendsViewHolder.setThumb(userThumb);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list, parent, false);

                return new FriendsViewHolder(view);
            }
        };


        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public static void setDate(String date){

            TextView friendSince = mView.findViewById(R.id.date);
            friendSince.setText(date);

        }

        public static void setUserName(String name){

            TextView userName = mView.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public static void setStatus(String status){

            TextView userStatus = mView.findViewById(R.id.status);
            userStatus.setText(status);
        }

        public static void setThumb(final String url){

            final CircleImageView userThumb = mView.findViewById(R.id.profile_thumb);
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE).into(userThumb, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(url).into(userThumb);
                }
            });
        }
    }
}



