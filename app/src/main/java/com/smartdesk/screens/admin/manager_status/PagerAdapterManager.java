package com.smartdesk.screens.admin.manager_status;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smartdesk.R;

public class PagerAdapterManager extends FragmentPagerAdapter {

    private static final int[] TAB_TITLES = new int[]{R.string.userRequest, R.string.userAprroved};
    private final Context mContext;

    public PagerAdapterManager(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentMangerRequest((Activity) mContext);
                break;
            case 1:
                fragment = new FragmentManagerApproved((Activity) mContext);
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