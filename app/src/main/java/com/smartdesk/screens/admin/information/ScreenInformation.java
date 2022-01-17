package com.smartdesk.screens.admin.information;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ScreenInformation extends AppCompatActivity {

    private Activity context;

    Integer activeTotalManagers = 0, activeTotalDeskUser = 0;
    Integer blockedTotalManagers = 0, blockedTotalDeskUser = 0;
    Integer requestNewTotalManagers = 0, requestNewTotalDeskUser = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_information);
        context = this;
        actionBar("Information");
        initLoadingBarItems();
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @SuppressLint("SetTextI18n")
    public void getData() {
        startAnim();
        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    stopAnim();
                    List<SignupUserDTO> users = queryDocumentSnapshots.toObjects(SignupUserDTO.class);
                    for (SignupUserDTO obj : users) {
                        if (obj.getUserStatus().equalsIgnoreCase(Constants.activeStatus) && obj.getRole().equals(Constants.managerRole))
                            activeTotalManagers++;
                        else if (obj.getUserStatus().equalsIgnoreCase(Constants.blockedStatus) && obj.getRole().equals(Constants.managerRole))
                            blockedTotalManagers++;
                        else if (obj.getUserStatus().equalsIgnoreCase(Constants.activeStatus) && obj.getRole().equals(Constants.deskUserRole))
                            activeTotalDeskUser++;
                        else if (obj.getUserStatus().equalsIgnoreCase(Constants.blockedStatus) && obj.getRole().equals(Constants.deskUserRole))
                            blockedTotalDeskUser++;
                        else if (obj.getUserStatus().equalsIgnoreCase(Constants.newAccountStatus) && obj.getRole().equals(Constants.deskUserRole))
                            requestNewTotalDeskUser++;
                        else if (obj.getUserStatus().equalsIgnoreCase(Constants.newAccountStatus) && obj.getRole().equals(Constants.managerRole))
                            requestNewTotalManagers++;
                    }
                    ((TextView) findViewById(R.id.activeTotalManager)).setText("" + activeTotalManagers);
                    ((TextView) findViewById(R.id.activeTotalDeskUser)).setText("" + activeTotalDeskUser);
                    ((TextView) findViewById(R.id.blockedTotalManagers)).setText("" + blockedTotalManagers);
                    ((TextView) findViewById(R.id.blockedTotalDeskUser)).setText("" + blockedTotalDeskUser);
                    ((TextView) findViewById(R.id.registerNewTotalDeskUser)).setText("" + requestNewTotalDeskUser);
                    ((TextView) findViewById(R.id.registerNewTotalManager)).setText("" + requestNewTotalManagers);
                })
                .addOnFailureListener(e -> {
                    stopAnim();
                    UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_LONG);
                });
    }

    public void actionBar(String actionTitle) {
        Toolbar a = findViewById(R.id.actionbarInclude).findViewById(R.id.toolbar);
        setSupportActionBar(a);
        ((TextView) findViewById(R.id.actionbarInclude).findViewById(R.id.actionTitleBar)).setText(actionTitle);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(context, R.drawable.icon_chevron_left_blue));
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