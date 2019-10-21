package com.ramyhelow.dailyplanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class NetworkAsyncTask extends AsyncTask<Void, Void, Boolean>{
    Context context;

    public NetworkAsyncTask(Context context){
        this.context = context;
    }

    protected Boolean doInBackground(Void... params) {
        return isNetworkAvailable();
    }
    protected void onPostExecute(Boolean hasActiveConnection) {
        Log.d(getClass().getName(),"Internet Connection State = " + hasActiveConnection);
        if(!hasActiveConnection){
            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}