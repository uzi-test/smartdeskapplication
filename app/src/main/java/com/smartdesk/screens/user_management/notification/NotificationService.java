package com.smartdesk.screens.user_management.notification;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.smartdesk.R;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.memory.MemoryCache;

public class NotificationService extends AppCompatActivity {

    private Activity context;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_notification_service);
        context = this;
        initLoadingBarItems();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    checkAccessToken();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }


    private void checkAccessToken() {
        final SharedPreferences prefs = getSharedPreferences("info", Context.MODE_PRIVATE);
        String AccessToken = prefs.getString("AccessToken", null);
        final String mobile = prefs.getString("Mobile", "");
        final String pass = prefs.getString("Password", "");

        if (AccessToken != null && !AccessToken.equals("")) {
//            Constants.ACCESS_TOKEN = AccessToken;
            startActivity(new Intent(NotificationService.this, ScreenNotification.class));
            finish();
        } else {
            loginApi(mobile, pass);
        }
    }

    private void loginApi(final String mobile, final String pass) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnim();
            }
        }, 0);
    }

    //======================================== Show Loading bar ==============================================
    private LinearLayout load, bg_main;
    private ObjectAnimator anim;
    private ImageView progressBar;
    private Boolean isLoad;

    private void initLoadingBarItems() {
        load = findViewById(R.id.loading_view);
        bg_main = findViewById(R.id.bg_main);
        progressBar = findViewById(R.id.loading_image);
    }

    public void startAnim() {
        isLoad = true;
        load.setVisibility(View.VISIBLE);
        bg_main.setAlpha((float) 0.2);
        anim = UtilityFunctions.loadingAnim(this, progressBar);
        load.setOnTouchListener((v, event) -> isLoad);
    }

    public void stopAnim() {
        anim.end();
        load.setVisibility(View.GONE);
        bg_main.setAlpha((float) 1);
        isLoad = false;
    }
    //======================================== Show Loading bar ==============================================
}