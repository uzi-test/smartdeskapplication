package com.smartdesk.screens.user_management.help;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.CustomEditext;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.utility.gmail.GmailSender;
import com.smartdesk.utility.memory.MemoryCache;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;
import java.util.Date;

import static com.smartdesk.constants.Constants.smartDeskLogo;


public class ScreenHelp extends AppCompatActivity {

    private Activity context;

    private CustomEditext etMessage, emailReceiver;
    private Boolean isOkay;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_help);
        context = this;
        actionBar("Help");
        initLoadingBarItems();
        initIds();
        UtilityFunctions.setupUI(findViewById(R.id.parent), this);
    }

    private void initIds() {
        emailReceiver = findViewById(R.id.email);
        etMessage = findViewById(R.id.complain);
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

    private String getDataFromEditext(EditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            isOkay = false;
        }
        return text;
    }

    String email, msg;

    public void helpSubmit(View view) {
        isOkay = true;
        email = getDataFromEditext(emailReceiver, "Invalid email", 5);
        if (!UtilityFunctions.isValidEmail(email)) {
            emailReceiver.setError("Invalid Email");
            isOkay = false;
        }
        msg = getDataFromEditext(etMessage, "Message Length not appropriate", 10);
        if (isOkay) {
            startAnim();
            SendEmail asyncTask = new SendEmail();
            asyncTask.execute(1);
        } else {
            UtilityFunctions.orangeSnackBar(context, "Please provide valid information", Snackbar.LENGTH_SHORT);
        }
    }

    private void postHelpAPI() {
        try {
            GmailSender sender = new GmailSender(Constants.emailID, Constants.password);
            sender.sendMail("Successfully received your issue at " +Constants.SmartDesk,
                    "<html> <body >"
                            + "<img src='" + smartDeskLogo + "' alt='"+Constants.SmartDesk+" Logo' />"
                            + "<br><br>Dear " + Constants.USER_NAME + ":<br><br>"
                            + "We received your below message regarding your issue<br><br><strong>\""
                            + msg + "\".</strong><br><br>We will solve your issue as soon as possible.<br><br>"
                            + "Thanks & Best Regards,<br>"
                            + Constants.SmartDesk
                            + "</body> </html>",
                    Constants.emailID,
                    email);

            sender.sendMail(Constants.SmartDesk + " Issue by " + Constants.USER_MOBILE,
                    "<html> <body >"
                            + "<img src='" + smartDeskLogo + "' alt='"+Constants.SmartDesk+" Logo' />"
                            + "<br><br>Dear Admin:<br><br>"
                            + "We received an issue<br><br><strong>\""
                            + msg + "\".</strong><br><br>"
                            + "<strong>User Detials</strong><br>"
                            + "Name:" + Constants.USER_NAME + "<br>"
                            + "Mobile Number:" + Constants.USER_MOBILE + "<br>"
                            + "Document ID:" + Constants.USER_DOCUMENT_ID + "<br>"
                            + "Profile Image: <img src='" + Constants.USER_PROFILE + "' alt='User Image' width='200' height='200'/><br>"
                            + "<br><br>See and fix the issue as soon as possible.<br><br>"
                            + "Thanks & Best Regards,<br>"
                            + Constants.SmartDesk
                            + "</body> </html>",
                    Constants.emailID,
                    Constants.emailID);


        } catch (Exception e) {
            UtilityFunctions.redSnackBar(context, "Failed to send email", Snackbar.LENGTH_LONG);
            stopAnimOnUithread();
            Log.e("SendMail", e.getMessage(), e);
        }
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

    private class SendEmail extends AsyncTask<Integer, Integer, Long> {
        protected Long doInBackground(Integer... urls) {
            postHelpAPI();
            return 0l;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            stopAnimOnUithread();
            new Thread(() -> {
                UtilityFunctions.sendFCMMessage(context, new Data(FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()).getTime(), "Help", Constants.SmartDesk+" issue reported", Constants.USER_NAME + " reported an issue kindly visit email"));
                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.adminRole, FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()), Constants.SmartDesk+" issue reported", Constants.USER_NAME + " reported an issue kindly visit email for more details:\nIssue:" + msg, false));
            }).start();
            new Thread(() -> {
                UtilityFunctions.sendFCMMessage(context, new Data(Constants.USER_DOCUMENT_ID, new Timestamp(new Date().getTime()).getTime(), "Help", "Your Issue noted", Constants.USER_NAME + " your reported issue is received successfully"));
                UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.USER_ROLE, Constants.USER_DOCUMENT_ID, new Timestamp(new Date().getTime()), Constants.SmartDesk + " issue reported", "You reported an issue.\nIssue:" + msg, false));
            }).start();
            UtilityFunctions.alertNoteWithOkButton(context, "Email Sent", "Email has been sent successfully", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, null);
        }
    }

    private void stopAnimOnUithread() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            stopAnim();
        }, 0);
    }
}
