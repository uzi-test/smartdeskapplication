package com.smartdesk.utility.location;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.smartdesk.utility.UtilityFunctions;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class LocationJob extends JobService {

    private static final String TAG = LocationJob.class.getSimpleName();

    public static String mobile = "";
    public static String password = "";

    @Override
    public boolean onStartJob(final JobParameters job) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sentLocation(job);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public void sentLocation(final JobParameters parameters) {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                String AccessToken = UtilityFunctions.getDocumentID(getApplicationContext());
                if (AccessToken != null && !AccessToken.equals("")) {

                    try {
                        new FusedLocationForService( getApplicationContext(), AccessToken).startLocationUpdates(true);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }else{
                    jobFinished(parameters, true);
                    Log.d(TAG, "localtion: " + "jobFinish");
                }
            }, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jobFinished(parameters, true);
        }
    }
}