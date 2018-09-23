package com.example.android.chattingapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout mEmail, mPassword;
    private Button mSign_in_btn, mResendMail;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Toolbar mToolbarSignIn;
    private ProgressDialog mSignInProgressBar;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        mToolbarSignIn = findViewById(R.id.signIn_layout_toolbar);
        setSupportActionBar(mToolbarSignIn);
        getSupportActionBar().setTitle(R.string.sign_in);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSignInProgressBar = new ProgressDialog(this);


        mEmail = findViewById(R.id.email_sign_in);
        mPassword = findViewById(R.id.password_sign_in);
        mSign_in_btn = findViewById(R.id.submit_sign_in);
        mResendMail = findViewById(R.id.resend_mail);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);


        mResendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);
                mResendMail.setEnabled(false);

                mUser = FirebaseAuth.getInstance().getCurrentUser();
                if(mUser != null){

                    if(!mUser.isEmailVerified()){
                        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignInActivity.this, "Please check your email for verification. And again Sign in." , Toast.LENGTH_SHORT).show();

                                }else{
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignInActivity.this, "Problem sending email for verification. TRY AGAIN!" , Toast.LENGTH_SHORT).show();
                                }

                                mResendMail.setEnabled(true);
                            }
                        });
                    }else{
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SignInActivity.this, "Email already verified. Continue with Sign in!" , Toast.LENGTH_SHORT).show();
                    }
                }else{
                    sendEmailVerification();
                    mResendMail.setEnabled(true);
                }

            }
        });

        mSign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mSignInProgressBar.setTitle("Signing In!..");
                    mSignInProgressBar.setMessage("Please wait while you are Authenticated");
                    mSignInProgressBar.setCanceledOnTouchOutside(false);
                    mSignInProgressBar.show();

                    signInUser(email, password);
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter the required details!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmailVerification() {
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();


        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){

                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SignInActivity.this, "Please check your email for verification. And again Sign in.", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }else{
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(SignInActivity.this, "Email already verified. Continue with Sign in!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SignInActivity.this, "Problem sending verification mail!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(SignInActivity.this, "Please provide the above details", Toast.LENGTH_SHORT).show();
        }
    }


    private void signInUser(String email, String password) {

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseAuth.getInstance().signOut();
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            mUser = FirebaseAuth.getInstance().getCurrentUser();


                            if(mUser.isEmailVerified()){

                                mSignInProgressBar.dismiss();

                                Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);

                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(mainIntent);
                                finish();

                            }else{
                                mSignInProgressBar.dismiss();

                                Toast.makeText(SignInActivity.this, "Please verify your email and try again", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                            }



                        } else {

                            mSignInProgressBar.hide();
                            Toast.makeText(SignInActivity.this, R.string.authentication_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}