package com.ramyhelow.dailyplanner.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.ramyhelow.dailyplanner.R;
import com.ramyhelow.dailyplanner.objects.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ramyhelow.dailyplanner.Utils.getCurrentDate;
import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.getUserId;


public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<Task> taskItemList = new ArrayList<>();
    private Context context;

    private Context mContext;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance()
            .getReference();


    public ListProvider(Context context, Intent intent) {
        getDatabase();
        this.context = context;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem(context, appWidgetId);
    }

    // Data for widget
    private void populateListItem(Context context, final int appWidgetId) {

        mContext = context;

        //Gets data from DB and listens for changes in DB
        mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("tasks")
                .orderByChild("date")
                .startAt("!")
                .endAt(String.valueOf(getCurrentDate()))
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    @SuppressWarnings("unchecked") Map<String, String> td = (HashMap<String, String>) dataSnapshot.getValue();
                    List<String> values = null;
                    if (td != null) {
                        values = new ArrayList<>(td.values());

                        JSONArray jsonArray = new JSONArray(values);
                        String jsonArrayStr = jsonArray.toString();

                        Type listType = new TypeToken<List<Task>>(){}.getType();
                        List<Task> taskList;
                        taskList = new Gson().fromJson(jsonArrayStr, listType);
                        taskItemList.clear();

                        final int N = taskList.size();
                        for (int i = 0; i < N; i++) {
                            Task taskItem = taskList.get(i);
                            if (taskItem.getState().equals("0")) {
                                taskItemList.add(taskItem);
                            }
                        }

                        AppWidgetManager.getInstance(mContext)
                                .notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewWidget);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return taskItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.list_row);
        Task taskItem = taskItemList.get(position);

        remoteView.setTextViewText(R.id.heading, taskItem.getTitle());
        remoteView.setTextViewText(R.id.content, taskItem.getContent());

        Intent fillInIntent = new Intent();
        remoteView.setOnClickFillInIntent(R.id.list_row, fillInIntent);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return taskItemList.size();
    }
}
