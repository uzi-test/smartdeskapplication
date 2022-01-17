package com.smartdesk.utility;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.services.MyResponse;
import com.smartdesk.services.RestClient;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.fcm.FCMPayload;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.utility.location.LocationJob;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pd.chocobar.ChocoBar;
import com.santalu.maskedittext.MaskEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;
import static com.smartdesk.constants.PermisionCode.MY_LOCATION_PERMISSIONS_CODE;

public class UtilityFunctions {

    private static String TAG = "UtilityFucntions";

    public static String remaingTimeCalculation(Timestamp currentTime, Timestamp registrationtime) {
        long milliseconds = currentTime.getTime() - registrationtime.getTime();
        Long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        if (days == 0) return "Today";
        else if (days == 1) return "Yesterday";
        else return days + " days ago";
    }

    public static Long remaingTimeCalculationInMinutes(Timestamp currentTime, Timestamp registrationtime) {
        long milliseconds = currentTime.getTime() - registrationtime.getTime();
        System.out.println("current Time: " + currentTime);
        System.out.println("end Time: " + registrationtime);
        System.out.println("Difference: ");
        System.out.println(" Hours: " + TimeUnit.MILLISECONDS.toHours(milliseconds));
        System.out.println(" Days: " + TimeUnit.MILLISECONDS.toDays(milliseconds));
        System.out.println(" Seconds: " + TimeUnit.MILLISECONDS.toSeconds(milliseconds));
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        return minutes;
    }

    public static RecyclerView setRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
//        recyclerView.setItemViewCacheSize(20);
//        recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        return recyclerView;
    }

    public static int timeConversion(String time) {
        String[] temp = time.split(":");
        try {
            int numberOfHoursTemp = Integer.parseInt(temp[0]);
            int numberOfMinsTemp;
            if (numberOfHoursTemp != 0)
                numberOfMinsTemp = numberOfHoursTemp * 60;
            else
                numberOfMinsTemp = 0;
            int totalMins = Integer.parseInt((temp[temp.length - 1]));
            totalMins = totalMins + numberOfMinsTemp;
            return totalMins;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void saveLoginCredentialsInSharedPreference(Context context, String mobile, String password, String documentID, Boolean isLogin) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.SP_MOBILE, mobile);
            editor.putString(Constants.SP_PASSWORD, password);
            editor.putString(Constants.SP_DOCUMENT_ID, documentID);
            editor.putBoolean(Constants.SP_ISLOGIN, isLogin);
            editor.apply();
        }, 0);
    }

    public static void setIsLoignSharedPreference(Context context, Boolean isLogin) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.SP_ISLOGIN, isLogin);
        editor.apply();
    }


    public static void removeLoginInfoInSharedPreference(final Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.SP_DOCUMENT_ID, null);
            editor.putString(Constants.SP_MOBILE, null);
            editor.putString(Constants.SP_PASSWORD, null);
            editor.putString(Constants.SP_ISLOGIN, null);
            editor.apply();
        }, 0);
    }

    public static String getDocumentID(Context context) {
        SharedPreferences prefs = null;
        String AccessToken = null;
        try {
            prefs = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
            AccessToken = prefs.getString(Constants.SP_DOCUMENT_ID, null);
            return AccessToken;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AccessToken;
    }

    public static void getCredentialsFromSharedPreference(Context context) {
        SharedPreferences prefs = null;
        try {
            prefs = context.getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
//            NotificationJob.mobile = prefs.getString(SP_MOBILE, null);
//            NotificationJob.password = prefs.getString(SP_PASSWORD, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void showNotification(Context context, int notificationID, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println("IN SHOW NOTIFICATION");
        String channelId = Constants.SmartDesk + "-01";
        String channelName = Constants.SmartDesk;
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.z_desk_loading)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(alarmSound);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(notificationID, mBuilder.build());
    }

    public static void scheduleJob(Context context, boolean isLocation) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        if (isLocation) {
            Job job = createLocationJob(dispatcher);
            dispatcher.mustSchedule(job);
        } else {
            Job job = createNotificationJob(dispatcher);
            dispatcher.mustSchedule(job);
        }
    }


    public static Boolean turnGPSOn(Activity context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    public static void showGPSDisabledAlertToUser(final Activity activity, String msg, Boolean isFusedLocation) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (Constants.confirmationAlert != null)
                    Constants.confirmationAlert.dismiss();
                Constants.confirmationAlert = new AlertDialog.Builder(activity).create();
                final View dialogView = activity.getLayoutInflater().inflate(R.layout.alert_dialog_return, null);
                ((TextView) dialogView.findViewById(R.id.noteText)).setText(msg);
                ((TextView) dialogView.findViewById(R.id.noteTitle)).setText("GPS Location");
                ((Button) dialogView.findViewById(R.id.yesbtn)).setText("Go");
                ((Button) dialogView.findViewById(R.id.nobtn)).setText("Back");
                dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                    Constants.confirmationAlert.dismiss();
                    if (!turnGPSOn(activity)) {
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(callGPSSettingIntent, MY_LOCATION_PERMISSIONS_CODE);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showGPSDisabledAlertToUser(activity, msg, false);
                        }, 200);
                    }
                });
                dialogView.findViewById(R.id.nobtn).setOnClickListener(v -> {
                    Constants.confirmationAlert.dismiss();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            activity.finish();
                        } catch (Exception ex) {

                        }
                    }, 0);
                });
                Constants.confirmationAlert.setView(dialogView);
                Constants.confirmationAlert.setCancelable(false);
                Constants.confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Constants.confirmationAlert.show();
            } catch (Exception ex) {
            }
        }, 0);
    }


    public static Job createLocationJob(FirebaseJobDispatcher dispatcher) {
        Job job = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)
