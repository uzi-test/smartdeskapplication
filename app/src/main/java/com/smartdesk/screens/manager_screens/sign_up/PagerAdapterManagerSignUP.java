package com.smartdesk.screens.manager_screens.sign_up;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smartdesk.R;

public class PagerAdapterManagerSignUP extends FragmentPagerAdapter {

    public static int pagesCount=2;
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.step_1, R.string.step_2};
    private final Context mContext;

    public PagerAdapterManagerSignUP(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentManagerStep1(mContext);
                break;
            case 1:
                fragment = new FragmentManagerStep2(mContext);
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
        return pagesCount;
    }
}