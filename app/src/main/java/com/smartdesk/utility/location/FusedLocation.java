package com.smartdesk.utility.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.smartdesk.constants.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import static com.smartdesk.constants.PermisionCode.MY_LOCATION_PERMISSIONS_CODE;
import static com.smartdesk.utility.UtilityFunctions.getAddressLatLng;
import static com.smartdesk.utility.UtilityFunctions.showGPSDisabledAlertToUser;
import static com.smartdesk.utility.UtilityFunctions.turnGPSOn;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class FusedLocation extends Service {

    private Boolean isAddressSet;
    Activity context;

    public FusedLocation() {
    }

    public FusedLocation(Activity context, Boolean isAddressSet) {
        this.context = context;
        this.isAddressSet = isAddressSet;
    }

//    public FusedLocation(Activity context, Boolean isAddressSet, FragmentMapView fragmentMapView) {
//        this.context = context;
//        this.isAddressSet = isAddressSet;
//        this.fragmentMapView = fragmentMapView;
//    }


    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 1000 * 30;  /* 1 sec */
    private long FASTEST_INTERVAL = 1000 * 30; /* 1 sec */
    Boolean isService;
    Boolean isFirst = true;

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(Boolean isService) {
        try {
            this.isService = isService;
            // Create the location request to start receiving updates
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
            SettingsClient settingsClient = LocationServices.getSettingsClient(context);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            if (getPermission()) {
                getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                onLocationChanged(locationResult.getLastLocation());
                            }
                        },
                        Looper.getMainLooper());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Boolean getPermission() {
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION))
                    (context).requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSIONS_CODE);
                else
                    (context).requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSIONS_CODE);
                return false;
            }
            try {
                if (!turnGPSOn(context)) {
                    showGPSDisabledAlertToUser(context, "GPS is disabled in your device. Please enable it to use this app", false);
                }
            } catch (Exception ex) {

            }
            return true;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);
        if (getPermission()) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    });
        }
    }


    public void onLocationChanged(Location location) {
        try {
            if (!turnGPSOn(context)) {
                showGPSDisabledAlertToUser(context, "GPS is disabled in your device. Please enable it to use this app", false);
            }
        } catch (Exception ex) {

        }
        Constants.const_lat = location.getLatitude();
        Constants.const_lng = location.getLongitude();

        try {
//            String AccessToken = UtilityFunctions.getDocumentID(context);
//            if (AccessToken != null && !AccessToken.equals("")) {
//                FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(AccessToken)
//                        .update("workerLat", Constants.const_lat,
//                                "workerLng", Constants.const_lng);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (isAddressSet != null && !isAddressSet) {
            isAddressSet = true;
            if (Constants.addressEditext != null)
                Constants.addressEditext.setText(getAddressLatLng(context, Constants.const_lat, Constants.const_lng));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
