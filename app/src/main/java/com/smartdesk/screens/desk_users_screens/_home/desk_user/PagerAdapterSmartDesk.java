package com.smartdesk.screens.desk_users_screens._home.desk_user;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smartdesk.R;

public class PagerAdapterSmartDesk extends FragmentPagerAdapter {

    private static final int[] TAB_TITLES = new int[]{R.string.deskAvailable, R.string.deskBooked};
    private final Context mContext;

    public PagerAdapterSmartDesk(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentDeskAvailable((Activity) mContext);
                break;
            case 1:
                fragment = new FragmentDeskBooked((Activity) mContext);
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }


    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}