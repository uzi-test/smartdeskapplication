package com.smartdesk.screens.user_management.change_password;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;

public class ScreenChangePassword extends AppCompatActivity {

    private EditText currentPassword, newPassowrd, newConfirmPassword;
    private Boolean isOkay;
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
        setContentView(R.layout.screen_change_password);
        context = this;
        initLoadingBarItems();
        UtilityFunctions.setupUI(findViewById(R.id.parent), this);
        actionBar("Change Password");
        initIds();
    }

    private void initIds() {
        currentPassword = findViewById(R.id.currentpass);
        newPassowrd = findViewById(R.id.password);
        newConfirmPassword = findViewById(R.id.confirmPassword);
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

    boolean isfirst = false;

    public void UpdatePassowrd(View view) {
        isOkay = true;
        isfirst = false;

        String currentpassword = getDataFromEditext(currentPassword, "Password min length should be 6", 6);
        String newPassword = getDataFromEditext(newPassowrd, "Password min length should be 6", 6);
        String newCPassword = getDataFromEditext(newConfirmPassword, "Password min length should be 6", 6);

        if (newPassword.length() < 6) {
            newPassowrd.setError("Password min length should be 6");
            isOkay = false;
            if (!isfirst) {
                isfirst = true;
//                newPassowrd.requestFocus();
            }
        }

        if (newCPassword.length() < 6) {
            newConfirmPassword.setError("Password min length should be 6");
            isOkay = false;
            if (!isfirst) {
                isfirst = true;
//                newConfirmPassword.requestFocus();
            }
        }

        if (isOkay) {
            if (newPassword.equals(newCPassword)) {
                changePasswordAPI(currentpassword, newPassword);
            } else {
                isOkay = false;
                newPassowrd.setError("Password not match");
                newConfirmPassword.setError("Password not match");
                UtilityFunctions.orangeSnackBar(this, "Password not match", Snackbar.LENGTH_SHORT);
            }
        }
    }

    public void changePasswordAPI(String oldPassowrd, String newPassword) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startAnim();
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                    .get().addOnSuccessListener(documentSnapshot -> {
                new Thread(() -> {
                    try {
                        String pass = EncryptionDecryption.decryptionCypherText(documentSnapshot.toObject(SignupUserDTO.class).getWorkerPassword());
                        if (oldPassowrd.equals(pass)) {
                            String newPass = EncryptionDecryption.encryptionNormalText(newPassword);
                            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                                    .update("workerPassword", newPass)
                                    .addOnSuccessListener(aVoid -> {
                                        stopAnimOnUIThread();
                                        UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.USER_MOBILE, newPassword, Constants.USER_DOCUMENT_ID, true);
                                        UtilityFunctions.alertNoteWithOkButton(context, "Password Changed", "Your password has been changed successfully.", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        stopAnimOnUIThread();
                                        UtilityFunctions.alertNoteWithOkButton(context, "Password Changed", "Failed to change password.", Gravity.CENTER, R.color.SmartDesk_Editext_red, R.color.white, false, false, null);
                                    });
                        } else {
                            stopAnimOnUIThread();
                            UtilityFunctions.alertNoteWithOkButton(context, "Password Changed", "Current password is incorrect. Kindly provide the correct password", Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
                        }
                    } catch (Exception e) {
                        stopAnimOnUIThread();
                        e.printStackTrace();
                        UtilityFunctions.alertNoteWithOkButton(context, "Password Changed", "Failed to change password.", Gravity.CENTER, R.color.SmartDesk_Editext_red, R.color.white, false, false, null);
                    }
                }).start();

            }).addOnFailureListener(e -> {
                stopAnim();
                UtilityFunctions.alertNoteWithOkButton(context, "Password Changed", "Failed to change password.", Gravity.CENTER, R.color.SmartDesk_Editext_red, R.color.white, false, false, null);
            });
        }, 0);
    }

    private String getDataFromEditext(EditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            isOkay = false;
        }
        return text;
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

    public void stopAnimOnUIThread() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> stopAnim(), 0);
    }
}
