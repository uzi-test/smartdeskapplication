package com.smartdesk.screens.admin._home;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.databinding.ScreenAdminHomeBinding;
import com.smartdesk.screens.admin._home.desk_user.PagerAdapterDeskUser;
import com.smartdesk.screens.admin.manager_status.ScreenBlockedManager;
import com.smartdesk.screens.admin.manager_status.ScreenAdminManager;
import com.smartdesk.screens.user_management.login.ScreenLogin;
import com.smartdesk.screens.user_management.notification.ScreenNotification;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.screens.admin.desk_user_status.ScreenBlockedDeskUser;
import com.smartdesk.screens.admin.information.ScreenInformation;
import com.smartdesk.screens.user_management.setting.ScreenAdminSetting;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class ScreenAdminHome extends AppCompatActivity {

    ScreenAdminHomeBinding binding;
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
        binding = ScreenAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        actionbar();
        initViewPager();
        UtilityFunctions.setupUI(findViewById(R.id.parent), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.name.setText(Constants.USER_NAME);
        binding.phoneNumber.setText(UtilityFunctions.getPhoneNumberInFormat(Constants.USER_MOBILE));
        UtilityFunctions.picassoGetCircleImage(context, Constants.USER_PROFILE, binding.profilePic, binding.profileShimmer, R.drawable.side_profile_icon);
        getNotificationCount();
    }

    private void getNotificationCount() {
        Constants.notificationCount = 0;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection).whereEqualTo("documentID", Constants.USER_DOCUMENT_ID)
                    .whereEqualTo("read", false).get()
                    .addOnSuccessListener(task -> {
                        for (QueryDocumentSnapshot document : task) {
                            Constants.notificationCount++;
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            ((TextView) findViewById(R.id.countNotify)).setText("" + Constants.notificationCount);
                        }, 0);
                    }).addOnFailureListener(e -> {
            });
        }, 0);
    }

    private void initViewPager() {
        PagerAdapterDeskUser sectionsPagerAdapter = new PagerAdapterDeskUser(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void actionbar() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toolbar toolbar = findViewById(R.id.actionbarInclude).findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            assert getSupportActionBar() != null;
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            ((TextView) findViewById(R.id.actionbarInclude).findViewById(R.id.actionTitleBar)).setText("Desk-Users Request");
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(context, binding.drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            toggle.setHomeAsUpIndicator(R.drawable.icon_menu);
            binding.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.drawerLayout.openDrawer(GravityCompat.START), 0);
        return true;
    }

    @Override
    public void onBackPressed() {
        closeDrawer();
    }

    public void closeDrawer() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0);
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

    public void blockedDeskUsers(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenBlockedDeskUser.class), false, 0);
    }

    public void notifications(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenNotification.class), false, 0);
    }

    public void settings(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenAdminSetting.class), false, 0);
    }

    public void logout(View view) {
        closeDrawer();
        UtilityFunctions.removeLoginInfoInSharedPreference(this);
        UtilityFunctions.logoutSnackBar(this, "Logout Successfully!", Snackbar.LENGTH_SHORT);
        UtilityFunctions.sendIntentClearPreviousActivity(context, new Intent(context, ScreenLogin.class), Constants.changeIntentDelay);
    }

    public void information(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenInformation.class), false, 0);
    }

    public void blockedManager(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenBlockedManager.class), false, 0);
    }

    public void ManagerRequests(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenAdminManager.class), false, 0);
    }
    //======================================== Show Loading bar ==============================================
}