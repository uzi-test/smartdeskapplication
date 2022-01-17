package com.smartdesk.screens.desk_users_screens._home.desk_user;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
import com.smartdesk.databinding.ScreenSmartDeskDetailUserBinding;
import com.smartdesk.model.SmartDesk.NewDesk;
import com.smartdesk.model.SmartDesk.UserBookDate;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.WorkaroundMapFragment;
import com.smartdesk.utility.memory.MemoryCache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScreenSmartDeskDetailUser extends AppCompatActivity {

    ScreenSmartDeskDetailUserBinding binding;
    private Activity context;
    public static NewDesk deskUserDetailsScreenDTO;

    String docId = "";
    String bookDate = "02-12-2021";

    private GoogleMap mMap;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenSmartDeskDetailUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        actionBar("Smart Desk Detail");
        initIds();

        try {
            bookDate = getIntent().getStringExtra("date");
        } catch (Exception ex) {
        }

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

                    try {
                        BitmapDescriptor icon = UtilityFunctions.getBitmapFromVector(context, R.drawable.z_desk_loading,
                                ContextCompat.getColor(context, R.color.colorPrimary));
                        LatLng latLngMechanic = new LatLng(deskUserDetailsScreenDTO.getDeskLat(), deskUserDetailsScreenDTO.getDeskLng());
                        mMap.addMarker(new MarkerOptions().icon(icon).position(latLngMechanic).title(deskUserDetailsScreenDTO.getName()));
                        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLngMechanic, Constants.cameraZoomInMap);
                        mMap.animateCamera(location);

                        icon = UtilityFunctions.getBitmapFromVector(context, R.drawable.icon_garage,
                                ContextCompat.getColor(context, R.color.SmartDesk_Editext_red));
                    } catch (Exception eex) {
                    }

                });
            }, 0);
        }
    }

    private void initIds() {
        List<UserBookDate> book = deskUserDetailsScreenDTO.getBookDate();
        if (book != null && book.size() > 0) {
            findViewById(R.id.bgStatus).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.whatsapp_green_dark));
            binding.statusImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_approve_24));
            binding.statusText.setText("Desk book by users");
        } else {
            findViewById(R.id.bgStatus).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.SmartDesk_Orange));
            binding.statusImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_block));
            binding.statusText.setText("Desk not yet book by anyone");
        }

        binding.deskID.setText(deskUserDetailsScreenDTO.getId());
        binding.name.setText(deskUserDetailsScreenDTO.getName());
        binding.registrationDate.setText("Reg. date: " + UtilityFunctions.getDateFormat(deskUserDetailsScreenDTO.getRegistrationDate()));

        Date registrationDate = deskUserDetailsScreenDTO.getRegistrationDate();
        String time = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
        binding.timeAgo.setText(time);


        binding.wirelessChargingText.setText(deskUserDetailsScreenDTO.wirelessCharging);
        binding.bluetoothConnectionText.setText(deskUserDetailsScreenDTO.bluetoothConnection);
        binding.builtinSpeakerText.setText(deskUserDetailsScreenDTO.builtinSpeaker);
        binding.groupUserText.setText(deskUserDetailsScreenDTO.groupUser);
        docId = UtilityFunctions.getDocumentID(context);

        binding.recyclerViewCard.setVisibility(View.GONE);
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
    private ObjectAnimator anim;
    private Boolean isLoad;

    public void startAnim() {
        isLoad = true;
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.bgMain.setAlpha((float) 0.2);
        anim = UtilityFunctions.loadingAnim(this, binding.loadingImage);
        binding.loadingView.setOnTouchListener((v, event) -> isLoad);
    }

    public void stopAnim() {
        anim.end();
        binding.loadingView.setVisibility(View.GONE);
        binding.bgMain.setAlpha((float) 1);
        isLoad = false;
    }

    public void delete(View view) {
        setDeskStatus("Are you sure, you want to delete the Desk?", R.color.red);
    }

    public void setDeskStatus(String text, int color) {
        try {
            AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
            ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Smart Desk Status");
            dialogView.findViewById(R.id.llTITLECOLOR).setBackgroundTintList(ContextCompat.getColorStateList(context, color));
            dialogView.findViewById(R.id.yesbtn).setBackgroundTintList(ContextCompat.getColorStateList(context, color));
            ((TextView) dialogView.findViewById(R.id.noteText)).setText(text);
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();
                startAnim();
                if (color == R.color.red) {
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection).document(deskUserDetailsScreenDTO.getDocID())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                stopAnim();

                                UtilityFunctions.sendFCMMessage(context, new Data(docId, new Timestamp(new Date().getTime()).getTime(), "deletion", "SmartDesk deleted", deskUserDetailsScreenDTO.getName() + " your desk has been deleted"));
                                UtilityFunctions.deleteUserCompletelu(docId);

                                UtilityFunctions.greenSnackBar(context, "Smart Desk Deleted Successfully!", Snackbar.LENGTH_LONG);
                                new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1000);
                            }).addOnFailureListener(e -> {
                        stopAnim();
                        UtilityFunctions.redSnackBar(context, "Smart Desk Deletion Unsuccessfully!", Snackbar.LENGTH_LONG);
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
    //======================================== Show Loading bar ==============================================

    public void moveToMechanicLocation(View view) {
        try {
            LatLng latLng = new LatLng(deskUserDetailsScreenDTO.getDeskLat(), deskUserDetailsScreenDTO.getDeskLng());
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                    latLng, Constants.cameraZoomInMap);
            mMap.animateCamera(location);
        } catch (Exception ex) {
        }
    }


    public void Book(View view) {
        try {
            AlertDialog confirmationAlert = new AlertDialog.Builder(context).create();
            final View dialogView = getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
            ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("Smart Desk Booking");
            dialogView.findViewById(R.id.llTITLECOLOR).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.SmartDesk_Blue));
            dialogView.findViewById(R.id.yesbtn).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.SmartDesk_Blue));
            ((TextView) dialogView.findViewById(R.id.noteText)).setText("Are you sure you want to Book the desk for the date of\n" + bookDate + " ?");
            dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                confirmationAlert.dismiss();

                UserBookDate booking = new UserBookDate();
                booking.deskDocId = deskUserDetailsScreenDTO.docID;
                booking.userDocId = docId;
                booking.date = bookDate;

                if (deskUserDetailsScreenDTO.bookDate == null)
                    deskUserDetailsScreenDTO.bookDate = new ArrayList<>();

                deskUserDetailsScreenDTO.bookDate.add(booking);

                startAnim();
                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection)
                        .document(deskUserDetailsScreenDTO.getDocID()).set(deskUserDetailsScreenDTO).
                        addOnCompleteListener(task ->
                        {
                            if (task.isSuccessful()) {
                                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                                        .get().addOnCompleteListener(userTask -> {
                                    stopAnim();
                                    if (userTask.isSuccessful()) {
                                        SignupUserDTO user = userTask.getResult().toObject(SignupUserDTO.class);
                                        if (user.bookDate == null)
                                            user.bookDate = new ArrayList<>();

                                        user.bookDate.add(booking);
                                        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                                                .set(user);

                                        UtilityFunctions.alertNoteWithOkButton(context, "Smart Desk Booking", "Your desk has been booked Successfully!",
                                                Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, true, null);
                                    }
                                });
                            } else {
                                stopAnim();
                                UtilityFunctions.redSnackBar(context, "Unfortunately,Desk not book!", Snackbar.LENGTH_LONG);
                            }
                        }).addOnFailureListener(e ->
                        {
                            stopAnim();
                            UtilityFunctions.redSnackBar(context, "Unfortunately, Desk not book!", Snackbar.LENGTH_LONG);
                        }
                );
            });

            dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> confirmationAlert.dismiss());
            confirmationAlert.setView(dialogView);
            confirmationAlert.setCancelable(false);
            confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            confirmationAlert.show();
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
    }
}