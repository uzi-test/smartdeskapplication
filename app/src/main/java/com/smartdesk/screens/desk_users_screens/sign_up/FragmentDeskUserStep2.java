package com.smartdesk.screens.desk_users_screens.sign_up;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.PermisionCode;
import com.smartdesk.utility.UtilityFunctions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.smartdesk.utility.UtilityFunctions.noRoundImageCorner;

public class FragmentDeskUserStep2 extends Fragment implements View.OnClickListener {

    private View view;

    private File imageFile;
    private ImageView take;
    private Boolean isCameraOpen = false;
    private String imageName;
    private Bitmap profileBitmap;

    private Context signupContext;

    public FragmentDeskUserStep2() {
    }

    public FragmentDeskUserStep2(Context mContext) {
        signupContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_desk_user_step2, container, false);
        take = view.findViewById(R.id.take);
        Constants.const_usersSignupDTO.setProfilePicture(null);
        System.out.println("CREATEEEEEEEEEEEEEEEEEEEEED AGAIN");
        take.setOnClickListener(this);
        return view;
    }

    public void validationsAndGetValues() {
        ScreenDeskUserSignup.isOkayStep2 = true;
        if (Constants.const_usersSignupDTO.getProfilePicture() == null) {
            ScreenDeskUserSignup.isOkayStep2 = false;
            System.out.println("NOT GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGOING PROFILE");
            UtilityFunctions.orangeSnackBar((Activity) signupContext, "Please upload your Profile Picture!", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onClick(final View v) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            switch (v.getId()) {
                case R.id.take:
                    imageName = "Profile";
                    isCameraOpen = false;
                    if (Constants.const_usersSignupDTO.getWorkerPhone() == null) {
                        UtilityFunctions.orangeSnackBar((Activity) signupContext, "Please complete step 1 first", Snackbar.LENGTH_SHORT);
                        return;
                    }
                    if (Constants.const_usersSignupDTO.getProfilePicture() == null) {
                        if (askForPermission()) {
                            if (!isCameraOpen)
                                openCamera(imageName);
                        }
                    } else
                        showImageOn(Constants.const_usersSignupDTO.getProfilePicture(), "Profile Image");
                    break;
            }
        }, 0);
    }

    public boolean askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allTrue = true;
            if (ContextCompat.checkSelfPermission(signupContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) signupContext, Manifest.permission.READ_EXTERNAL_STORAGE))
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
            } else if (ContextCompat.checkSelfPermission(signupContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) signupContext, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermisionCode.MY_STORAGE_PERMISSION_CODE);
            } else if (ContextCompat.checkSelfPermission(signupContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                allTrue = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) signupContext, Manifest.permission.CAMERA))
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PermisionCode.MY_CAMERA_PERMISSION_CODE);
                else
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PermisionCode.MY_CAMERA_PERMISSION_CODE);
            }
            return allTrue;
        } else {
            return true;
        }
    }


    public void openCamera(String imageName) {
        File directory = new File(signupContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "");
//        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Android/data/"+signupContext.getPackageName() +"/files/pictures");
        if (!directory.exists())
            directory.mkdir();
        imageFile = new File(directory, imageName + ".jpg");
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(signupContext, getString(R.string.fileProvider), imageFile));
            startActivityForResult(intent, PermisionCode.CAMERA_RESULT_CODE);
        } catch (Exception ex) {
            ex.printStackTrace();
            UtilityFunctions.orangeSnackBar((Activity) signupContext, "error try again click photo", Snackbar.LENGTH_SHORT);
        }
    }

    //Permission Results
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
                                Toast.makeText(signupContext, "Permission Deny", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(signupContext, "Permission Deny", Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(signupContext);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage(msg);
            alertDialogBuilder.setPositiveButton("GO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", signupContext.getPackageName(), null);
                    intent.setData(uri);
                    FragmentDeskUserStep2.this.startActivityForResult(intent, PermisionCode.MY_STORAGE_PERMISSION_CODE);
                }
            });
            alertDialogBuilder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } catch (Exception ex) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermisionCode.CAMERA_RESULT_CODE && resultCode == ((Activity) signupContext).RESULT_OK) {
            try {
                File f = new File(imageFile.getPath());
                profileBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                Glide.with((Activity) signupContext).load(profileBitmap).apply(new RequestOptions().fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new RoundedCorners(22))).into(take);
                File file = UtilityFunctions.BitmapToFile((Activity) signupContext, f, profileBitmap);
                UtilityFunctions.uploadImage((Activity) signupContext, Uri.fromFile(file), imageName, Constants.const_usersSignupDTO.getWorkerPhone(), 2);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                UtilityFunctions.orangeSnackBar((Activity) signupContext, "Photo file can't be created, please try again", Snackbar.LENGTH_SHORT);
            }
        }
    }

    public void showImageOn(String alertDialogImageURL, String name) {
        try {
            final AlertDialog confirmationAlert = new AlertDialog.Builder(signupContext).create();
            final View dialogView = ((Activity) signupContext).getLayoutInflater().inflate(R.layout.alert_dialog_big_image, null);
            ImageView img = dialogView.findViewById(R.id.imagePreview);
            TextView nameText = dialogView.findViewById(R.id.name);
            nameText.setText(name);
            ShimmerFrameLayout shimmer = dialogView.findViewById(R.id.shimmer);
            UtilityFunctions.noRoundImageCorner(signupContext, img, alertDialogImageURL, shimmer);
            dialogView.findViewById(R.id.retake).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (askForPermission()) {
                        if (!isCameraOpen)
                            openCamera(imageName);
                    }
                    confirmationAlert.dismiss();
                    confirmationAlert.cancel();
                }
            });
            dialogView.findViewById(R.id.hideDialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmationAlert.dismiss();
                    confirmationAlert.cancel();
                }
            });

            confirmationAlert.setView(dialogView);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (Exception ex) {

        }
    }
}
