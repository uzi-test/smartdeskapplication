package com.smartdesk.screens.manager_screens.sign_up;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.constants.PermisionCode;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.screens.user_management.signup_otp.ScreenSignUpOTP;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.utility.library.PageStepIndicator;
import com.smartdesk.utility.location.FusedLocation;
import com.smartdesk.utility.memory.MemoryCache;

public class ScreenMangerSignup extends AppCompatActivity {

    private Activity context;

    private PageStepIndicator indicator;
    private ViewPager viewPager;
    private PagerAdapterManagerSignUP _pagerAdapterManagerSignUP;

    public static boolean isOkayStep1 = false;
    public static boolean isOkayStep2 = false;

    public Button nextBtn;
    public LinearLayout linearHide;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_desk_user_signup);
        context = this;
        Constants.const_usersSignupDTO = new SignupUserDTO();
        Constants.const_usersSignupDTO.setRole(Constants.managerRole);
        actionbar();
        UtilityFunctions.setupUI(findViewById(R.id.parent), context);
        initLoadingBarItems();
        nextBtn = findViewById(R.id.nextbtn);
        linearHide = findViewById(R.id.linearHide);
        _pagerAdapterManagerSignUP = new PagerAdapterManagerSignUP(context, getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        indicator = findViewById(R.id.tabs);
        viewPager.setAdapter(_pagerAdapterManagerSignUP);
        indicator.setupWithViewPager(viewPager);
        indicator.setStepsCount(2);
        indicator.setCurrentStepPosition(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    nextBtn.setText("Submit");
                } else {
                    nextBtn.setText("Next");
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        simpleTextWithOkButton(context, getResources().getString(R.string.note_register));
    }

    public void simpleTextWithOkButton(Activity activity, String textMsg) {
        try {
            final AlertDialog confirmationAlert = new AlertDialog.Builder(activity).create();
            final View dialogView = activity.getLayoutInflater().inflate(R.layout.alert_note_dialog, null);
            ((TextView) dialogView.findViewById(R.id.noteText)).setText(textMsg);
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                new FusedLocation(context, false).startLocationUpdates(false);
                if (askForLocationPermission()) {
                    new FusedLocation(context, false).startLocationUpdates(false);
                }
            });
            confirmationAlert.setView(dialogView);
            confirmationAlert.setCancelable(false);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean askForLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allTrue = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermisionCode.MY_LOCATION_PERMISSIONS_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PermisionCode.MY_LOCATION_PERMISSIONS_CODE);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PermisionCode.MY_LOCATION_PERMISSIONS_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PermisionCode.MY_LOCATION_PERMISSIONS_CODE);
            }
            return allTrue;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermisionCode.MY_LOCATION_PERMISSIONS_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new FusedLocation(context, false).startLocationUpdates(false);
                } else {
                    boolean showRationale = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                            if (!showRationale)
                                dialogForNeverAskAgain("Prompt to the Application Permission Setting & Allow Location");
                            else {
                                locationDialog("You can't go further until you didn't allow Location Permission");
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
                break;
            }
        }
    }

    //Take you to the app setting where you allow permission
    public void dialogForNeverAskAgain(String msg) {
        try {
            final AlertDialog confirmationAlert = new AlertDialog.Builder(this).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
            ((TextView) dialogView.findViewById(R.id.noteText)).setText(msg);
            ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Location Permission");
            ((Button) dialogView.findViewById(R.id.yesbtn)).setText("Go");
            ((Button) dialogView.findViewById(R.id.nobtn)).setText("Back");
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, PermisionCode.MY_LOCATION_PERMISSIONS_CODE);
                } else {
                    if (askForLocationPermission()) {
                        new FusedLocation(context, false).startLocationUpdates(false);
                    }
                }
            });
            dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                finish();
            });
            confirmationAlert.setView(dialogView);
            confirmationAlert.setCancelable(false);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (Exception ex) {

        }
    }

    public void locationDialog(final String msg) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                final AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
                final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
                ((TextView) dialogView.findViewById(R.id.noteText)).setText(msg);
                ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Location Permission");
                ((Button) dialogView.findViewById(R.id.yesbtn)).setText("Allow");
                ((Button) dialogView.findViewById(R.id.nobtn)).setText("Back");
                dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                    confirmationAlert.dismiss();
                    if (askForLocationPermission()) {
                        new FusedLocation(context, false).startLocationUpdates(false);
                    }
                });
                dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> {
                    confirmationAlert.dismiss();
                    finish();
                });
                confirmationAlert.setView(dialogView);
                confirmationAlert.setCancelable(false);
                confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                confirmationAlert.show();
            } catch (Exception ex) {

            }
        }, 0);
    }

    private void actionbar() {
        Toolbar toolbar = findViewById(R.id.actionbarInclude).findViewById(R.id.toolbar);
        ((TextView) findViewById(R.id.actionbarInclude).findViewById(R.id.actionTitleBar)).setText("Manager Registration");
        setSupportActionBar(toolbar);
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
        UtilityFunctions.hideSoftKeyboard(this);
        finish();
    }

    public Fragment getReferenceFragment(int page) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + page);
    }

    public void changeStateOnValid() {
        Constants.const_usersSignupDTO.setWorkerLat(Constants.const_lat + 0.00030);
        Constants.const_usersSignupDTO.setWorkerLng(Constants.const_lng + 0.00030);
        int position = viewPager.getCurrentItem();
        FragmentManagerStep1 fragmentManagerStep1 = (FragmentManagerStep1) getReferenceFragment(0);
        FragmentManagerStep2 fragmentManagerStep2 = (FragmentManagerStep2) getReferenceFragment(1);
        if (position == 0) {
            fragmentManagerStep1.validationsAndGetValues();
            if (isOkayStep1) {
                changePage(1);
            }
        } else if (position == 1) {
            fragmentManagerStep2.validationsAndGetValues();
            fragmentManagerStep1.validationsAndGetValues();
            if (!isOkayStep1)
                changePage(0);
            else if (!isOkayStep2)
                changePage(1);
            else {
                ((ScreenMangerSignup) context).startAnim();
                try {
                    String encryptedMobile = EncryptionDecryption.encryptionNormalText(Constants.const_usersSignupDTO.getWorkerPhone());
                    Query queryNumber = FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).whereEqualTo("workerPhone", encryptedMobile);
                    queryNumber.get().addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    isOkayStep1 = false;
                                    fragmentManagerStep1.met_phoneNumber.setError("Phone Number already registered");
                                    UtilityFunctions.orangeSnackBar((Activity) context, "Phone Number already registered!", Snackbar.LENGTH_SHORT);
                                }
                                Query queryCnic = FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).whereEqualTo("workerEmail", Constants.const_usersSignupDTO.getWorkerEmail());
                                queryCnic.get().addOnCompleteListener(querySnapshotTask -> {
                                    try {
                                        if (querySnapshotTask.isSuccessful()) {
                                            ((ScreenMangerSignup) context).stopAnim();
                                            if (!querySnapshotTask.getResult().isEmpty()) {
                                                isOkayStep1 = false;
                                                fragmentManagerStep1.et_email.setError("Email already registered");
                                                UtilityFunctions.orangeSnackBar((Activity) context, "Cnic already registered!", Snackbar.LENGTH_SHORT);
                                            }
                                            if (!isOkayStep1) {
                                                changePage(0);
                                            } else {
                                                new Handler(Looper.getMainLooper()).postDelayed(() -> showAlertDialog(context, "Are you sure, you want to create an account with the provided information?"), 0);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ((ScreenMangerSignup) context).stopAnim();
                                    }
                                }).addOnFailureListener(e -> {
                                    ((ScreenMangerSignup) context).stopAnim();
                                    isOkayStep1 = false;
                                    UtilityFunctions.redSnackBar((Activity) context, "No Internet!", Snackbar.LENGTH_SHORT);
                                });
                            }
                        } catch (Exception ex) {
                            ((ScreenMangerSignup) context).stopAnim();
                        }
                    }).addOnFailureListener(e -> {
                        ((ScreenMangerSignup) context).stopAnim();
                        isOkayStep1 = false;
                        UtilityFunctions.redSnackBar((Activity) context, "No Internet!", Snackbar.LENGTH_SHORT);
                    });
                } catch (Exception ex) {
                    ((ScreenMangerSignup) context).stopAnim();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void changePage(int page) {
        try {
            if (PagerAdapterManagerSignUP.pagesCount != 2) {
                PagerAdapterManagerSignUP.pagesCount = 2;
                indicator.setEnableStepClick(true);
            }
            viewPager.setCurrentItem(page);
            indicator.setCurrentStepPosition(page);
        } catch (Exception ex) {
            ex.printStackTrace();
            viewPager.setAdapter(_pagerAdapterManagerSignUP);
            viewPager.setCurrentItem(page);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermisionCode.MY_LOCATION_PERMISSIONS_CODE && resultCode == ((Activity) context).RESULT_OK) {
        }
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

    public void nextbuttonClick(View view) {
        UtilityFunctions.hideSoftKeyboard(this);
        changeStateOnValid();
    }
    //======================================== Show Loading bar ==============================================

    private void showAlertDialog(final Activity activity, String msg) {
        final AlertDialog confirmationAlert = new AlertDialog.Builder(activity).create();
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
        ((TextView) dialogView.findViewById(R.id.noteText)).setText(msg);
        ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Information Confirmation");
        ((Button) dialogView.findViewById(R.id.yesbtn)).setText("Yes");
        ((Button) dialogView.findViewById(R.id.nobtn)).setText("No");
        (dialogView.findViewById(R.id.llTITLECOLOR)).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.whatsapp_green_dark));
        (dialogView.findViewById(R.id.yesbtn)).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.whatsapp_green_dark));
        dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
            confirmationAlert.dismiss();
            UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenSignUpOTP.class), true, 0);
        });
        dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> {
            confirmationAlert.dismiss();
        });
        confirmationAlert.setView(dialogView);
        confirmationAlert.setCancelable(false);
        confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationAlert.show();
    }
}
