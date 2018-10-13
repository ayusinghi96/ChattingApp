package com.example.android.chattingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mUser, mPassword, mEmail;

    private Button mSubmit;

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private ProgressDialog mRegisterProgressBar;

    private DatabaseReference mDatabase;

    private static final int PER_LOGIN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();


        mToolbar = findViewById(R.id.register_layout_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.register_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mRegisterProgressBar = new ProgressDialog(this);

        mUser = findViewById(R.id.user_name_reg);
        mEmail = findViewById(R.id.email_sign_in);
        mPassword = findViewById(R.id.password_sign_in);
        mSubmit = findViewById(R.id.submit_sign_in);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mUser.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mRegisterProgressBar.setTitle("Registering User!");
                    mRegisterProgressBar.setMessage("Please wait while account is being created");
                    mRegisterProgressBar.setCanceledOnTouchOutside(false);
                    mRegisterProgressBar.show();

                    RegisterUser(display_name, email, password);

                }else{
                    Toast.makeText(getApplicationContext(), "Please enter the required details!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void RegisterUser(final String display_name, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {



                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();


                    // Write a message to the database
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    HashMap<String, String> user_data = new HashMap<>();
                    user_data.put("name", display_name.toLowerCase());
                    user_data.put("image", "https://firebasestorage.googleapis.com/v0/b/chattingapp-3ed98.appspot.com/o/profile_images%2Fmsn-people-person-profile-user-icon-icon-search-engine-11.png?alt=media&token=a4205e70-6ed2-4413-abba-76ffd97cd7a8");
                    user_data.put("thumb_image", "https://firebasestorage.googleapis.com/v0/b/chattingapp-3ed98.appspot.com/o/profile_images%2Fmsn-people-person-profile-user-icon-icon-search-engine-11.png?alt=media&token=a4205e70-6ed2-4413-abba-76ffd97cd7a8");
                    user_data.put("status", "Hi there I am using Chatting App");

                    mDatabase.setValue(user_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mRegisterProgressBar.dismiss();

                                sendEmailVerificationMail();


                                Intent signIn = new Intent(RegisterActivity.this, SignInActivity.class);
                                signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(signIn);
                                finish();

                            }
                        }
                    });



                } else {
                    // If sign in fails, display a message to the user.
                    mRegisterProgressBar.hide();
                    Toast.makeText(RegisterActivity.this, R.string.authentication_failed, Toast.LENGTH_LONG).show();

                }
            }


        });
    }


    /**
     * Function to send email for verification of the registered email account
     */
    private void sendEmailVerificationMail() {

        final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        if(current_user != null){

            current_user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        Toast.makeText(RegisterActivity.this, "Please check your email for verification.", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            Toast.makeText(RegisterActivity.this, "Problem sending email for verification. TRY AGAIN!", Toast.LENGTH_LONG).show();
        }
    }




}
