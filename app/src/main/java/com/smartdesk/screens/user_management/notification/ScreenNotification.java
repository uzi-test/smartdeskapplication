package com.smartdesk.screens.user_management.notification;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.smartdesk.R;
import com.smartdesk.constants.Constants;
import com.smartdesk.constants.FirebaseConstants;
import com.smartdesk.utility.UtilityFunctions;
import com.smartdesk.model.notification.NotificationDTO;
import com.smartdesk.model.notification.NotificationLocalDTO;
import com.smartdesk.utility.memory.MemoryCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenNotification extends AppCompatActivity {

    private Activity context;

    //RecyclerView Variables
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ScreenNotification.Adapter adapter;
    List<NotificationLocalDTO> notificationLocalDTOArrayList = new ArrayList<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        new MemoryCache().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_notification);
        context = this;
        actionBar("Notifications");
        initLoadingBarItems();
        initIds();
        setRecyclerView();
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

    void onItemsLoadComplete() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void showDataOnList(Boolean isSwipe) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isSwipe)
                startAnim();
            System.out.println(Constants.USER_DOCUMENT_ID + "#################################");
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection)
                    .whereEqualTo("documentID", Constants.USER_DOCUMENT_ID).get().
                    addOnSuccessListener(task -> {
                        onItemsLoadComplete();
                        notificationLocalDTOArrayList.clear();
                        if (!isSwipe)
                            stopAnim();
                        if (task.isEmpty()) {
                            findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            List<NotificationDTO> notificationDTOList = task.toObjects(NotificationDTO.class);
                            Collections.sort(notificationDTOList);
                            for (NotificationDTO document : notificationDTOList)
                                notificationLocalDTOArrayList.add(new NotificationLocalDTO(document.getRole(), document.getDocumentID(), document.getDate(), document.getTitle(), document.getDescription(), document.getRead(), false));
                            for (int i = 0; i < task.size(); i++)
                                notificationLocalDTOArrayList.get(i).setLocalDocumentID(task.getDocuments().get(i).getId());

                            if (notificationDTOList.size() > 0) {
                                findViewById(R.id.listEmptyText).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.listEmptyText).setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                onItemsLoadComplete();
                if (!isSwipe)
                    stopAnim();
                notificationLocalDTOArrayList.clear();
                adapter.notifyDataSetChanged();
            });
        }, 0);
    }

    public void setRecyclerView() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            adapter = new Adapter(notificationLocalDTOArrayList);
            recyclerView = UtilityFunctions.setRecyclerView(findViewById(R.id.recycler_view), context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }, 0);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        List<NotificationLocalDTO> notificationDTOList;

        public Adapter(List<NotificationLocalDTO> notificationDTOList) {
            this.notificationDTOList = notificationDTOList;
        }

        @NonNull
        @Override
        public ScreenNotification.Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.rv_item_notification, parent, false);
            return new ScreenNotification.Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ScreenNotification.Adapter.ViewHolder holder, final int position) {
            final Context innerContext = holder.itemView.getContext();
            if (!notificationDTOList.get(position).getRead())
                holder.cardView.setBackgroundTintList(ContextCompat.getColorStateList(innerContext, R.color.cardViewColor));

            holder.title.setText(notificationDTOList.get(position).getTitle());
            holder.time.setText(UtilityFunctions.getDateFormat(notificationDTOList.get(position).getDate()));
            holder.description.setText(notificationDTOList.get(position).getDescription());

            final View.OnClickListener readmore = v -> {
                if (notificationDTOList.get(position).getOpen()) {
                    notificationDTOList.get(position).setOpen(false);
                    ObjectAnimator animation = ObjectAnimator.ofInt(holder.description, "maxLines", 1);
                    animation.setDuration(100).start();
                    holder.readMoreBtn.setImageDrawable(ContextCompat.getDrawable(ScreenNotification.this, R.drawable.chevrin_down));
                    holder.readMore.setText("Read more");
                } else {
                    notificationDTOList.get(position).setOpen(true);
                    ObjectAnimator animation = ObjectAnimator.ofInt(holder.description, "maxLines", 40);
                    animation.setDuration(100).start();
                    holder.readMoreBtn.setImageDrawable(ContextCompat.getDrawable(ScreenNotification.this, R.drawable.chevron_up));
                    holder.readMore.setText("Read less");
                }
            };
            FirebaseConstants.firebaseFirestore.collection(FirebaseConstants.notificationCollection).document(notificationDTOList.get(position).getLocalDocumentID())
                    .update("read", true);
            holder.readMore.setOnClickListener(readmore);
            holder.readMoreBtn.setOnClickListener(readmore);
        }

        @Override
        public void onViewDetachedFromWindow(ScreenNotification.Adapter.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.itemView.clearAnimation();
        }

        public int getItemCount() {
            return notificationDTOList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView title, time, description, readMore;
            ImageView readMoreBtn;
            LinearLayout cardView;

            public ViewHolder(@NonNull View view) {
                super(view);
                cardView = view.findViewById(R.id.cardView);
                title = view.findViewById(R.id.notificaiton_title);
                time = view.findViewById(R.id.time);
                description = view.findViewById(R.id.description);
                readMore = view.findViewById(R.id.readmore);
                readMoreBtn = view.findViewById(R.id.readmoreArrowImage);
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
    //======================================== Show Loading bar ==============================================
}
