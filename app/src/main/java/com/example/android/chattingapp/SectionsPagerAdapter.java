package com.example.android.chattingapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    /**
     * Public constructor of the custom SectionPagerAdapter class
     * @param fm instance of the FragmentManager
     */
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * The method is to set up the position of the items on the Tab
     * @param position indicates the position of a element in Tab
     * @return the method returns the Fragment for the current tab selected.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            default:
                return null;
        }
    }

    /**
     * @return The method returns the count of the number of items listed in the tab
     */
    @Override
    public int getCount() {
        return 3;
    }

    /**
     * The function returns the title of the tabs
     * @param position takes the position of the current tab
     * @return the title corresponding to the current position
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "CHATS";
            case 1:
                return "FRIENDS";
            case 2:
                return "REQUESTS";
            default:
                return null;
        }
    }
}
