package com.smartdesk.screens.user_management.setting;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.constants.PermisionCode;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.screens.user_management.change_password.ScreenChangePassword;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.CustomEditext;
import com.smartdesk.utility.memory.MemoryCache;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.santalu.maskedittext.MaskEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.smartdesk.utility.UtilityFunctions.noRoundImageCorner;
import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class ScreenAdminSetting extends AppCompatActivity implements TextView.OnEditorActionListener {

    private Activity context;
    MemoryCache memoryCache = new MemoryCache();

    private TextView title;
    private TextView name, mobileNumber, gender;
    private CircleImageView profilePic;

    private String isProfileUrl = "";
    private int cameraReult;

    private DatePickerDialog mDateListener;
    private boolean isOkay;
    private Spinner genderSpinner;
    private boolean isGenderSelected = false;

    private SignupUserDTO const_SettingData;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_admin_setting);
        context = this;
        actionBar("Settings");
        initLoadingBarItems();
        initIds();
        UtilityFunctions.setupUI(findViewById(R.id.parent), context);
        clearCache();
        getData();
    }

    public void getData() {
        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    const_SettingData = documentSnapshot.toObject(SignupUserDTO.class);
                    setData();
                })
                .addOnFailureListener(e -> {
                    UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_LONG);
                });
    }

    public void clearCache() {
        try {
            memoryCache.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initIds() {
        title = findViewById(R.id.title);
        name = findViewById(R.id.name);
        mobileNumber = findViewById(R.id.phoneNumber);
        gender = findViewById(R.id.gender);

        profilePic = findViewById(R.id.profilePic);
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

    private String getDataFromMaskText(MaskEditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromMaskWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            isOkay = false;
        }
        return text;
    }

    private String getDataFromEditext(EditText editText, String errorMSG, int minimumLength, TextView error) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            error.setVisibility(View.VISIBLE);
            isOkay = false;
        }
        return text;
    }

    public void changeProfilePic(View view) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            cameraReult = 1;
            isCameraOpen = false;
            imageName = "Profile";
            clearCache();
            if (isProfileUrl == null || isProfileUrl.equals("")) {
                if (askForPermission()) {
                    if (!isCameraOpen)
                        openCamera(imageName);
                }
            } else {
                showImageOn(isProfileUrl, "Profile Image");
            }
        }, 0);
    }

    public void changePassword(View view) {
        UtilityFunctions.sendIntentNormal(context, new Intent(this, ScreenChangePassword.class), false, 0);
    }

    public void setData() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Constants.USER_NAME = const_SettingData.getWorkerName();
            Constants.USER_MOBILE = const_SettingData.getWorkerPhone();
            Constants.USER_PROFILE = const_SettingData.getProfilePicture();

            title.setText(const_SettingData.getWorkerName());
            name.setText(const_SettingData.getWorkerName());
            mobileNumber.setText(UtilityFunctions.getPhoneNumberInFormat(const_SettingData.getWorkerPhone()));
            gender.setText(const_SettingData.getWorkerGender());
            ((TextView) findViewById(R.id.email)).setText(const_SettingData.getWorkerEmail());

            try {
                isProfileUrl = const_SettingData.getProfilePicture();
                if (const_SettingData.getProfilePicture() != null)
                    UtilityFunctions.picassoGetCircleImage(context,const_SettingData.getProfilePicture(), profilePic, findViewById(R.id.profile_shimmer), R.drawable.side_profile_icon);
                else
                    UtilityFunctions.disableShimmer(findViewById(R.id.profile_shimmer));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            clearCache();
        }, 0);
    }

    public void updateProfilePictureAPI() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> startAnim(), 0);
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

    //Cninc Variables
    private File imageFile;
    private String imageName;
    private Boolean isProfilePic;
    private Boolean isCameraOpen = false;

    public boolean askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allTrue = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                }
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PermisionCode.MY_CAMERA_PERMISSION_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PermisionCode.MY_CAMERA_PERMISSION_CODE);
            }
            return allTrue;
        } else {
            return true;
        }
    }

    // When you send some Permisssion the result will be here
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermisionCode.MY_CAMERA_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (askForPermission()) {
                        isCameraOpen = true;
                        openCamera(imageName);
                    }
                } else {
                    boolean showRationale = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                            if (!showRationale)
                                dialogForNeverAskAgain("Prompt to the Application Permission Setting & Allow Camera");
                            else
                                Toast.makeText(this, "Permission Deny", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {

                        }
                    }
                }
                break;
            }
            case PermisionCode.MY_STORAGE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (askForPermission()) {
                        isCameraOpen = true;
                        openCamera(imageName);
                    }
                } else {
                    boolean showRationale = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            showRationale = shouldShowRequestPermissionRationale(permissions[0]);
                            if (!showRationale)
                                dialogForNeverAskAgain("Prompt to the Application Permission Setting & Allow Storage");
                            else
                                Toast.makeText(this, "Permission Deny", Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setPositiveButton("GO", (arg0, arg1) -> {
                arg0.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, PermisionCode.MY_STORAGE_PERMISSION_CODE);
            });
            alertDialogBuilder.setNegativeButton("Back", (arg0, arg1) -> arg0.dismiss());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } catch (Exception ex) {

        }
    }

    //Open Camera
    public void openCamera(String imageName) {
        File directory = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "");
        if (!directory.exists())
            directory.mkdir();
        imageFile = new File(directory, imageName + ".jpg");
        if (imageFile.exists()) {
            imageFile.delete();
            System.out.println("DDDDDDDDDDDDDDDELETEDDDDDDDDDDDDDD");
        }
        System.out.println(imageFile.getPath());
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, getString(R.string.fileProvider), imageFile));
            startActivityForResult(intent, PermisionCode.CAMERA_RESULT_CODE);
        } catch (Exception ex) {
            ex.printStackTrace();
            UtilityFunctions.orangeSnackBar(this, "error try again click photo", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermisionCode.CAMERA_RESULT_CODE && resultCode == (this).RESULT_OK) {
            try {
                File f = new File(imageFile.getPath());
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                File file = UtilityFunctions.BitmapToFile(ScreenAdminSetting.this, f, b);

                if (cameraReult == 1) {
                    Glide.with(this).load(b).apply(new RequestOptions().fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(new RoundedCorners(12))).into(profilePic);
                    UtilityFunctions.uploadImage((Activity) context, Uri.fromFile(file), imageName, const_SettingData.getWorkerPhone(), 2);
                }
            } catch (FileNotFoundException e) {
                UtilityFunctions.orangeSnackBar(ScreenAdminSetting.this, "Photo file can't be created, please try again", Snackbar.LENGTH_SHORT);
                e.printStackTrace();
            }
        }
    }

    public void showImageOn(String alertDialogImageURL, String name) {
        try {
            clearCache();
            final AlertDialog confirmationAlert = new AlertDialog.Builder(ScreenAdminSetting.this).create();
            final View dialogView = ScreenAdminSetting.this.getLayoutInflater().inflate(R.layout.alert_dialog_big_image, null);
            ImageView img = dialogView.findViewById(R.id.imagePreview);
            TextView nameText = dialogView.findViewById(R.id.name);
            nameText.setText(name);
            ShimmerFrameLayout shimmer = dialogView.findViewById(R.id.shimmer);
            UtilityFunctions.noRoundImageCorner(this, img, alertDialogImageURL, shimmer);
            ((Button) dialogView.findViewById(R.id.retake)).setText("Take New Profile Image");
            dialogView.findViewById(R.id.retake).setOnClickListener(v -> {
                if (askForPermission()) {
                    if (!isCameraOpen)
                        openCamera(imageName);
                }
                confirmationAlert.dismiss();
                confirmationAlert.cancel();
            });
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

    public void openProfilePic(View view) {
        cameraReult = 1;
        isCameraOpen = false;
        imageName = "Profile";
        if (isProfileUrl == null || isProfileUrl.equals("")) {
            UtilityFunctions.orangeSnackBar(this, "please Upload Image First", Snackbar.LENGTH_SHORT);
        } else {
            showImageOn(isProfileUrl, "Profile Image");
        }
    }

    public void editName(View view) {
        openBottomView("Change Name", R.drawable.side_profile_icon, InputType.TYPE_TEXT_VARIATION_PERSON_NAME, "Invalid Name", 3, "workerName", "Enter Your Name");
    }

    public void gender(View view) {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(Constants.genderSelection);
        spinnerArray.add("Male");
        spinnerArray.add("Female");
        openBottomViewSpinner("Change Gender", R.drawable.icon_gender, "Gender not Selected *", "workerGender", spinnerArray, Constants.genderSelection);
    }


    public void openBottomView(String title, int icon, int inputType, String errorMsg, int minimumLength, String fieldName, String hint) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomDialogTheme);
        bottomView = LayoutInflater.from(this).inflate(R.layout.alert_bottom_editext, findViewById(R.id.bottom_view));
        UtilityFunctions.setupUI(bottomView.findViewById(R.id.bottom_view), context);
        ((TextView) bottomView.findViewById(R.id.changeTitle)).setText(title);
        ((ImageView) bottomView.findViewById(R.id.iconImage)).setImageDrawable(ContextCompat.getDrawable(context, icon));
        CustomEditext customEditext = bottomView.findViewById(R.id.editext);
        customEditext.setInputType(inputType);
        customEditext.setHint(hint + " ⁕");
        bottomView.findViewById(R.id.save).setOnClickListener(v -> {
            isOkay = true;
            (bottomView.findViewById(R.id.spinnerError)).setVisibility(View.GONE);
            ((TextView) bottomView.findViewById(R.id.spinnerError)).setText(errorMsg);

            UtilityFunctions.removeFocusFromEditexts(bottomView.findViewById(R.id.parent), context);
            String data = getDataFromEditext(customEditext, errorMsg, minimumLength, bottomView.findViewById(R.id.spinnerError));
            if (!UtilityFunctions.isValidName(data)) {
                isOkay = false;
                (bottomView.findViewById(R.id.spinnerError)).setVisibility(View.VISIBLE);
            }
            if (isOkay) {
                startAnim();
                bottomSheetDialog.dismiss();
                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID).
                        update(fieldName, data)
                        .addOnSuccessListener(aVoid -> {
                            stopAnim();
                            getData();
                        })
                        .addOnFailureListener(e -> {
                            stopAnim();
                        });
            }
        });
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.show();
    }

    public void openBottomViewSpinner(String title, int icon, String errorMsg, String fieldName, List<String> list, String defaultText) {
        isSpinnerSelected = false;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomDialogTheme);
        bottomView = LayoutInflater.from(this).inflate(R.layout.alert_bottom_spinner, findViewById(R.id.bottom_view));
        UtilityFunctions.setupUI(bottomView.findViewById(R.id.bottom_view), context);
        ((ImageView) bottomView.findViewById(R.id.iconImage)).setImageDrawable(ContextCompat.getDrawable(context, icon));
        ((TextView) bottomView.findViewById(R.id.changeTitle)).setText(title);
        Spinner spinner = bottomView.findViewById(R.id.editext);
        TextView spinnerError = bottomView.findViewById(R.id.spinnerError);
        spinnerError.setText(errorMsg);
        setSpinner(list, spinner, spinnerError, defaultText);
        bottomView.findViewById(R.id.save).setOnClickListener(v -> {
            isOkay = true;

            if (!isSpinnerSelected) {
                isOkay = false;
                spinnerError.setVisibility(View.VISIBLE);
            }

            if (isOkay) {
                startAnim();
                bottomSheetDialog.dismiss();
                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                        .update(fieldName, spinnerItemName)
                        .addOnSuccessListener(aVoid -> {
                            stopAnim();
                            getData();
                        })
                        .addOnFailureListener(e -> {
                            stopAnim();
                        });
            }
        });
        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.show();
    }

    Boolean isSpinnerSelected = false;
    String spinnerItemName;

    public void setSpinner(List<String> list, Spinner spinner, TextView spinnerError, String defaultText) {
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, list);
            spinner.setAdapter(adapter);
            spinner.post(() -> {
                int height = spinner.getHeight();
                spinner.setDropDownVerticalOffset(height);
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(defaultText)) {
                        isSpinnerSelected = false;
                    } else {
                        spinnerError.setVisibility(View.GONE);
                        spinnerItemName = parent.getItemAtPosition(position).toString();
                        isSpinnerSelected = true;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    View bottomView;

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (bottomView != null)
                UtilityFunctions.removeFocusFromEditexts(bottomView.findViewById(R.id.parent), context);
            else
                UtilityFunctions.removeFocusFromEditexts(findViewById(R.id.parent), context);
        }
        return false;
    }
}