package com.smartdesk.screens.admin.desk_user_status;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.databinding.ScreenUserDetailBinding;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.WorkaroundMapFragment;
import com.smartdesk.utility.memory.MemoryCache;

import java.sql.Timestamp;
import java.util.Date;

import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;
import static java.util.ResourceBundle.clearCache;

public class ScreenDeskUserDetail extends AppCompatActivity {

    ScreenUserDetailBinding binding;
    private Activity context;
    public static SignupUserDTO deskUserDetailsScreenDTO;


    private GoogleMap mMap;
    String userType = "Desk-User";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        actionBar(userType + " Detail");
        initLoadingBarItems();
        initIds();

        if (mMap == null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(googleMap -> {
                    mMap = googleMap;
                    MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.google_style);
                    mMap.setMapStyle(mapStyleOptions);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getUiSettings().setZoomControlsEnabled(true);

                    final ScrollView mScrollView = findViewById(R.id.scrollView); //parent scrollview in xml, give your scrollview id value
                    ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                            .setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));

                    BitmapDescriptor icon = UtilityFunctions.getBitmapFromVector(context, R.drawable.icon_users,
                            ContextCompat.getColor(context, R.color.SmartDesk_Editext_red));
                    LatLng latLngMechanic = new LatLng(deskUserDetailsScreenDTO.getWorkerLat(), deskUserDetailsScreenDTO.getWorkerLng());
                    mMap.addMarker(new MarkerOptions().icon(icon).position(latLngMechanic).title(deskUserDetailsScreenDTO.getWorkerName()));
                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLngMechanic, Constants.cameraZoomInMap);
                    mMap.animateCamera(location);

                    icon = UtilityFunctions.getBitmapFromVector(context, R.drawable.icon_garage,
                            ContextCompat.getColor(context, R.color.SmartDesk_Editext_red));
                });
            }, 0);
        }
    }

    private void initIds() {
        if (deskUserDetailsScreenDTO.getUserStatus().equalsIgnoreCase(Constants.activeStatus)) {
            ((LinearLayout) findViewById(R.id.llStatuslist)).setWeightSum(8);
            findViewById(R.id.approved).setVisibility(View.GONE);
        } else if (deskUserDetailsScreenDTO.getUserStatus().equalsIgnoreCase(Constants.blockedStatus)) {
            ((LinearLayout) findViewById(R.id.llStatuslist)).setWeightSum(8);
            findViewById(R.id.blocked).setVisibility(View.GONE);

            findViewById(R.id.bgStatus).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.SmartDesk_Orange));
            binding.statusImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_block));
            binding.statusText.setText("Account is blocked");
        } else {
            findViewById(R.id.bgStatus).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.SmartDesk_Orange));
            binding.statusImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_time_simple));
            binding.statusText.setText("Account is in Review");
        }

        picassoGetCircleImage(context, deskUserDetailsScreenDTO.getProfilePicture(), binding.profilePic, binding.profileShimmer, R.drawable.side_profile_icon);

        binding.gender.setText(deskUserDetailsScreenDTO.getWorkerGender());

        binding.name.setText(deskUserDetailsScreenDTO.getWorkerName());
        binding.registrationDate.setText("Reg date: " + UtilityFunctions.getDateFormat(deskUserDetailsScreenDTO.getRegistrationDate()));
        binding.phoneNumber.setText(UtilityFunctions.getPhoneNumberInFormat(deskUserDetailsScreenDTO.getWorkerPhone()));
        Date registrationDate = deskUserDetailsScreenDTO.getRegistrationDate();
        String time = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
        binding.timeAgo.setText(time);
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

    public void Approve(View view) {
        setMechanicStatus("Are you sure, you want to approve the " + userType + "?", R.color.whatsapp_green_dark);
    }

    public void block(View view) {
        setMechanicStatus("Are you sure, you want to block the " + userType + "?", R.color.SmartDesk_Orange);
    }

    public void delete(View view) {
        setMechanicStatus("Are you sure, you want to delete the " + userType + "?", R.color.red);
    }

    public void setMechanicStatus(String text, int color) {
        try {
            AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
            ((TextView) dialogView.findViewById(R.id.noteTitle)).setText(userType + " Status");
            dialogView.findViewById(R.id.llTITLECOLOR).setBackgroundTintList(ContextCompat.getColorStateList(context, color));
            dialogView.findViewById(R.id.yesbtn).setBackgroundTintList(ContextCompat.getColorStateList(context, color));
            ((TextView) dialogView.findViewById(R.id.noteText)).setText(text);
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                startAnim();
                if (color == R.color.red) {
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(deskUserDetailsScreenDTO.getLocalDocuementID())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                stopAnim();

                                UtilityFunctions.sendFCMMessage(context, new Data(deskUserDetailsScreenDTO.getLocalDocuementID(), new Timestamp(new Date().getTime()).getTime(), "deletion", "SmartDesk account deleted", deskUserDetailsScreenDTO.getWorkerName() + " your account has been deleted"));
                                UtilityFunctions.deleteUserCompletelu(deskUserDetailsScreenDTO.getLocalDocuementID());

                                UtilityFunctions.greenSnackBar(context, userType + " Deleted Successfully!", Snackbar.LENGTH_LONG);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                            }).addOnFailureListener(e -> {
                        stopAnim();
                        UtilityFunctions.redSnackBar(context, userType + " Deletion Unsuccessfully!", Snackbar.LENGTH_LONG);
                    });
                } else if (color == R.color.SmartDesk_Orange) {
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(deskUserDetailsScreenDTO.getLocalDocuementID())
                            .update("userStatus", Constants.blockedStatus)
                            .addOnSuccessListener(aVoid -> {
                                stopAnim();

                                UtilityFunctions.sendFCMMessage(context, new Data(deskUserDetailsScreenDTO.getLocalDocuementID(), new Timestamp(new Date().getTime()).getTime(), "blocked", "SmartDesk account blocked", deskUserDetailsScreenDTO.getWorkerName() + " your account has been blocked"));
                                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.deskUserRole, deskUserDetailsScreenDTO.getLocalDocuementID(), new Timestamp(new Date().getTime()), "Account blocked", "your account has been blocked", false));

                                UtilityFunctions.greenSnackBar(context, userType + " Blocked Successfully!", Snackbar.LENGTH_LONG);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                            }).addOnFailureListener(e -> {
                        stopAnim();
                        UtilityFunctions.redSnackBar(context, userType + " Blocked Unsuccessfully!", Snackbar.LENGTH_LONG);
                    });
                } else if (color == R.color.whatsapp_green_dark) {
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(deskUserDetailsScreenDTO.getLocalDocuementID())
                            .update("userStatus", Constants.activeStatus)
                            .addOnSuccessListener(aVoid -> {
                                stopAnim();

                                UtilityFunctions.sendFCMMessage(context, new Data(deskUserDetailsScreenDTO.getLocalDocuementID(), new Timestamp(new Date().getTime()).getTime(), "Approved", " SmartDesk account approved", deskUserDetailsScreenDTO.getWorkerName() + " your account has been approved"));
                                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.deskUserRole, deskUserDetailsScreenDTO.getLocalDocuementID(), new Timestamp(new Date().getTime()), "Account approved", "your account has been approved", false));

                                UtilityFunctions.greenSnackBar(context, userType + " Approved Successfully!", Snackbar.LENGTH_LONG);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                            }).addOnFailureListener(e -> {
                        stopAnim();
                        UtilityFunctions.redSnackBar(context, userType + " Approved Unsuccessfully!", Snackbar.LENGTH_LONG);
                    });
                }
            });

            dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> confirmationAlert.dismiss());
            confirmationAlert.setView(dialogView);
            confirmationAlert.setCancelable(false);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void viewProfilePic(View view) {
        showImageOn(deskUserDetailsScreenDTO.getProfilePicture(), "Profile");
    }
    //======================================== Show Loading bar ==============================================

    public void showImageOn(String alertDialogImageURL, String name) {
        try {
            clearCache();
            final AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_big_image, null);
            ImageView img = dialogView.findViewById(R.id.imagePreview);
            TextView nameText = dialogView.findViewById(R.id.name);
            nameText.setText(name);
            ShimmerFrameLayout shimmer = dialogView.findViewById(R.id.shimmer);
            UtilityFunctions.noRoundImageCorner(this, img, alertDialogImageURL, shimmer);
            dialogView.findViewById(R.id.retake).setVisibility(View.GONE);
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

    public void moveToUserLocation(View view) {
        LatLng latLng = new LatLng(deskUserDetailsScreenDTO.getWorkerLat(), deskUserDetailsScreenDTO.getWorkerLng());
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                latLng, Constants.cameraZoomInMap);
        mMap.animateCamera(location);
    }
}