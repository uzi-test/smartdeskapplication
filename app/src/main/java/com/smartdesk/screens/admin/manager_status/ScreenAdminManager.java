package com.smartdesk.screens.admin.manager_status;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.smartdesk.R;
import com.smartdesk.databinding.ScreenAdminManagerBinding;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.memory.MemoryCache;

public class ScreenAdminManager extends AppCompatActivity {

    ScreenAdminManagerBinding binding;
    Activity context;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenAdminManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        actionBar("Manager Requests");
        initViewPager();
        UtilityFunctions.setupUI(binding.parent, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initViewPager() {
        PagerAdapterManager sectionsPagerAdapter = new PagerAdapterManager(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    public void actionBar(String actionTitle) {
        Toolbar a = findViewById(R.id.actionbarInclude).findViewById(R.id.toolbar);
        setSupportActionBar(a);
        ((TextView) findViewById(R.id.actionbarInclude).findViewById(R.id.actionTitleBar)).setText(actionTitle);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.icon_chevron_left_blue));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    //======================================== Show Loading bar ==============================================
    private ObjectAnimator anim;
    private Boolean isLoad;

    public void startAnim() {
        isLoad = true;
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.loadingImage.setAlpha((float) 0.2);
        anim = UtilityFunctions.loadingAnim(context, binding.loadingImage);
        binding.loadingView.setOnTouchListener((v, event) -> isLoad);
    }

    public void stopAnim() {
        anim.end();
        binding.loadingView.setVisibility(View.GONE);
        binding.bgMain.setAlpha((float) 1);
        isLoad = false;
    }
    //======================================== Show Loading bar ==============================================
}