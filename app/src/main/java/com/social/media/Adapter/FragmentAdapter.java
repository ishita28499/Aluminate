package com.social.media.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.social.media.Fragment.NotificationFragment;
import com.social.media.Fragment.RequestFrag;


public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new NotificationFragment();
            case 1: return new RequestFrag();
            default: return new NotificationFragment();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if(position == 0)
        {
            title = "Notification";
        }
        else if(position == 1)
        {
            title = "Requests";
        }
        return title;
    }
}
