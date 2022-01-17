package com.smartdesk.screens.desk_users_screens._home.desk_user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.model.SmartDesk.DesksSortedList;
import com.smartdesk.model.SmartDesk.NewDesk;
import com.smartdesk.model.SmartDesk.UserBookDate;
import com.smartdesk.model.signup.SignupUserDTO;
import com.smartdesk.screens.admin.desk_user_status.ScreenDeskUserDetail;
import com.smartdesk.screens.desk_users_screens._home.ScreenDeskUserHome;
import com.smartdesk.utility.UtilityFunctions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class FragmentDeskBooked extends Fragment {

    private View view;
    private Activity context;
    boolean isStop;

    //RecyclerView Variables
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    Adapter adapter;
    DesksSortedList deskListNew = new DesksSortedList();
    String searchDateString = "";

    public FragmentDeskBooked() {
    }

    public FragmentDeskBooked(Activity context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_approved, container, false);
        initIds();
        ((TextView) view.findViewById(R.id.listEmptyText)).setText("No Booked Desks Found");
        setRecyclerView();
        showDataOnList(false);
        return view;
    }

    private void initIds() {
        swipeRefreshLayout = view.findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.SmartDesk_Editext_red), ContextCompat.getColor(context, R.color.SmartDesk_Blue));
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> showDataOnList(true), 0));
    }

    void onItemsLoadComplete() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void setRecyclerView() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            adapter = new Adapter(deskListNew);
            recyclerView = UtilityFunctions.setRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view), context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }, 0);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isStop) {
            showDataOnList(false);
            isStop = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isStop = true;
    }

    public void showDataOnList(Boolean isSwipe) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isSwipe)
                ((ScreenDeskUserHome) context).startAnim();
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection).get().
                    addOnSuccessListener(task ->
                    {
                        ((ScreenDeskUserHome) context).stopAnim();
                        if (!task.isEmpty()) {
                            deskListNew.clear();
                            List<NewDesk> deskLLL = task.toObjects(NewDesk.class);
                            if (deskLLL.isEmpty()) {
                                view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            } else {

                                view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                List<NewDesk> filterData = new ArrayList<>();
                                List<String> datesList = new ArrayList<>();
                                for (int i = 0; i < deskLLL.size(); i++) {
                                    for (UserBookDate t : deskLLL.get(i).bookDate) {
                                        if (t.userDocId.equals(Constants.USER_DOCUMENT_ID)) {
                                            filterData.add(deskLLL.get(i));
                                            datesList.add(t.getDate());
                                        }
                                    }
                                }

                                if (filterData.size() > 0) {
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                    deskListNew.clear();
                                    deskListNew.addAll(filterData, datesList);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                    deskListNew.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            deskListNew.clear();
                            adapter.notifyDataSetChanged();
                        }
                        onItemsLoadComplete();
                        adapter.notifyDataSetChanged();
                    }).
                    addOnFailureListener(e ->
                    {
                        ((ScreenDeskUserHome) context).stopAnim();
                        deskListNew.clear();
                        onItemsLoadComplete();
                        if (!isSwipe)
                            ((ScreenDeskUserHome) context).stopAnim();
                        UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                        adapter.notifyDataSetChanged();
                    });
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<FragmentDeskBooked.Adapter.ViewHolder> {

        DesksSortedList deskListNew;

        public Adapter(DesksSortedList deskListNew) {
            this.deskListNew = deskListNew;
        }

        @NonNull
        @Override
        public FragmentDeskBooked.Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_for_desk_booked, parent, false);
            view.findViewById(R.id.cardView).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.tumblr_logo));
            return new FragmentDeskBooked.Adapter.ViewHolder(view);
        }

        @Override
        public void onViewDetachedFromWindow(FragmentDeskBooked.Adapter.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentDeskBooked.Adapter.ViewHolder holder, final int position) {
            final Context innerContext = holder.itemView.getContext();

            Date registrationDate = deskListNew.deskListNew.get(position).getRegistrationDate();
            String timeAgo = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
            holder.timeAgo.setText(timeAgo);

            holder.regDate.setText(UtilityFunctions.getDateFormat(deskListNew.deskListNew.get(position).getRegistrationDate()));
            holder.name.setText(deskListNew.deskListNew.get(position).getName());
            holder.deskID.setText(UtilityFunctions.getDeskID(deskListNew.deskListNew.get(position).id));
            holder.Date.setText("Booked Date: " + deskListNew.dateList.get(position));

            holder.mordetails.setOnClickListener(v -> {
                try {
                    ScreenSmartDeskBookDetailUser.deskUserDetailsScreenDTO = deskListNew.deskListNew.get(position);
                    Intent intent = new Intent(innerContext, ScreenSmartDeskBookDetailUser.class);
                    UtilityFunctions.sendIntentNormal((Activity) innerContext, intent, false, 0);
                } catch (Exception ex) {
                }
            });
        }

        public int getItemCount() {
            return deskListNew.deskListNew.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, deskID, regDate, timeAgo, Date;
            Button mordetails;

            public ViewHolder(@NonNull View view) {
                super(view);
                Date = view.findViewById(R.id.Date);
                mordetails = view.findViewById(R.id.moreDetailsbtn);
                timeAgo = view.findViewById(R.id.timeAgo);
                name = view.findViewById(R.id.name);
                deskID = view.findViewById(R.id.deskID);
                regDate = view.findViewById(R.id.regDate);
            }
        }
    }
}
