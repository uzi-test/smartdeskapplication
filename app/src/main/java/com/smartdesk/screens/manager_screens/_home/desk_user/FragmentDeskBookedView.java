package com.smartdesk.screens.manager_screens._home.desk_user;

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
import com.smartdesk.model.SmartDesk.NewDesk;
import com.smartdesk.model.SmartDesk.UserBookDate;
import com.smartdesk.screens.manager_screens._home.ScreenManagerHome;
import com.smartdesk.utility.UtilityFunctions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.smartdesk.utility.UtilityFunctions.getAddressLatLng;
import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class FragmentDeskBookedView extends Fragment {

    private View view;
    private Activity context;
    boolean isStop;

    //RecyclerView Variables
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    Adapter adapter;
    List<NewDesk> bookedDesks = new ArrayList<>();

    public FragmentDeskBookedView() {
    }

    public FragmentDeskBookedView(Activity context) {
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
            adapter = new Adapter(bookedDesks);
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
                ((ScreenManagerHome) context).startAnim();
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.smartDeskCollection).get()
                    .addOnSuccessListener(task -> {
                        onItemsLoadComplete();
                        bookedDesks.clear();
                        if (!isSwipe)
                            ((ScreenManagerHome) context).stopAnim();
                        if (!task.isEmpty()) {
                            List<NewDesk> deskLLL = task.toObjects(NewDesk.class);
                            if (deskLLL.isEmpty()) {
                                bookedDesks.clear();
                                view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            } else {
                                view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);

                                List<NewDesk> filterData = new ArrayList<>();
                                List<String> datesList = new ArrayList<>();
                                for (int i = 0; i < deskLLL.size(); i++) {
                                    for (UserBookDate t : deskLLL.get(i).bookDate) {
                                        filterData.add(deskLLL.get(i));
                                        datesList.add(t.getDate());
                                        break;
                                    }
                                }

                                if (filterData.size() > 0) {
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                    bookedDesks.clear();
                                    bookedDesks.addAll(filterData);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    bookedDesks.clear();
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            bookedDesks.clear();
                            view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                onItemsLoadComplete();
                bookedDesks.clear();
                if (!isSwipe)
                    ((ScreenManagerHome) context).stopAnim();
                UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                adapter.notifyDataSetChanged();
            });
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<FragmentDeskBookedView.Adapter.ViewHolder> {

        List<NewDesk> deskList;

        public Adapter(List<NewDesk> deskList) {
            this.deskList = deskList;
        }

        @NonNull
        @Override
        public FragmentDeskBookedView.Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_for_desk, parent, false);
            view.findViewById(R.id.cardView).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.whatsapp_green_dark));
            return new FragmentDeskBookedView.Adapter.ViewHolder(view);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentDeskBookedView.Adapter.ViewHolder holder, final int position) {
            final Context innerContext = holder.itemView.getContext();

            Date registrationDate = deskList.get(position).getRegistrationDate();
            String timeAgo = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
            holder.timeAgo.setText(timeAgo);

            holder.regDate.setText(UtilityFunctions.getDateFormat(deskList.get(position).getRegistrationDate()));
            holder.name.setText(deskList.get(position).getName());
            holder.deskID.setText(UtilityFunctions.getDeskID(deskList.get(position).id));
            holder.city.setText(getAddressLatLng(context, deskList.get(position).getDeskLat(), deskList.get(position).getDeskLng()));
            holder.mordetails.setOnClickListener(v -> {
                try {
                    ScreenSmartDeskDetailBookManager.deskUserDetailsScreenDTO = deskList.get(position);
                    UtilityFunctions.sendIntentNormal((Activity) innerContext, new Intent(innerContext, ScreenSmartDeskDetailBookManager.class), false, 0);
                } catch (Exception ex) {
                }
            });
        }

        public int getItemCount() {
            return deskList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, city, timeAgo;
            TextView deskID, regDate;
            Button mordetails;

            public ViewHolder(@NonNull View view) {
                super(view);
                name = view.findViewById(R.id.name);
                city = view.findViewById(R.id.address);
                timeAgo = view.findViewById(R.id.timeAgo);
                mordetails = view.findViewById(R.id.moreDetailsbtn);
                timeAgo = view.findViewById(R.id.timeAgo);
                name = view.findViewById(R.id.name);
                deskID = view.findViewById(R.id.deskID);
                regDate = view.findViewById(R.id.regDate);
            }
        }
    }
}
