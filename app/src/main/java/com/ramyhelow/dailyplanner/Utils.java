package com.ramyhelow.dailyplanner;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import com.ramyhelow.dailyplanner.widget.WidgetProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static FirebaseDatabase mDatabase;

    private static int mCurrentDate;

    private static int mTomorrowDate;

    private static int mOtherTimeValue;


    // Gets FirebaseDatabase with offline data persistence
    public static void getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        } else {
            Log.v("Firebase", "Firebase DB is null");
        }
    }

    public static void updateWidget(Context context) {
        ComponentName name = new ComponentName(context, WidgetProvider.class);
        int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(ids, R.id.listViewWidget);
    }

    // Returns ID of logged in user
    public static String getUserId() {
        String mUserId = "0";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserId = user.getUid();
        } else {
            Log.v("Auth", "User ID is null");
        }
        return mUserId;
    }

    public static String formatDate(String dbDate) {

        DateFormat originalFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        DateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        Date date;

        try {
            date = originalFormat.parse(dbDate);
        } catch (ParseException e){
            return null;
        }
        String dateString = targetFormat.format(date);
        return dateString;
    }

    // Returns current date
    // Used to filter tasks
    public static int getCurrentDate() {
        if (mCurrentDate == 0) {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            Date dateRepresentation = c.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            String dateString = sdf.format(dateRepresentation);
            mCurrentDate = Integer.parseInt(dateString);
        } else {
            Log.v("DateUtil", "getCurrentDate is null");
        }
        return mCurrentDate;
    }

    public static boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }

    // Returns current date and time
    // Used to create unique IDs for tasks
    public static String getCurrentDateAndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String mCurrentDateAndTime = sdf.format(new Date());
            return mCurrentDateAndTime;
    }

    public static class DateUtil
    {
        public static Date addDays(Date date, int days)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, days);
            return cal.getTime();
        }
    }

    // Returns tomorrow date
    // Used to filter tasks
    public static int getTomorrowDate() {
        String sourceDate = String.valueOf(getCurrentDate());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date myDate;

        try {
            myDate = sdf.parse(sourceDate);
            myDate = DateUtil.addDays(myDate, 1);
            String dateString = sdf.format(myDate);
            mTomorrowDate = Integer.parseInt(dateString);

        } catch (ParseException e){
            Log.v("DATE", "getTomorrowDate parsing error");
        }

        return mTomorrowDate;
    }

    // Returns current date + 2 days
    // Used to filter tasks
    public static int getOtherTimeValue() {
        String sourceDate = String.valueOf(getCurrentDate());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date myDate;

        try {
            myDate = sdf.parse(sourceDate);
            myDate = DateUtil.addDays(myDate, 2);
            String dateString = sdf.format(myDate);
            mOtherTimeValue = Integer.parseInt(dateString);

        } catch (ParseException e){
            Log.v("DATE", "getOtherTimeValue parsing error");
        }
        return mOtherTimeValue;
    }
}
