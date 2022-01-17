package com.smartdesk.screens.user_management.login;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.databinding.ScreenLoginBinding;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.screens.admin._home.ScreenAdminHome;
import com.smartdesk.screens.desk_users_screens.sign_up.ScreenDeskUserSignup;
import com.smartdesk.screens.manager_screens.sign_up.ScreenMangerSignup;
import com.smartdesk.screens.user_management.forget_password.ScreenForgetPasswordStep1;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.encryption.EncryptionDecryption;
import com.smartdesk.utility.memory.MemoryCache;

import java.util.List;

public class ScreenLogin extends AppCompatActivity {

    private Activity context;
    private ScreenLoginBinding binding;
    //======================================== Show Loading bar ==============================================
    private LinearLayout load, bg_main;
    private ObjectAnimator anim;
    private ImageView progressBar;
    private Boolean isLoad;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initLoadingBarItems();
        UtilityFunctions.setupUI(findViewById(R.id.bg_main), this);
    }

    private void init() {
        binding = ScreenLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
    }

    public void forgotPassword(View view) {
        UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenForgetPasswordStep1.class), false, 0);
    }

    public void signUP(View view) {
        try {
            UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
            Constants.const_usersSignupDTO = null;
//            Constants.const_ConsumerSignupDTO = null;
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomDialogTheme);
            View bottomView = LayoutInflater.from(this).inflate(R.layout.alert_bottom_registration, findViewById(R.id.bottom_view));

            bottomView.findViewById(R.id.WorkerRegistration).setOnClickListener(v -> UtilityFunctions.sendIntentNormal(context, new Intent(ScreenLogin.this, ScreenDeskUserSignup.class), false, 0));
            bottomView.findViewById(R.id.MangerRegistration).setOnClickListener(v -> UtilityFunctions.sendIntentNormal(context, new Intent(ScreenLogin.this, ScreenMangerSignup.class), false, 0));
            bottomSheetDialog.setContentView(bottomView);
            bottomSheetDialog.show();
        } catch (Exception ex) {

        }
    }

    public void login(View view) {
        UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.bg_main), context);
        Boolean isOkay = true;
        String mobile = "", pass = "";
        try {
            mobile = UtilityFunctions.getStringFromMaskWithLengthLimit(binding.phone, 12);
            if (!UtilityFunctions.isValidPhone(mobile)) {
                binding.phone.setError("invalid number");
                isOkay = false;
            }

            if (!mobile.startsWith("07")) {
                binding.phone.setError("Number should start from 07");
                isOkay = false;
            }
        } catch (NullPointerException ex) {
            binding.phone.setError("invalid number");
            isOkay = false;
        }

        try {
            pass = UtilityFunctions.getStringFromEditTextWithLengthLimit(binding.password, 6);
        } catch (NullPointerException ex) {
            binding.password.setError("Invalid Password");
            isOkay = false;
        }

        if (pass.length() < 6) {
            binding.password.setError("Min Length should be 6");
            isOkay = false;
        } else {
            System.out.println("Valid");
        }

        if (isOkay) {
            try {
                mobile = mobile.replaceAll("-", "");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            final String finalMobile = mobile;
            final String finalPass = pass;
            new Handler(Looper.getMainLooper()).postDelayed(() -> new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
                startAnim();
                String encryptedMobile = EncryptionDecryption.encryptionNormalText(finalMobile);
                String passAAAA = EncryptionDecryption.encryptionNormalText("asd123");
                System.out.println(encryptedMobile + "HELLO");

                //                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection)
//                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<SignupUserDTO> signupUserDTO = queryDocumentSnapshots.toObjects(SignupUserDTO.class);
//
//                    for (DocumentSnapshot aas: queryDocumentSnapshots.getDocuments()) {
//                        SignupUserDTO aa = aas.toObject(SignupUserDTO.class);
//                        aa.setWorkerPassword(passAAAA);
//                        System.out.println( aas.getId());
//                            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(aas.getId()).set(aa);
//                    }
//                });

//                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(FirebaseConstants.adminDocumentID).get().addOnSuccessListener(documentSnapshot -> {
//                    SignupUserDTO aa = documentSnapshot.toObject(SignupUserDTO.class);
//                    aa.setWorkerPhone(encryptedMobile);
//                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(FirebaseConstants.adminDocumentID).set(aa);
//                });

                Query queryNumber = FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).whereEqualTo("workerPhone", encryptedMobile);
                queryNumber.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    new Thread(() -> {
                        List<SignupUserDTO> signupUserDTO = queryDocumentSnapshots.toObjects(SignupUserDTO.class);
                        if (signupUserDTO.isEmpty()) {
                            stopAnimOnUIThread();
                            UtilityFunctions.orangeSnackBar(context, "Phone Number is not Registered!", Snackbar.LENGTH_LONG);
                        } else {
                            String decryptPassword = "";
                            try {
                                signupUserDTO.get(0).setWorkerPhone(finalMobile);
                                decryptPassword = EncryptionDecryption.decryptionCypherText(signupUserDTO.get(0).getWorkerPassword());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            stopAnimOnUIThread();
                            if (decryptPassword.equals(finalPass)) {
                                UtilityFunctions.scheduleJob(this, true);
                                if (signupUserDTO.get(0).getUserStatus().equals(Constants.activeStatus)) {
                                    String documentID = "";
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        documentID = document.getId();
                                        break;
                                    }
                                    Constants.USER_DOCUMENT_ID = documentID;
                                    Constants.USER_NAME = signupUserDTO.get(0).getWorkerName();
                                    Constants.USER_PROFILE = signupUserDTO.get(0).getProfilePicture();
                                    Constants.USER_MOBILE = signupUserDTO.get(0).getWorkerPhone();
                                    Constants.USER_Password = finalPass;

                                    if (signupUserDTO.get(0).getRole() == Constants.adminRole) {
                                        Constants.USER_ROLE = Constants.adminRole;
                                        UtilityFunctions.saveLoginCredentialsInSharedPreference(context, Constants.USER_MOBILE, Constants.USER_Password, Constants.USER_DOCUMENT_ID, true);
                                        UtilityFunctions.sendIntentNormal(context, new Intent(context, ScreenAdminHome.class), true, Constants.changeIntentDelay);
                                    } else if (signupUserDTO.get(0).getRole() == Constants.deskUserRole) {
                                        Constants.USER_ROLE = Constants.deskUserRole;
                                        UtilityFunctions.sendIntentNormal(context, new Intent(context, LoginOTP.class), false, Constants.changeIntentDelay);
                                    } else if (signupUserDTO.get(0).getRole() == Constants.managerRole) {
                                        Constants.USER_ROLE = Constants.managerRole;
                                        UtilityFunctions.sendIntentNormal(context, new Intent(context, LoginOTP.class), false, Constants.changeIntentDelay);
                                    }
                                } else {
                                    UtilityFunctions.alertNoteWithOkButton(context, "Account Status", signupUserDTO.get(0).getUserStatus(), Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
                                }

                            } else {
                                UtilityFunctions.orangeSnackBar((Activity) context, "Incorrect Password!", Snackbar.LENGTH_SHORT);
                            }
                        }
                    }).start();
                }).addOnFailureListener(e -> {
                    stopAnim();
                    UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                });
            }, 0), 0);

        } else

            UtilityFunctions.orangeSnackBar(this, "Please Enter Credentials Properly!", Snackbar.LENGTH_SHORT);

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

    public void stopAnim() {
        anim.end();
        load.setVisibility(View.GONE);
        bg_main.setAlpha((float) 1);
        isLoad = false;
    }

    public void contactUs(View view) {
        UtilityFunctions.alertNoteWithOkButton(context, "Contact Us", "Please Contact us on " + Constants.emailID + " with your contact details and the issue you are facing", Gravity.CENTER, R.color.SmartDesk_Orange, R.color.black_color, false, false, null);
    }
    //======================================== Show Loading bar ==============================================

    public void stopAnimOnUIThread() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> stopAnim(), 0);
    }
}