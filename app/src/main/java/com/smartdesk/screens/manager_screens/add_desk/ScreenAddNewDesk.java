package com.smartdesk.screens.manager_screens.add_desk;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.databinding.ScreenAddNewDeskBinding;
import com.smartdesk.model.SmartDesk.NewDesk;
import com.smartdesk.model.fcm.Data;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.screens.user_management.login.ScreenLogin;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.utility.library.CustomEditext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartdesk.utility.UtilityFunctions.getDeskID;

public class ScreenAddNewDesk extends AppCompatActivity {

    private ScreenAddNewDeskBinding binding;
    private Activity context;

    private boolean isOkay;
    private NewDesk deskItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ScreenAddNewDeskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        actionBar("Add New Desk");

        UtilityFunctions.setupUI(findViewById(R.id.parent), this);

        setSpinner(binding.wirelessCharging, Constants.wirelessChargingSelection, Constants.yesNo);
        setSpinner(binding.bluetoothConnection, Constants.bluetoothSelection, Constants.yesNo);
        setSpinner(binding.builtinSpeaker, Constants.builtinSpeakerSelection, Constants.yesNo);
        setSpinner(binding.groupUser, Constants.groupUserSelection, Constants.groupUserOptions);
        getSelectedSpinnerItem();
        deskItem = new NewDesk();
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

    private String getDataFromEditext(CustomEditext editText, String errorMSG, int minimumLength) {
        String text = "";
        try {
            text = UtilityFunctions.getStringFromEditTextWithLengthLimit(editText, minimumLength);
        } catch (NullPointerException ex) {
            editText.setError(errorMSG);
            isOkay = false;
        }
        return text;
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
        if (anim != null)
            anim.end();
        binding.loadingView.setVisibility(View.GONE);
        binding.bgMain.setAlpha((float) 1);
        isLoad = false;
    }
    //======================================== Show Loading bar ==============================================

    private boolean isWirelessChargingSelected;
    private boolean isBuiltinSpearkSelected;
    private boolean isBluetoonSelected;
    private boolean isGroupUserSelected;

    public void setSpinner(Spinner spinner, String nameSelection, String[] options) {
        try {
            List<String> spinnerArray = new ArrayList<>();
            spinnerArray.add(nameSelection);
            for (String item : options)
                spinnerArray.add(item);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, spinnerArray);
            spinner.setAdapter(adapter);
            spinner.post(() -> {
                int height = spinner.getHeight();
                spinner.setDropDownVerticalOffset(height);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getSelectedSpinnerItem() {
        binding.wirelessCharging.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(Constants.wirelessChargingSelection)) {
                    isWirelessChargingSelected = false;
                } else {
                    binding.wirelessError.setVisibility(View.GONE);
                    isWirelessChargingSelected = true;
                    deskItem.wirelessCharging = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.bluetoothConnection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(Constants.bluetoothSelection)) {
                    isBluetoonSelected = false;
                } else {
                    binding.bluetoothError.setVisibility(View.GONE);
                    isBluetoonSelected = true;
                    deskItem.bluetoothConnection = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.builtinSpeaker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(Constants.builtinSpeakerSelection)) {
                    isBuiltinSpearkSelected = false;
                } else {
                    binding.speakerError.setVisibility(View.GONE);
                    isBuiltinSpearkSelected = true;
                    deskItem.builtinSpeaker = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.groupUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(Constants.groupUserSelection)) {
                    isGroupUserSelected = false;
                } else {
                    binding.groupUserError.setVisibility(View.GONE);
                    isGroupUserSelected = true;
                    deskItem.groupUser = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void addNewDeskBtn(View view) {
        boolean isOkay = true;

        deskItem.name = getDataFromEditext(binding.smartDeskname, "Invalid Name", 3);

        if (deskItem.name.length() < 3) {
            binding.smartDeskname.setError("Invalid Name");
            isOkay = false;
        }

        if (!isWirelessChargingSelected) {
            isOkay = false;
            binding.wirelessError.setVisibility(View.VISIBLE);
        }
        if (!isBluetoonSelected) {
            isOkay = false;
            binding.bluetoothError.setVisibility(View.VISIBLE);
        }
        if (!isBuiltinSpearkSelected) {
            isOkay = false;
            binding.speakerError.setVisibility(View.VISIBLE);
        }
        if (!isGroupUserSelected) {
            isOkay = false;
            binding.groupUserError.setVisibility(View.VISIBLE);
        }

        if (isOkay) {

            deskItem.setDeskLat(Constants.const_lat + 0.00030);
            deskItem.setDeskLng(Constants.const_lng + 0.00030);

            startAnim();
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection).add(deskItem).addOnCompleteListener(task1 -> {
                stopAnim();
                if (task1.isSuccessful()) {
                    deskItem.docID = task1.getResult().getId();
                    deskItem.id = getDeskID(task1.getResult().getId());
                    FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection).document(task1.getResult().getId())
                            .update("id", deskItem.id, "docID", deskItem.docID);
                    UtilityFunctions.sendFCMMessage(context, new Data(FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()).getTime(), "Add New desk", deskItem.name + " Desk Added", "new smart desk has been added"));
                    UtilityFunctions.saveNotficationCollection(new NotificationDTO(Constants.adminRole, FirebaseConstants.adminDocumentID, new Timestamp(new Date().getTime()), deskItem.name + " Desk Added", "new smart desk has been added", false));

                    UtilityFunctions.alertNoteWithOkButton(context, "Add Desk", "Smart Desk has been added successfully", Gravity.CENTER, R.color.whatsapp_green_dark, R.color.white, true, false, null);
                } else {
                    UtilityFunctions.redSnackBar(context, "Desk not Added!", Snackbar.LENGTH_SHORT);
                }
            }).addOnFailureListener(e -> {
                stopAnim();
                UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
            });
        }
    }
}