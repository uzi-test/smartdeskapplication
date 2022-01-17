package com.smartdesk.screens.user_management.login;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.screens.desk_users_screens._home.ScreenDeskUserHome;
import com.smartdesk.screens.manager_screens._home.ScreenManagerHome;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.CustomEditext;
import com.smartdesk.utility.memory.MemoryCache;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LoginOTP extends AppCompatActivity {

    Boolean isAnyDialogOpen = false;
    Boolean isResend = false;
    PhoneAuthProvider.ForceResendingToken resendToken;
    private Activity context;
    private AlertDialog confirmationAlert;
    private CustomEditext code;
    private Button proceed, resend;
    private Boolean isOkay;
    private String phoneNumber;
    private String codeOtp;
    private String mVerificationId;
    private long otpExpire = Constants.timeoutOtp * 1000;

    private Handler handlerOtpExpire = new Handler();
    private Runnable otpExpireRunnable;
    private TextView timerTV;

    private int countResend = 0;
    //======================================== Show Loading bar ==============================================
    private LinearLayout load, bg_main;
    private ObjectAnimator anim;
    private ImageView progressBar;
    private Boolean isLoad;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            try {
                stopAnim();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            codeOtp = phoneAuthCredential.getSmsCode();
            if (codeOtp != null) {
                code.setText(codeOtp);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            try {
                stopAnim();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
                UtilityFunctions.redSnackBar(context, "Invalid request!", Snackbar.LENGTH_SHORT);
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
                UtilityFunctions.redSnackBar(context, "!", Snackbar.LENGTH_SHORT);
                UtilityFunctions.alertNoteWithOkButton(context, "Otp Verification", "OTP sending limit cross", Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
            }
        }


        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            try {
                new Handler(Looper.getMainLooper()).postDelayed(() -> stopAnim(), 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (isResend) {
                UtilityFunctions.greenSnackBar(context, "Code Resend Successfully!", Snackbar.LENGTH_LONG);
                isResend = false;
            }
            otpExpire = Constants.timeoutOtp * 1000;
            countDownStart();
            changeActiveButtons(true, false, R.color.SmartDesk_Blue, R.color.SmartDesk_Blue_Oppaque);
            //storing the verification id that is sent to the user
            mVerificationId = s;
            resendToken = forceResendingToken;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signup_otp);
        context = this;
        initIds();
        listenerOTPCode();
        initLoadingBarItems();
        UtilityFunctions.setupUI(findViewById(R.id.bg_main), context);
        if (Constants.USER_MOBILE != null && Constants.USER_MOBILE != "")
            phoneNumber = Constants.USER_MOBILE;
        System.out.println( phoneNumber+"-Phone NO");
//        else
//            phoneNumber = Constants.const_ConsumerSignupDTO.getWorkerPhone();
        phoneAuthticate();
    }

    private void listenerOTPCode() {
        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = code.getText().toString();
                if (str.length() == 6 && proceed.isEnabled()) {
                    isAnyDialogOpen = false;
                    codeOtp = str;
                    UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
                    verifyVerificationCode(codeOtp);
                }
            }
        });
    }

    private void phoneAuthticate() {
        startAnim();
        try {
            if (phoneNumber != null) {
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(FirebaseConstants.firebaseAuth)
                                .setPhoneNumber(Constants.countryCode + phoneNumber.substring(1))       // Phone number to verify
                                .setTimeout((long) Constants.timeoutOtp, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(context)                 // Activity (for callback binding)
                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    stopAnim();
                    UtilityFunctions.orangeSnackBar(context, "Please Login Again", Snackbar.LENGTH_LONG);
                    finish();
                }, 500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateCountDownText() {
        int minutes = (int) (otpExpire / 1000) / 60;
        int seconds = (int) (otpExpire / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTV.setText(timeLeftFormatted);
    }

    private void countDownStart() {
        otpExpireRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    otpExpire -= 1000;
                    if (otpExpire <= 0) {
                        handlerOtpExpire.removeCallbacks(otpExpireRunnable);
                        changeActiveButtons(false, true, R.color.SmartDesk_Blue_Oppaque, R.color.SmartDesk_Blue);
                    } else {
                        handlerOtpExpire.postDelayed(this, 1000);
                    }
                    updateCountDownText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handlerOtpExpire.postDelayed(otpExpireRunnable, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerOtpExpire.removeCallbacks(otpExpireRunnable);
        try {
            confirmationAlert.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.gc();
        new MemoryCache().clear();
    }

    private void initIds() {
        code = findViewById(R.id.code);
        timerTV = findViewById(R.id.timer);
        proceed = findViewById(R.id.numProceedButton);
        resend = findViewById(R.id.resend);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onProceedClick(View view) {
        UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
        isOkay = true;
        final String code = getDataFromEditext(this.code, "Invalid Code", 6);
        if (isOkay) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                verifyVerificationCode(codeOtp);
            }, 0);
        } else
            UtilityFunctions.orangeSnackBar(this, "Invalid Code!", Snackbar.LENGTH_SHORT);
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

    public void onResendClick(View view) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
            if (resendToken != null) {
                startAnim();
                isResend = true;
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        Constants.countryCode + phoneNumber.substring(1),
                        Constants.timeoutOtp,
                        TimeUnit.SECONDS,
                        context,
                        mCallbacks, resendToken);
            } else {
                UtilityFunctions.orangeSnackBar(context, "Unable to send OTP to your number!", Snackbar.LENGTH_SHORT);
            }
        }, 0);
    }

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
    //======================================== Show Loading bar ==============================================

    public void stopAnim() {
        anim.end();
        load.setVisibility(View.GONE);
        bg_main.setAlpha((float) 1);
        isLoad = false;
    }

    private void changeActiveButtons(Boolean proceedB, Boolean resendB, int proceedColor, int resendColor) {
        proceed.setEnabled(proceedB);
        resend.setEnabled(resendB);
        proceed.setBackgroundTintList(ContextCompat.getColorStateList(context, proceedColor));
        resend.setBackgroundTintList(ContextCompat.getColorStateList(context, resendColor));
    }

    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        startAnim();
        FirebaseConstants.firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    stopAnim();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information

                        UtilityFunctions.greenSnackBar(context, "Login Successfully!", Snackbar.LENGTH_SHORT);

                        if (Constants.USER_ROLE == Constants.deskUserRole) {
                            Constants.USER_ROLE = Constants.deskUserRole;
                            UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.USER_MOBILE, Constants.USER_Password, Constants.USER_DOCUMENT_ID, true);
                            UtilityFunctions.sendIntentClearPreviousActivity(context, new Intent(context, ScreenDeskUserHome.class),  Constants.changeIntentDelay);
                        } else if (Constants.USER_ROLE == Constants.managerRole) {
                            Constants.USER_ROLE = Constants.managerRole;
                            UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.USER_MOBILE, Constants.USER_Password, Constants.USER_DOCUMENT_ID, true);
                            UtilityFunctions.sendIntentClearPreviousActivity(context, new Intent(context, ScreenManagerHome.class),  Constants.changeIntentDelay);
                        } else {
                            finish();
                        }
                    } else {
                        Log.d("OTP", "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            code.setError("Wrong Code");
                            if (!isAnyDialogOpen) {
                                UtilityFunctions.alertNoteWithOkButton(context, "Otp Verification", "Wrong OTP! used wrong SMS verification code to verify the phone number", Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
                                isAnyDialogOpen = true;
                            }
                        } else {
                            if (!isAnyDialogOpen) {
                                UtilityFunctions.alertNoteWithOkButton(context, "Otp Verification", "Something Went Wrong", Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
                                isAnyDialogOpen = true;
                            }
                        }
                    }
                }).addOnFailureListener(this, e -> {
            stopAnim();
            if (!isAnyDialogOpen) {
                UtilityFunctions.alertNoteWithOkButton(context, "Otp Verification", e.getMessage(), Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
                isAnyDialogOpen = true;
            }
        });
    }

}
