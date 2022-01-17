package com.smartdesk.screens.user_management.signup_otp;

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

import com.google.firebase.auth.PhoneAuthOptions;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.utility.library.CustomEditext;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.screens.user_management.login.ScreenLogin;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ScreenSignUpOTP extends AppCompatActivity {

    Boolean isAnyDialogOpen = false;
    Boolean isResend = false;
    private Activity context;
    private AlertDialog confirmationAlert;

    private CustomEditext code;
    private Button proceed, resend;
    private Boolean isOkay;

    private String phoneNumber;
    private String codeOtp;
    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken resendToken;

    private long otpExpire = Constants.timeoutOtp * 1000;

    private Handler handlerOtpExpire = new Handler();
    private Runnable otpExpireRunnable;
    private TextView timerTV;

    private int countResend = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_signup_otp);
        context = this;
        initIds();
        listenerOTPCode();
        initLoadingBarItems();
        UtilityFunctions.setupUI(findViewById(R.id.bg_main), context);
        if (Constants.const_usersSignupDTO != null)
            phoneNumber = Constants.const_usersSignupDTO.getWorkerPhone();
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
                    UtilityFunctions.orangeSnackBar(context, "Please Register Again", Snackbar.LENGTH_LONG);
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
            confirmationAlert = new AlertDialog.Builder(ScreenSignUpOTP.this).create();
            final View dialogView = ScreenSignUpOTP.this.getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
            ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Exit Registration");
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UtilityFunctions.sendIntentClearPreviousActivity(context, new Intent(context, ScreenLogin.class), 0);
                }
            });

            dialogView.findViewById(R.id.nobtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmationAlert.dismiss();
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
                        Log.d("OTP", "signInWithCredential:success");
                        FirebaseUser user = task.getResult().getUser();
                        startAnim();
                        if (Constants.const_usersSignupDTO != null) {
                            Constants.const_usersSignupDTO.setUuID(user.getUid());
                            Constants.const_usersSignupDTO.setRegistrationDate(new Timestamp(new Date().getTime()));
                            Constants.const_usersSignupDTO.setUserStatus(Constants.newAccountStatus);
                            Constants.const_usersSignupDTO.setWorkerPhone(EncryptionDecryption.encryptionNormalText(Constants.const_usersSignupDTO.getWorkerPhone()));
                            Constants.const_usersSignupDTO.setWorkerPassword(EncryptionDecryption.encryptionNormalText(Constants.const_usersSignupDTO.getWorkerPassword()));
                            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).add(Constants.const_usersSignupDTO).addOnCompleteListener(task1 -> {
                                stopAnim();

                                UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.const_usersSignupDTO.getWorkerPhone(), Constants.const_usersSignupDTO.getWorkerPassword(), task1.getResult().getId(), false);

                                UtilityFunctions.sendFCMMessage(context, new Data(FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()).getTime(), "registration", Constants.const_usersSignupDTO.getWorkerName() + " Verification", "worker registration request for verification"));
                                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.adminRole, FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()), Constants.const_usersSignupDTO.getWorkerName() + " Verification", "worker registration request for verification", false));

                                UtilityFunctions.alertNoteWithOkButton(context, "Account Registered", "Your account registered successfully", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, new Intent(context, ScreenLogin.class));
                            }).addOnFailureListener(e -> {
                                stopAnim();
                                UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                            });
                        }
//                        else if (Constants.const_ConsumerSignupDTO != null) {
//                            Constants.const_ConsumerSignupDTO.setUuID(user.getUid());
//                            Constants.const_ConsumerSignupDTO.setRegistrationDate(new Timestamp(new Date().getTime()));
//                            Constants.const_ConsumerSignupDTO.setUserStatus(Constants.newAccountStatus);
//                            Constants.const_ConsumerSignupDTO.setWorkerPassword(EncryptPassword.passwordEncryption(Constants.const_ConsumerSignupDTO.getWorkerPassword()));
//                            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).add(Constants.const_ConsumerSignupDTO).addOnCompleteListener(task1 -> {
//                                stopAnim();
//
//                                UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.const_ConsumerSignupDTO.getWorkerPhone(), Constants.const_ConsumerSignupDTO.getWorkerPassword(), task1.getResult().getId(), false);
//                                UtilityFunctions.sendFCMMessage(context, new Data(FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()).getTime(), "registration", Constants.const_ConsumerSignupDTO.getWorkerName() + " Verification", "worker registration request for verification"));
//                                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.adminRole, FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()), Constants.const_ConsumerSignupDTO.getWorkerName() + " Verification", "worker registration request for verification", false));
//
//                                UtilityFunctions.alertNoteWithOkButton(context, "Account Registered", "Your account registered successfully", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, new Intent(context, ScreenLogin.class));
//                            }).addOnFailureListener(e -> {
//                                stopAnim();
//                                UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
//                            });
//                        }
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
