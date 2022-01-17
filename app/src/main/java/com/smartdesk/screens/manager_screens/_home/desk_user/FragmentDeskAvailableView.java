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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.smartdesk.R;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.model.SmartDesk.NewDesk;
import com.smartdesk.screens.manager_screens._home.ScreenManagerHome;
import com.smartdesk.utility.UtilityFunctions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.smartdesk.utility.UtilityFunctions.getAddressLatLng;
import static com.smartdesk.utility.UtilityFunctions.getDeskRegDate;
import static com.smartdesk.utility.UtilityFunctions.picassoGetCircleImage;

public class FragmentDeskAvailableView extends Fragment {

    private View view;
    private Activity context;
    boolean isStop;

    //RecyclerView Variables
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    Adapter adapter;
    List<NewDesk> avaiablesDesks = new ArrayList<>();

    public FragmentDeskAvailableView() {
    }

    public FragmentDeskAvailableView(Activity context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_request, container, false);
        initIds();
        ((TextView) view.findViewById(R.id.listEmptyText)).setText("Desks are not available");
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
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter = new Adapter(avaiablesDesks);
                recyclerView = UtilityFunctions.setRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view), context);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
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
                        avaiablesDesks.clear();
                        if (!isSwipe)
                            ((ScreenManagerHome) context).stopAnim();
                        if (!task.isEmpty()) {
                            List<NewDesk> deskLLL = task.toObjects(NewDesk.class);
                            if (deskLLL.isEmpty()) {
                                view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            } else {
                                view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                if (deskLLL.size() > 0) {
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                                    avaiablesDesks.clear();
                                    avaiablesDesks.addAll(deskLLL);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                                    avaiablesDesks.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            avaiablesDesks.clear();
                            view.findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
//                                redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                onItemsLoadComplete();
                avaiablesDesks.clear();
                if (!isSwipe)
                    ((ScreenManagerHome) context).stopAnim();
                UtilityFunctions.redSnackBar(context, "No Internet!", Snackbar.LENGTH_SHORT);
                adapter.notifyDataSetChanged();
            });
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<FragmentDeskAvailableView.Adapter.ViewHolder> {

        List<NewDesk> desksList;

        public Adapter(List<NewDesk> desksList) {
            this.desksList = desksList;
        }

        @NonNull
        @Override
        public FragmentDeskAvailableView.Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_for_desk, parent, false);
            view.findViewById(R.id.cardView).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.tumblr_logo));
            return new FragmentDeskAvailableView.Adapter.ViewHolder(view);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        @Override
        public void onBindViewHolder(@NonNull FragmentDeskAvailableView.Adapter.ViewHolder holder, final int position) {
            final Context innerContext = holder.itemView.getContext();

            Date registrationDate = desksList.get(position).getRegistrationDate();
            String timeAgo = UtilityFunctions.remaingTimeCalculation(new Timestamp(new Date().getTime()), new Timestamp(registrationDate.getTime()));
            holder.timeAgo.setText(timeAgo);

            holder.regDate.setText(UtilityFunctions.getDateFormat(desksList.get(position).getRegistrationDate()));
            holder.name.setText(desksList.get(position).getName());
            holder.deskID.setText(UtilityFunctions.getDeskID(desksList.get(position).id));
            holder.city.setText(getAddressLatLng(context, desksList.get(position).getDeskLat(), desksList.get(position).getDeskLng()));
            holder.mordetails.setOnClickListener(v -> {
                try {
                    ScreenSmartDeskDetailManager.deskUserDetailsScreenDTO = desksList.get(position);
                    UtilityFunctions.sendIntentNormal((Activity) innerContext, new Intent(innerContext, ScreenSmartDeskDetailManager.class), false, 0);
                } catch (Exception ex) {
                }
            });
        }

        public int getItemCount() {
            return desksList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView name, deskID, regDate,timeAgo,city;
            Button mordetails;

            public ViewHolder(@NonNull View view) {
                super(view);
                mordetails = view.findViewById(R.id.moreDetailsbtn);
                city = view.findViewById(R.id.address);
                timeAgo = view.findViewById(R.id.timeAgo);
                name = view.findViewById(R.id.name);
                deskID = view.findViewById(R.id.deskID);
                regDate = view.findViewById(R.id.regDate);
            }
        }
    }
}
