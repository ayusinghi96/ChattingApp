package com.example.android.chattingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeStatusActivity extends AppCompatActivity {

    private Toolbar mStatusToolbar;

    private Button mChangeStatus;

    private TextInputLayout mStatus;

    private DatabaseReference mDatabase;

    private FirebaseUser current_user;

    private ProgressDialog mStatusProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        mStatusToolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(mStatusToolbar);
        getSupportActionBar().setTitle("New Status");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mChangeStatus = findViewById(R.id.update_status);
        mStatus = findViewById(R.id.status_update_bar);

        String status = getIntent().getStringExtra("status");
        mStatus.getEditText().setText(status);

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mStatusProgressDialog = new ProgressDialog(ChangeStatusActivity.this);
                mStatusProgressDialog.setTitle("Updating Status");
                mStatusProgressDialog.setMessage("Please wait while your status is updated");
                mStatusProgressDialog.setCanceledOnTouchOutside(false);
                mStatusProgressDialog.show();

                String newStatus = mStatus.getEditText().getText().toString();

                current_user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = current_user.getUid();

                mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("status");
                mDatabase.setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            mStatusProgressDialog.dismiss();
                            Intent backIntent = new Intent(ChangeStatusActivity.this, SettingsActivity.class);
                            startActivity(backIntent);
                            finish();

                        }else{
                            mStatusProgressDialog.cancel();
                            Toast.makeText(ChangeStatusActivity.this, "Status not updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
