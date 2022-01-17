package com.smartdesk.screens.manager_screens.sign_up;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.santalu.maskedittext.MaskEditText;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.CustomEditext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentManagerStep1 extends Fragment implements View.OnClickListener {

    private View view;

    private DatePickerDialog mDateListener;
    private CustomEditext et_workerName,et_dob, et_password, et_confirmPassword, et_shopLocation;
    public CustomEditext et_email;
    public MaskEditText met_phoneNumber;
    private Spinner genderSpinner;
    private TextView genderSpinnerError;

    private TextView termsTextview;
    private CheckBox termscheckbox;

    private static boolean isfirstRequestFocus = false;
    private Context signupContext;

    private ScrollView sv;
    private boolean isGenderSelected = false;

    public FragmentManagerStep1() {
    }

    public FragmentManagerStep1(Context mContext) {
        signupContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_desk_user_step1, container, false);
        initIDS();
        makeAttributedText();
        setSpinnerForGender();
        getSelectedSpinnerItem();
        dobListner();
        UtilityFunctions.setupUI(view.findViewById(R.id.parent), (Activity) signupContext);
        initKeyBoardListener();
        return view;
    }

    private void initKeyBoardListener() {
        try {
            final int MIN_KEYBOARD_HEIGHT_PX = 150;
            final View decorView = ((ScreenMangerSignup) signupContext).getWindow().getDecorView();
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                private final Rect windowVisibleDisplayFrame = new Rect();
                private int lastVisibleDecorViewHeight;

                @Override
                public void onGlobalLayout() {
                    try {
                        decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                        final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                        if (lastVisibleDecorViewHeight != 0) {
                            if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                                ((ScreenMangerSignup) signupContext).nextBtn.setVisibility(View.GONE);
                                ((ScreenMangerSignup) signupContext).linearHide.setVisibility(View.GONE);
                            } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                                ((ScreenMangerSignup) signupContext).nextBtn.setVisibility(View.VISIBLE);
                                ((ScreenMangerSignup) signupContext).linearHide.setVisibility(View.VISIBLE);
                            }
                        }
                        lastVisibleDecorViewHeight = visibleDecorViewHeight;
                    } catch (Exception ex) {

                    }
                }
            });
        } catch (Exception ex) {

        }
    }

    private void dobListner() {
        et_dob.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mmonth = c.get(Calendar.MONTH);
            int mdate = c.get(Calendar.DAY_OF_MONTH);
            int myear = c.get(Calendar.YEAR);

            mDateListener = new DatePickerDialog(signupContext, R.style.DialogTheme, (view, year, month, dayOfMonth) -> {
                c.set(year, month, dayOfMonth);
                String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                et_dob.setText(date);
            }, myear, mmonth, mdate);
            mDateListener.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDateListener.show();
        });
    }

    public void makeAttributedText() {
        String textAttributed = "Are you Agree with our Terms & Conditions";
        int startIndex = textAttributed.indexOf("Terms & Conditions");
        int endIndex = "Terms & Conditions".length();

        SpannableString ss = new SpannableString(textAttributed);
        ClickableSpan privacyPolicy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
//                Uri uri = Uri.parse("https://socolcorp.com/#/policy"); // missing 'http://' will cause crashed
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
                UtilityFunctions.orangeSnackBar((Activity) signupContext, "don't have terms & conditions", Snackbar.LENGTH_SHORT);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(true);
                ds.setTypeface(Typeface.DEFAULT_BOLD);
                ds.setColor(ContextCompat.getColor(signupContext, R.color.SmartDesk_Blue));
                super.updateDrawState(ds);
            }
        };
        ss.setSpan(privacyPolicy, startIndex, startIndex + endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsTextview.setText(ss);
        termsTextview.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextview.setHighlightColor(Color.TRANSPARENT);
    }

    private void initIDS() {
        //Edit Text
        ((TextView ) view.findViewById(R.id.UserDetails)).setText("Manager Details ⁕");
        sv = view.findViewById(R.id.sv);
        et_workerName = view.findViewById(R.id.WorkerName);
        et_email = view.findViewById(R.id.Email);
        et_dob = view.findViewById(R.id.dob);
        et_password = view.findViewById(R.id.password);
        et_confirmPassword = view.findViewById(R.id.confirmPassword);
        et_shopLocation = view.findViewById(R.id.AddressLocation);
        Constants.addressEditext = et_shopLocation;
        //Mask Text
        met_phoneNumber = view.findViewById(R.id.phoneNumber);
        //Spinners
        genderSpinner = view.findViewById(R.id.gender);
        genderSpinnerError = view.findViewById(R.id.genderError);

        termscheckbox = view.findViewById(R.id.termscheckbox);
        termsTextview = view.findViewById(R.id.attr_text);
        termscheckbox.setOnClickListener(this);
    }

    public void setLocatioEditText() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> et_shopLocation.setText(UtilityFunctions.getAddressLatLng((Activity) signupContext, Constants.const_lat, Constants.const_lng)), 1000);
    }

    public void validationsAndGetValues() {
        ScreenMangerSignup.isOkayStep1 = true;
        isfirstRequestFocus = false;
        Constants.const_usersSignupDTO.setWorkerLocation(et_shopLocation.getText().toString());
        Constants.const_usersSignupDTO.setWorkerName(getDataFromEditext(et_workerName, "Invalid Name", 3));
        Constants.const_usersSignupDTO.setWorkerEmail(getDataFromEditext(et_email, "Invalid Email", 5));

        if(!UtilityFunctions.isValidEmail(Constants.const_usersSignupDTO.getWorkerEmail())){
            ScreenMangerSignup.isOkayStep1 = false;
            et_email.setError("Invalid Email");
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, et_email);
                isfirstRequestFocus = true;
            }
        }

        if (!UtilityFunctions.isValidName(Constants.const_usersSignupDTO.getWorkerName())) {
            ScreenMangerSignup.isOkayStep1 = false;
            et_workerName.setError("Invalid Name");
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, et_workerName);
                isfirstRequestFocus = true;
            }
        }

        Constants.const_usersSignupDTO.setWorkerPhone(getDataFromMaskText(met_phoneNumber, "Invalid Number", 12));

        try {
            Constants.const_usersSignupDTO.setWorkerPhone(Constants.const_usersSignupDTO.getWorkerPhone().replaceAll("-", ""));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!Constants.const_usersSignupDTO.getWorkerPhone().startsWith("07")) {
            met_phoneNumber.setError("Number should start from 07");
            ScreenMangerSignup.isOkayStep1 = false;
        }

        Constants.const_usersSignupDTO.setWorkerDob(et_dob.getText().toString());

        if (Constants.const_usersSignupDTO.getWorkerDob() == null) {
            ScreenMangerSignup.isOkayStep1 = false;
            Toast.makeText(signupContext, "Please set Date of Birth", Toast.LENGTH_SHORT).show();
        }
        Constants.const_usersSignupDTO.setWorkerPassword(getDataFromEditext(et_password, "Invalid Password", 6));
        if (Constants.const_usersSignupDTO.getWorkerPassword().length() < 6) {
            et_password.setError("Password min length should be 6");
            ScreenMangerSignup.isOkayStep1 = false;
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, et_password);
                isfirstRequestFocus = true;
            }
        }

        String cPass = getDataFromEditext(et_confirmPassword, "Invalid Confirm Password", 6);
        if (cPass.length() < 6) {
            et_confirmPassword.setError("Password min length should be 6");
            ScreenMangerSignup.isOkayStep1 = false;
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, et_confirmPassword);
                isfirstRequestFocus = true;
            }
        }

        if (!isGenderSelected) {
            ScreenMangerSignup.isOkayStep1 = false;
            genderSpinnerError.setVisibility(View.VISIBLE);
        }


        if (ScreenMangerSignup.isOkayStep1) {
            if (!Constants.const_usersSignupDTO.getWorkerPassword().equals(cPass)) {
                ScreenMangerSignup.isOkayStep1 = false;
                et_password.setError("Password not match");
                et_confirmPassword.setError("Password not match");
                if (!isfirstRequestFocus) {
                    UtilityFunctions.editeTextFocusReset(sv, et_password);
                    isfirstRequestFocus = true;
                }
            }
        } else {
            UtilityFunctions.orangeSnackBar((Activity) signupContext, "Please Enter Details properly", Snackbar.LENGTH_SHORT);
        }
    }

    private String getDataFromEditext(EditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            ScreenMangerSignup.isOkayStep1 = false;
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, editText);
                isfirstRequestFocus = true;
            }
        }
        return text;
    }

    private String getDataFromMaskText(MaskEditText editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromMaskWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            ScreenMangerSignup.isOkayStep1 = false;
            if (!isfirstRequestFocus) {
                UtilityFunctions.editeTextFocusReset(sv, editText);
                isfirstRequestFocus = true;
            }
        }
        return text;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.termscheckbox) {
            if (termscheckbox.isChecked()) {
                ((ScreenMangerSignup) signupContext).nextBtn.setBackgroundTintList(ContextCompat.getColorStateList(signupContext, R.color.SmartDesk_Blue));
                ((ScreenMangerSignup) signupContext).nextBtn.setEnabled(true);
            } else {
                ((ScreenMangerSignup) signupContext).nextBtn.setBackgroundTintList(ContextCompat.getColorStateList(signupContext, R.color.SmartDesk_Blue_Oppaque));
                ((ScreenMangerSignup) signupContext).nextBtn.setEnabled(false);
                ScreenMangerSignup.isOkayStep1 = false;
            }
        }
    }

    public void setSpinnerForGender() {
        try {
            List<String> spinnerArray = new ArrayList<>();
            spinnerArray.add(Constants.genderSelection);
            for (String item : Constants.genderItems)
                spinnerArray.add(item);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(signupContext, R.layout.spinner_item, spinnerArray);
            genderSpinner.setAdapter(adapter);
            genderSpinner.post(() -> {
                int height = genderSpinner.getHeight();
                genderSpinner.setDropDownVerticalOffset(height);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getSelectedSpinnerItem() {
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(Constants.genderSelection)) {
                    isGenderSelected = false;
                } else {
                    genderSpinnerError.setVisibility(View.GONE);
                    Constants.const_usersSignupDTO.setWorkerGender(parent.getItemAtPosition(position).toString());
                    isGenderSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
