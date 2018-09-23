package com.example.android.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class SearchActivity extends AppCompatActivity {

    private EditText mSearchTerm;

    private ImageButton mSearchButton;

    private RecyclerView mRecycleView;

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mSearchTerm = findViewById(R.id.search_term);
        mRecycleView = findViewById(R.id.response_view);
        mSearchButton = findViewById(R.id.search_button);


        mRecycleView.setHasFixedSize(true);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));


        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchTerm = mSearchTerm.getText().toString();

                userSearch(searchTerm.toLowerCase());

            }
        });

    }

    private void userSearch(String searchTerm) {

        Toast.makeText(SearchActivity.this, "Searching...", Toast.LENGTH_LONG).show();

        Query mDatabaseQuery = mDatabase.orderByChild("name").startAt(searchTerm).endAt(searchTerm+"\uf8ff");

        FirebaseRecyclerOptions<Users> users =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mDatabaseQuery, Users.class)
                        .build();



        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(users) {
            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(UserViewHolder holder, int position, Users model) {

                holder.setDetails(model.getName(), model.getStatus(), model.getThumb_image());

                final String userUID = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(SearchActivity.this, UserProfileActivity.class);
                        profileIntent.putExtra("userUID", userUID);
                        startActivity(profileIntent);

                    }
                });

            }


        };


        adapter.startListening();
        mRecycleView.setAdapter(adapter);


    }


    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDetails(String name, String status, String image) {
            TextView profile_name = mView.findViewById(R.id.user_name);
            TextView profile_status = mView.findViewById(R.id.status);
            CircleImageView profile_image = mView.findViewById(R.id.profile_thumb);


            profile_name.setText(name);
            profile_status.setText(status);
            Picasso.get().load(image).placeholder(R.drawable.ic_person_gray_256dp).into(profile_image);

        }
    }

}
