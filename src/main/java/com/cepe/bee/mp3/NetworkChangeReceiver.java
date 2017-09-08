package com.cepe.bee.mp3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Boolean status = NetworkUtil.getConnectivityStatusString(context);
        if(!status){
            try {
                //MainActivity.getInstace().updateTheTextView(true);
            } catch (Exception e) {
                //Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
        }else{
            try {
                //MainActivity.getInstace().updateTheTextView(false);
            } catch (Exception e) {
                //Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
        }

    }
}