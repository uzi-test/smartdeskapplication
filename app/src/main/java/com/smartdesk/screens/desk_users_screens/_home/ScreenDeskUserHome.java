package com.smartdesk.screens.desk_users_screens._home;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.databinding.ScreenDeskUserHomeBinding;
import com.smartdesk.screens.desk_users_screens._home.desk_user.PagerAdapterSmartDesk;
import com.smartdesk.screens.desk_users_screens.charging.ScreenWirelessCharging;
import com.smartdesk.screens.user_management.help.ScreenHelp;
import com.smartdesk.screens.user_management.login.ScreenLogin;
import com.smartdesk.screens.user_management.notification.ScreenNotification;
import com.smartdesk.screens.user_management.setting.ScreenDeskUserSetting;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.location.FusedLocation;
import com.smartdesk.utility.memory.MemoryCache;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static java.util.ResourceBundle.clearCache;

public class ScreenDeskUserHome extends AppCompatActivity {

    ScreenDeskUserHomeBinding binding;
    Activity context;

    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenDeskUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        actionbar();
        initViewPager();
        initIDS();
        UtilityFunctions.setupUI(findViewById(R.id.parent), context);
        new FusedLocation(context, true).startLocationUpdates(false);
    }

    private void initViewPager() {
        PagerAdapterSmartDesk sectionsPagerAdapter = new PagerAdapterSmartDesk(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
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

    private void initIDS() {
        binding.name.setText(Constants.USER_NAME);
        binding.phoneNumber.setText(UtilityFunctions.getPhoneNumberInFormat(Constants.USER_MOBILE));
        UtilityFunctions.picassoGetCircleImage(context, Constants.USER_PROFILE, binding.profilePic, binding.profileShimmer, R.drawable.side_profile_icon);
    }

    private void actionbar() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toolbar toolbar = findViewById(R.id.actionbarInclude).findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            assert getSupportActionBar() != null;
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            ((TextView) findViewById(R.id.actionbarInclude).findViewById(R.id.actionTitleBar)).setText("Smart Desks");
            drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(context, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            toggle.setHomeAsUpIndicator(R.drawable.icon_menu);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }, 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> drawer.openDrawer(GravityCompat.START), 0);
        return true;
    }

    @Override
    public void onBackPressed() {
        closeDrawer();
    }

    public void closeDrawer() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0);
    }

    public void showImageOn(String alertDialogImageURL, String name) {
        try {
            clearCache();
            final AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_big_image, null);
            ImageView img = dialogView.findViewById(R.id.imagePreview);
            TextView nameText = dialogView.findViewById(R.id.name);
            nameText.setText(name);
            ShimmerFrameLayout shimmer = dialogView.findViewById(R.id.shimmer);
            UtilityFunctions.noRoundImageCorner(this, img, alertDialogImageURL, shimmer);
            dialogView.findViewById(R.id.retake).setVisibility(View.GONE);
            dialogView.findViewById(R.id.hideDialog).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                confirmationAlert.cancel();
            });

            confirmationAlert.setView(dialogView);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    //======================================== Show Loading bar ==============================================
    private ObjectAnimator anim;
    private Boolean isLoad;

    public void startAnim() {
        isLoad = true;

        binding.loadingView.setVisibility(View.VISIBLE);
        binding.bgMain.setAlpha((float) 0.2);
        anim = UtilityFunctions.loadingAnim(this, binding.loadingImage);
        binding.loadingView.setOnTouchListener((v, event) -> isLoad);
    }

    public void stopAnim() {
        anim.end();
        binding.loadingView.setVisibility(View.GONE);
        binding.bgMain.setAlpha((float) 1);
        isLoad = false;
    }

    public void notifications(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenNotification.class), false, 0);
    }

    public void settings(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenDeskUserSetting.class), false, 0);
    }

    public void logout(View view) {
        closeDrawer();
        UtilityFunctions.removeLoginInfoInSharedPreference(this);
        UtilityFunctions.logoutSnackBar(this, "Logout Successfully!", Snackbar.LENGTH_SHORT);
        UtilityFunctions.sendIntentClearPreviousActivity(context, new Intent(context, ScreenLogin.class), Constants.changeIntentDelay);
    }

    public void help(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenHelp.class), false, 0);
    }

    public void wirelessCharging(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenWirelessCharging.class), false, 0);
    }
    //======================================== Show Loading bar ==============================================
}