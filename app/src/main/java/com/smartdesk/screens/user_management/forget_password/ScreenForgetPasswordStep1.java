package com.smartdesk.screens.user_management.forget_password;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.santalu.maskedittext.MaskEditText;

import java.util.List;


public class ScreenForgetPasswordStep1 extends AppCompatActivity {

    MaskEditText phone;
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
        setContentView(R.layout.screen_forget_password_number);
        context = this;
        initLoadingBarItems();
        UtilityFunctions.setupUI(findViewById(R.id.bg_main), this);
        phone = findViewById(R.id.phoneNumber);
    }

    public void onProceedClick(View view) {
        UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
        Boolean isOkay = true;
        String number = "";
        number = getDataFromMaskText(phone, "Invalid Number", 12);
        try {
            number = number.replaceAll("-", "");
        } catch (Exception ex) {
            isOkay = false;
            phone.setError("Invalid Number");
            ex.printStackTrace();
        }
        if (!number.startsWith("03")) {
            phone.setError("Number should start from 03");
            isOkay = false;
        }
        if (isOkay) {
            String finalNumber = number;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startAnim();
                String encryptedMobile = EncryptionDecryption.encryptionNormalText(finalNumber);
                Query queryNumber = FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).whereEqualTo("workerPhone", encryptedMobile );
                queryNumber.get().addOnCompleteListener(task -> {
                    new Thread(() -> {
                        System.out.println("Complete Listnere");
                        if(task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                stopAnimOnUIThread();
                                UtilityFunctions.orangeSnackBar(context, "Phone Number is not Registered!", Snackbar.LENGTH_LONG);
                            } else {
                                stopAnimOnUIThread();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Constants.USER_DOCUMENT_ID = document.getId();
                                    break;
                                }
                                List<SignupUserDTO> signupUserDTO = task.getResult().toObjects(SignupUserDTO.class);
                                signupUserDTO.get(0).setWorkerPhone(finalNumber);
                                Constants.const_usersSignupDTO = signupUserDTO.get(0);
                                Constants.USER_MOBILE = finalNumber;
                                UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenForgetPasswordStep2.class), true, 0);
                            }
                        }else
                            UtilityFunctions.redSnackBar( context, "No Internet!", Snackbar.LENGTH_SHORT);
                    }).start();
                }).addOnFailureListener(e -> {
                    stopAnim();
                    UtilityFunctions.redSnackBar( context, "No Internet!", Snackbar.LENGTH_SHORT);
                });
            }, 0);
        }
    }

    private String getDataFromMaskText(MaskEditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromMaskWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
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

    public void stopAnimOnUIThread() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> stopAnim(), 0);
    }
    //======================================== Show Loading bar ==============================================
}