package com.example.android.chattingapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // FirebaseAuth Instance
    private FirebaseAuth mAuth;

    // Main Toolbar instance
    private Toolbar mainToolbar;

    // Main View Pager instance for Inflating the layout
    private ViewPager mViewPager;

    // Custom Fragment pager adapter instance
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // Main Tab Layout Bar instance
    private TabLayout mainTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the Main Tool bar on
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        // Setting the title on the main_activity
        getSupportActionBar().setTitle("Chatting App");

        // Instantiate the FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Setting up View Pager
        mViewPager = findViewById(R.id.main_view_pager);

        // Instantiating the custom Adapter SectionPagerAdapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Setting up custom adapter to the View Pager.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Getting the Tab Layout
        mainTabLayout = findViewById(R.id.main_tabs);

        // Setting up the custom view pager to the tab layout
        mainTabLayout.setupWithViewPager(mViewPager);



    }

    /**
     * As the App starts current user id is extracted
     * If the current user is null
     * gotoStart method is implemented
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            gotoStart();
        }
    }


    /**
     * gotoStart method set up Intent to the Start Activity.
     */
    private void gotoStart() {
        Intent i = new Intent(MainActivity.this, StartActivity.class);
        startActivity(i);
        // finish() is called to stop user from  being able to come back to Main Activity on pressing back button.
        finish();
    }

    /**
     * Method is used to inflate a Menu List on the App Tool bar
     * @param menu It is the object for Menu class
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    /**
     * This method is used to apply functionality to the items of the menu.
     * @param item is object used to refer to the item of the menu list.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // if the item is Logout then the user is logged out of the account.
        // user is returned to the Start Activity
        if(item.getItemId() == R.id.mainLogout){

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Are you sure you want to log out?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // default method signOut is used to signOut of the current user login
                            FirebaseAuth.getInstance().signOut();
                            gotoStart();
                        }})
                    .setNegativeButton("CANCEL", null)
                    .setCancelable(false);

            AlertDialog dialog = builder.create();

            dialog.show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveButton.setTextColor(Color.parseColor("#FF0B8B42"));
            negativeButton.setTextColor(Color.parseColor("#FF0B8B42"));

        }

        // if the item selected is Account Settings then user is sent to Setting Activity
        else if(item.getItemId() == R.id.mainAccountSettings){

            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }

        else if(item.getItemId() == R.id.mainSearch){

            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(searchIntent);


        }

        return true;
    }
}
