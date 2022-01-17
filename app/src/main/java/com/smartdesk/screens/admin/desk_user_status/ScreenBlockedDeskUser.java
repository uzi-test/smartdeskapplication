package com.smartdesk.screens.admin.desk_user_status;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.utility.memory.MemoryCache;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class ScreenBlockedDeskUser extends AppCompatActivity {

    private Activity context;

    //RecyclerView Variables
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ScreenBlockedDeskUser.Adapter adapter;
    List<SignupUserDTO> approvedMechanicDTOList = new ArrayList<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_blocked_users);
        context = this;
        initLoadingBarItems();
        actionBar("Blocked Desk-User");
        initIds();
        setRecyclerView();
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

    @Override
    protected void onResume() {
        super.onResume();
        showDataOnList(false);
    }

    private void initIds() {
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.SmartDesk_Editext_red), ContextCompat.getColor(context, R.color.SmartDesk_Blue));
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> showDataOnList(true), 0));
    }

    void onItemsLoadComplete() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void setRecyclerView() {
        new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
            adapter = new Adapter(approvedMechanicDTOList);
            recyclerView = UtilityFunctions.setRecyclerView((RecyclerView) findViewById(R.id.recycler_view), context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }, 0);
    }

    public void showDataOnList(Boolean isSwipe) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isSwipe)
                startAnim();
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.usersCollection).whereEqualTo("role", Constants.deskUserRole).whereEqualTo("userStatus", Constants.blockedStatus).get().
                    addOnSuccessListener(task -> {
                        onItemsLoadComplete();
                        approvedMechanicDTOList.clear();
                        if (!isSwipe)
                            stopAnim();
                        if (task.isEmpty()) {
                            findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                            List<SignupUserDTO> signupUserDTOSList = task.toObjects(SignupUserDTO.class);
                            if (signupUserDTOSList.size() > 0) {
                                for (int i = 0; i < task.size(); i++)
                                    signupUserDTOSList.get(i).setLocalDocuementID(task.getDocuments().get(i).getId());
                                findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                approvedMechanicDTOList.addAll(signupUserDTOSList);
                                adapter.notifyDataSetChanged();
                            } else {
                                findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                onItemsLoadComplete();
                approvedMechanicDTOList.clear();
                if (!isSwipe)
                    stopAnim();
                adapter.notifyDataSetChanged();
            });
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<ScreenBlockedDeskUser.Adapter.ViewHolder> {

        List<SignupUserDTO> mechanicsList;

        public Adapter(List<SignupUserDTO> mechanicsList) {
            this.mechanicsList = mechanicsList;
        }

        @NonNull
        @Override
        public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_approved_users, parent, false);
            return new Adapter.ViewHolder(view);
        }

        @Override
        public void onViewDetachedFromWindow(Adapter.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, final int position) {
            final Context innerContext = holder.itemView.getContext();
            Date registrationDate = mechanicsList.get(position).getRegistrationDate();
            String timeAgo = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
//            holder.ratingBar.setRating(UtilityFunctions.calculateRating(mechanicsList.get(position).getRatingUserCount(), mechanicsList.get(position).getRatingTotal()));
            holder.timeAgo.setText(timeAgo);
            holder.name.setText(mechanicsList.get(position).getWorkerName());
            holder.phoneNumber.setText(UtilityFunctions.getPhoneNumberInFormat(mechanicsList.get(position).getWorkerPhone()));
            picassoGetCircleImage(context, mechanicsList.get(position).getProfilePicture(), holder.profilePic, holder.profile_shimmer, R.drawable.side_profile_icon);

            holder.itemCardview.setOnClickListener(v -> {
                ScreenDeskUserDetail.deskUserDetailsScreenDTO = mechanicsList.get(position);
                UtilityFunctions.sendIntentNormal((Activity) innerContext, new Intent(innerContext, ScreenDeskUserDetail.class), false, 0);
            });
        }

        public int getItemCount() {
            return mechanicsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, phoneNumber, city, timeAgo;
            RatingBar ratingBar;
            CircleImageView profilePic;
            ShimmerFrameLayout profile_shimmer;
            LinearLayout itemCardview;

            public ViewHolder(@NonNull View view) {
                super(view);
                itemCardview = view.findViewById(R.id.cardView);
                name = view.findViewById(R.id.name);
                phoneNumber = view.findViewById(R.id.phoneNumber);
                profile_shimmer = view.findViewById(R.id.profile_shimmer);
                profilePic = view.findViewById(R.id.profilePic);
                city = view.findViewById(R.id.address);
                ratingBar = view.findViewById(R.id.rating);
                timeAgo = view.findViewById(R.id.timeAgo);
            }
        }
    }

    //======================================== Show Loading bar ==============================================
    private LinearLayout load;
    private CoordinatorLayout bg_main;
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
}