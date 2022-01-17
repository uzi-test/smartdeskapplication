package com.smartdesk.screens.user_management.forget_password;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.utility.library.CustomEditext;
import com.smartdesk.screens.user_management.login.ScreenLogin;
import com.smartdesk.utility.memory.MemoryCache;

public class ScreenForgetPasswordStep3 extends AppCompatActivity {

    private Activity context;

    private CustomEditext password, confirmPassword;
    private Boolean isOkay;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_forget_password_otp_password);
        context = this;
        initLoadingBarItems();
        initIds();
        UtilityFunctions.setupUI(findViewById(R.id.bg_main), context);
    }

    private void initIds() {
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
    }

    public void resetPasswordBTN(View view) {
        isOkay = true;
        String pass = getText(password, "Invalid Password");
        String cPass = getText(confirmPassword, "Invalid Password");
        if (pass.length() < 6) {
            password.setError("Password min length should be 6");
            isOkay = false;
        }

        if (cPass.length() < 6) {
            confirmPassword.setError("Password min length should be 6");
            isOkay = false;
        }
        if (isOkay) {
            if (pass.equals(cPass)) {
                startAnim();
                new Thread(() -> {
                    String encryptedPassword = EncryptionDecryption.encryptionNormalText(pass);
                    FirebaseConstants.firebaseFirestore
                            .collection(FirebaseConstants.usersCollection)
                            .document(Constants.USER_DOCUMENT_ID)
                            .update("workerPassword", encryptedPassword).addOnSuccessListener(aVoid -> {
                        stopAnimOnUiThread();
                        UtilityFunctions.alertNoteWithOkButton(context, "Password Reset", "Your password has been reset successfully", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, new Intent(context, ScreenLogin.class));
                    }).addOnFailureListener(e -> {
                        stopAnimOnUiThread();
                        UtilityFunctions.alertNoteWithOkButton(context, "Password Reset", "Your password has not been reset, some error occurred", Gravity.CENTER, R.color.logo_red, R.color.white, false, false, null);
                    });
                }).start();
            } else {
                password.setError("Password does not match");
                confirmPassword.setError("Password does not match");
            }
        }
    }

    public String getText(EditText editText, String errorMSG) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, 6);
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
        anim = UtilityFunctions.loadingAnim(context, progressBar);
        load.setOnTouchListener((v, event) -> isLoad);
    }

    public void stopAnim() {
        anim.end();
        load.setVisibility(View.GONE);
        bg_main.setAlpha((float) 1);
        isLoad = false;
    }
    //======================================== Show Loading bar ==============================================

    public void stopAnimOnUiThread() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnim();
            }
        }, 0);
    }
}
