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

public class ScreenSmartDeskBookDetailUser extends AppCompatActivity {

    ScreenSmartDeskDetailUserBinding binding;
    private Activity context;
    public static NewDesk deskUserDetailsScreenDTO;

    String docId = "";

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
        binding.llStatuslist.setVisibility(View.GONE);

        setRecyclerView();

        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID).get().addOnSuccessListener(
                queryDocumentSnapshots ->
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (queryDocumentSnapshots.exists()) {
                                cUser = queryDocumentSnapshots.toObject(SignupUserDTO.class);
                                if (cUser != null) {
                                    List<UserBookDate> ub = new ArrayList<>();
                                    for (UserBookDate temp : book) {
                                        if (temp.getUserDocId().equals(cUser.getUuID())) {
                                            ub.add(temp);
                                        }
                                    }
                                    deskListNew.clear();
                                    deskListNew.addAll(ub);
                                    adapter.notifyDataSetChanged();
                                }
                            } else
                                System.out.println("FAILED");
                        })).
                addOnFailureListener(e -> {
                });
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

    RecyclerView recyclerView;
    ScreenSmartDeskBookDetailUser.Adapter adapter;
    List<UserBookDate> deskListNew = new ArrayList<UserBookDate>();
    public SignupUserDTO cUser = new SignupUserDTO();

    public void setRecyclerView() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            adapter = new ScreenSmartDeskBookDetailUser.Adapter(deskListNew);
            recyclerView = UtilityFunctions.setRecyclerView(binding.recyclerView, context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<ScreenSmartDeskBookDetailUser.Adapter.ViewHolder> {

        List<UserBookDate> userBookDates;

        public Adapter(List<UserBookDate> userBookDates) {
            this.userBookDates = userBookDates;
        }

        @NonNull
        @Override
        public ScreenSmartDeskBookDetailUser.Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_book, parent, false);
            return new ScreenSmartDeskBookDetailUser.Adapter.ViewHolder(view);
        }

        @Override
        public void onViewDetachedFromWindow(ScreenSmartDeskBookDetailUser.Adapter.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        @Override
        public void onBindViewHolder(@NonNull ScreenSmartDeskBookDetailUser.Adapter.ViewHolder holder, final int position) {

            UserBookDate cUB = userBookDates.get(position);
            holder.name.setText("Book By Unknown User");
            if (cUser != null) {
                holder.name.setText(cUser.getWorkerName());
                holder.bookDate.setText(userBookDates.get(position).getDate());
                SignupUserDTO finalUser = cUser;
                holder.cancelBtn.setOnClickListener(v ->
                {
                    try {
                        List<UserBookDate> ub = finalUser.getBookDate();

                        for (int i = 0; i < ub.size(); i++) {
                            if (ub.get(i).getDate().equals(cUB.getDate()) &&
                                    ub.get(i).getUserDocId().equals(cUB.getUserDocId()) &&
                                    ub.get(i).getDeskDocId().equals(cUB.getDeskDocId())) {
                                ub.remove(ub.get(i));
                                break;
                            }
                        }

                        finalUser.setBookDate(ub);
                        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection)
                                .document(finalUser.getUuID()).set(finalUser);

                        List<UserBookDate> ub1 = deskUserDetailsScreenDTO.getBookDate();
                        for (int i = 0; i < ub1.size(); i++) {
                            if (ub1.get(i).getDate().equals(cUB.getDate()) &&
                                    ub1.get(i).getUserDocId().equals(cUB.getUserDocId()) &&
                                    ub1.get(i).getDeskDocId().equals(cUB.getDeskDocId())) {
                                ub1.remove(ub1.get(i));
                                break;
                            }
                        }

                        deskUserDetailsScreenDTO.setBookDate(ub1);
                        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection)
                                .document(cUB.getDeskDocId()).set(deskUserDetailsScreenDTO);

                        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection)
                                .document(deskUserDetailsScreenDTO.getDocID()).get().addOnSuccessListener(documentSnapshot1 -> {
                            if (documentSnapshot1.exists()) {
                                List<UserBookDate> aa = documentSnapshot1.toObject(NewDesk.class).getBookDate();
                                if (aa.size() > 0) {
                                    userBookDates.clear();
                                    userBookDates.addAll(aa);
                                    adapter.notifyDataSetChanged();
                                } else
                                    finish();
                            } else {
                                finish();
                            }
                        });
                    } catch (Exception ex) {
                    }
                });
            }
        }

        public int getItemCount() {
            return userBookDates.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, bookDate;
            Button cancelBtn;

            public ViewHolder(@NonNull View view) {
                super(view);
                name = view.findViewById(R.id.name);
                bookDate = view.findViewById(R.id.bookDate);
                cancelBtn = view.findViewById(R.id.cancelBtn);
            }
        }
    }
}