//                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //call this service when the criteria are met.
                .setService(LocationJob.class)
                //unique id of the task
                .setTag("SmartDeskTaskJobLocation")
                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // We are mentioning that the job is periodic.
                .setRecurring(true)
                // Run between 30 - 60 seconds from now.
                .setTrigger(Trigger.executionWindow(60, 60 + 30))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
//                .setConstraints(Constraint.ON_ANY_NETWORK, Constraint.DEVICE_CHARGING)
                .build();
        return job;
    }

    public static Job createNotificationJob(FirebaseJobDispatcher dispatcher) {
        Job job = dispatcher.newJobBuilder()
                //persist the task across boots
                .setLifetime(Lifetime.FOREVER)
                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //call this service when the criteria are met.
//                .setService(NotificationJob.class)
                //unique id of the task
                .setTag(Constants.SmartDesk + "TaskNotification")
                //don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // We are mentioning that the job is periodic.
                .setRecurring(true)
                // Run between 30 - 60 seconds from now.
                .setTrigger(Trigger.executionWindow(1, 10))
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //Run this job only when the network is available.
//                .setConstraints(Constraint.ON_ANY_NETWORK, Constraint.DEVICE_CHARGING)
                .build();
        return job;
    }

    public static ObjectAnimator loadingAnim(final Activity activity, ImageView image) {
        try {
            Animation animation = AnimationUtils.loadAnimation(activity, R.anim.shake);
            image.startAnimation(animation);
            animation.setDuration(800);

            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(activity, R.animator.flip);
            anim.setTarget(image);
            anim.setDuration(1500);
            anim.start();
            return anim;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void setupUI(View view, final Activity activity) {
        try {
            // Set up touch listener for non-text box views to hide keyboard.
            if (!(view instanceof EditText)) {
                view.setOnTouchListener((v, event) -> {
                    hideKeyboard(activity);
                    try {
                        view.requestFocus();
                    } catch (Exception ex) {

                    }
                    return false;
                });
            }
            //If a layout container, iterate over children and seed recursion.
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View innerView = ((ViewGroup) view).getChildAt(i);
                    setupUI(innerView, activity);
                }
            }
        } catch (Exception ex) {

        }
    }

    public static void removeFocusFromEditexts(View view, final Activity activity) {
        try {
            hideKeyboard(activity);
        } catch (Exception ex) {

        }
        try {
            view.requestFocus();
        } catch (Exception ex) {

        }
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {
        }
    }

    public static boolean isValidPhone(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches());
    }

    public static boolean isValidName(String target) {
        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(target);
        boolean bs = ms.matches();
        if (bs == false)
            return false;
        return true;
    }

    public static boolean isValidPassword(final String password) {
        return Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$&+,:;=\\\\\\\\?@#|/'<>.^*()%!-]).+$").matcher(password).matches();
    }

    public static String getStringFromEditTextWithLengthLimit(EditText getText, int minSize) {
        if (getText == null || getText.getText().toString().isEmpty() || getText.getText().toString().length() < minSize)
            throw new NullPointerException();
        else
            return getText.getText().toString();
    }

    public static String getStringFromMaskWithLengthLimit(MaskEditText getText, int minSize) {
        if (getText == null || getText.getText().toString().isEmpty() || getText.getText().toString().length() < minSize)
            throw new NullPointerException();
        else
            return getText.getRawText();
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null)
                if (activity.getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void roundImageCorner(final Context context,
                                        final ImageView imageView, String imagePath, final ShimmerFrameLayout shimmer) {
        try {
            Glide.with(context).asBitmap().load(imagePath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                Bitmap mbitmap = ((BitmapDrawable) context.getResources().getDrawable( )).getBitmap();
                    try {
                        Bitmap mbitmap = resource;
                        Bitmap imageRounded = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
                        Canvas canvas = new Canvas(imageRounded);
                        Paint mpaint = new Paint();
                        mpaint.setAntiAlias(true);
                        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                        canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 100, 100, mpaint); // Round Image Corner 100 100 100 100
                        imageView.setImageBitmap(imageRounded);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    shimmerRemove(shimmer);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    shimmerRemove(shimmer);
                }
            });
        } catch (Exception ex) {

        }
    }

    public static void disableShimmer(ShimmerFrameLayout shimmer) {
        shimmerRemove(shimmer);
    }

    public static void picassoGetCircleImage(Activity activity, String url, CircleImageView
            imageView, ShimmerFrameLayout shimmer, int placeHolder) {
        if (url != null && !url.equals("")) {
            Picasso.get().load(url)
                    .resize(150, 150)
                    .placeholder(placeHolder)
                    .error(placeHolder)
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            shimmerRemove(shimmer);
                        }

                        @Override
                        public void onError(Exception e) {
                            shimmerRemove(shimmer);
                        }

                    });
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(activity, placeHolder));
            shimmerRemove(shimmer);
        }
    }

    public static void picassoGetCircleImage(Activity activity, int url, CircleImageView
            imageView, ShimmerFrameLayout shimmer, int placeHolder) {

        Picasso.get().load(url)
                .resize(150, 150)
                .placeholder(placeHolder)
                .error(placeHolder)
                .centerCrop()
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        shimmerRemove(shimmer);
                    }

                    @Override
                    public void onError(Exception e) {
                        shimmerRemove(shimmer);
                    }

                });
    }

    public static void picassoGetCircleImage(Activity activity, String url, ImageView
            imageView, ShimmerFrameLayout shimmer, int placeHolder) {
        if (url != null && !url.equals("")) {
            Picasso.get().load(url)
                    .resize(150, 150)
                    .placeholder(placeHolder)
                    .error(placeHolder)
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            shimmerRemove(shimmer);
                        }

                        @Override
                        public void onError(Exception e) {
                            shimmerRemove(shimmer);
                        }
                    });
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(activity, placeHolder));
            shimmerRemove(shimmer);
        }
    }

    public static void noRoundImageCorner(final Context context,
                                          final ImageView imageView, String imagePath, final ShimmerFrameLayout shimmer) {
        try {
            System.out.println(imagePath + "URL");
            Glide.with(context).asBitmap().placeholder(R.drawable.side_profile_icon)
                    .error(R.drawable.side_profile_icon).load(imagePath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Bitmap mbitmap = resource;
                    imageView.setImageBitmap(mbitmap);
                    System.out.println("SETTTTTTTTTTTT");
                    shimmerRemove(shimmer);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    shimmerRemove(shimmer);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void shimmerRemove(ShimmerFrameLayout shimmer) {
        try {
            shimmer.hideShimmer();
            shimmer.stopShimmer();
        } catch (Exception ex) {
        }
    }

    public static void noRoundImageCorner(final Context context,
                                          final ImageView imageView, Bitmap imagePath, final ShimmerFrameLayout shimmer) {
        try {
            Glide.with(context).asBitmap().load(imagePath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Bitmap mbitmap = resource;
                    imageView.setImageBitmap(mbitmap);
                    shimmerRemove(shimmer);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    shimmerRemove(shimmer);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static File BitmapToFile(Activity activity, File file, Bitmap bitmap) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void greenSnackBar(Activity activity, String msg, int length) {
        ChocoBar.builder()
                .setActivity(activity)
                .setText(msg)
                .setDuration(length)
                .green()
                .show();
    }

    public static void orangeSnackBar(Activity activity, String msg, int length) {
        ChocoBar.builder().setActivity(activity)
                .setText(msg)
                .setDuration(length)
                .orange()
                .show();
    }

    public static void redSnackBar(Activity activity, String msg, int length) {
        ChocoBar.builder().setActivity(activity)
                .setText(msg)
                .setDuration(length)
                .red()
                .show();
    }

    public static void logoutSnackBar(Activity activity, String msg, int length) {
        ChocoBar.builder()
                .setIcon(R.drawable.side__logout_vector).setActivity(activity)
                .setText(msg)
                .setActivity(activity)
                .setDuration(length)
                .red()
                .show();
    }

    public static void customSnackBar(Activity activity, String msg, int length) {
        ChocoBar.builder().setBackgroundColor(Color.parseColor("#00bfff"))
                .setTextSize(18)
                .setTextColor(Color.parseColor("#FFFFFF"))
                .setTextTypefaceStyle(Typeface.ITALIC)
                .setText(msg)
                .setMaxLines(4)
                .centerText()
                .setActionText("ChocoBar")
                .setActionTextColor(Color.parseColor("#66FFFFFF"))
                .setActionTextSize(20)
                .setActionTextTypefaceStyle(Typeface.BOLD)
                .setActivity(activity)
                .setDuration(length)
                .build()
                .show();
    }


    public static void editeTextFocusReset(ScrollView sv, View editText) {
        sv.scrollTo(0, (int) editText.getY());
    }

    public static void subscribeFCMTopic(String topicName) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                    }
                    Log.d(TAG, msg);
                    System.out.println("SSSSSSSSSSSUBSSS ++" + msg);
                });
    }

    public static void unSubscribeFCMTopic(String topicName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                    }
                    Log.d(TAG, msg);
                    System.out.println("SSSSSSSSSSSUBSSS ++" + msg);
                });
    }

    public static void sendFCMMessage(Activity activity, Data data) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            subscribeFCMTopic(FirebaseConstants.fcmTopic);
            FCMPayload fcmPayload = new FCMPayload(FirebaseConstants.fcmTopic, data);
            RestClient.get().fcmSend(fcmPayload).enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                    if (response.code() == 200) {
                        if (response.body().getSuccess() != 1) {
                            System.out.println("Failure");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MyResponse> call, Throwable t) {
                    System.out.println("FAILUREEEEEEEEEEEEEEEEEEEEEE");
                }
            });
        }, 0);
    }

    public static void saveNotficationCollection(NotificationDTO notificationDTO) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection).add(notificationDTO);
        }, 0);
    }

    public static void deleteUserCompletelu(String documentID) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection).whereEqualTo("documentID", documentID)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments())
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection).document(document.getId()).delete();
            });
        }, 0);
    }

    // UploadImage method
    public synchronized static void uploadImage(Activity activity, Uri filePath, String
            rootImageType, String phoneNumber, int postion) {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Uploading " + rootImageType);
            progressDialog.setCancelable(false);
            progressDialog.show();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference().child(rootImageType + "/" + phoneNumber + rootImageType);

            UploadTask uploadTask = ref.putFile(filePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    if (Constants.const_usersSignupDTO != null) {
                        if (postion == 2)
                            Constants.const_usersSignupDTO.setProfilePicture(downloadUri.toString());
                    }
//                    else if (Constants.const_ConsumerSignupDTO != null) {
//                        Constants.const_ConsumerSignupDTO.setProfilePicture(downloadUri.toString());
//                    }
                    else {
                        FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).document(Constants.USER_DOCUMENT_ID)
                                .update("profilePicture", downloadUri.toString());
                    }
                    System.out.println(downloadUri.toString());
                } else {
                    progressDialog.dismiss();
                }
            });
        }
    }

    public static String getAddressLatLng(Activity activity, Double lat, Double lng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null) {
                String address = "Address not found";
                for (int i = 0; i < addresses.size(); i++) {
                    address = addresses.get(0).getAddressLine(0);
                    String address1 = addresses.get(0).getAddressLine(1);
                    break;
                }
                return address;
            }
            return "Address not found";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return "Address not found";
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult((Activity) context, 1);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public static void sendIntentNormal(Activity activity, Intent intent, Boolean isFinish,
                                        int changeIntentDelay) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(intent);
                if (isFinish)
                    activity.finish();
            }
        }, changeIntentDelay);
    }

    public static void sendIntentClearPreviousActivity(Activity activity, Intent intent,
                                                       int changeIntentDelay) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }, changeIntentDelay);
    }

    public static AlertDialog setAlertDialog(Activity context) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setCancelable(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            return alertDialog;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void alertNoteWithOkButton(Activity activity, String title, String textMsg,
                                             int gravity, int bgColor, int fontColor, Boolean isFinish, Boolean isIntentNormal, Intent
                                                     intent) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                final AlertDialog confirmationAlert = new AlertDialog.Builder(activity).create();
                final View dialogView = activity.getLayoutInflater().inflate(R.layout.alert_note_dialog, null);
                ((TextView) dialogView.findViewById(R.id.noteTitle)).setText(title);
                ((TextView) dialogView.findViewById(R.id.noteText)).setText(textMsg);
                ((TextView) dialogView.findViewById(R.id.noteText)).setGravity(gravity);

                ((ImageView) dialogView.findViewById(R.id.iconWarning)).setImageTintList(ContextCompat.getColorStateList(activity, fontColor));
                ((TextView) dialogView.findViewById(R.id.noteTitle)).setTextColor(ContextCompat.getColor(activity, fontColor));
                ((Button) dialogView.findViewById(R.id.yesbtn)).setTextColor(ContextCompat.getColor(activity, fontColor));
                dialogView.findViewById(R.id.yesbtn).setBackgroundTintList(ContextCompat.getColorStateList(activity, bgColor));
                dialogView.findViewById(R.id.titleColor).setBackgroundTintList(ContextCompat.getColorStateList(activity, bgColor));

                dialogView.findViewById(R.id.yesbtn).setOnClickListener(v -> {
                    confirmationAlert.dismiss();
                    if (isFinish && intent == null)
                        activity.finish();
                    else if (isIntentNormal && intent != null) {
                        sendIntentNormal(activity, intent, isFinish, 0);
                    } else if (!isIntentNormal && intent != null) {
                        sendIntentClearPreviousActivity(activity, intent, 0);
                    }
                });
                confirmationAlert.setView(dialogView);
                confirmationAlert.setCancelable(false);
                confirmationAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                confirmationAlert.show();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0);
    }


    public static String getDateFormat(Date date) {
        String pattern = "EEE, dd-MMM-yyyy HH:mm ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date.getTime());
    }

    public static float calculateRating(Integer userCount, Integer totalRating) {
        if (userCount == null || userCount <= 0)
            return 5f;
        return (totalRating / userCount);
    }

    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                       @DrawableRes int vectorResourceId,
                                                       @ColorInt int tintColor) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                context.getResources(), vectorResourceId, null);
        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        DrawableCompat.setTint(vectorDrawable, tintColor);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.icon_pin);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static double distanceBetweenTwoLocations(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    public static String getPhoneNumberInFormat(String number) {
        try {
            return number.substring(0, 2) + "-" + number.substring(2);
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getDeskID(String docID) {
        return "Desk-" + docID.substring(docID.length() - 5, docID.length());
    }


    public static String getDeskRegDate(String date) {
        return "Reg. Date: " + date.split("at")[0];
    }

    public static String getDistanceInFormat(Double number) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(number) + " Km away";
    }
}
