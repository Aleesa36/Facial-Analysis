package com.example.apprestrictor;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
public class foregroundService extends Service {
    static int flag2=0, flag=0;
    String current_app="";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Starting foreground service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final String Channel_ID = "foreground service";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Channel_ID, Channel_ID,
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, Channel_ID)
                    .setContentText("Foreground Service App");
            startForeground(1001, notification.build());
        }
        startTimer();
        return START_STICKY;
    }

    //Stopping timer
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    //Starting Timer
    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                if (flag == 0) {
                    if (checkingApps(getForegroundApp())) {
                        // flag = 1 means stop loop
                        flag = 1;
                        current_app = getForegroundApp();
                        //Face detection
                        Intent intent = new Intent(getApplicationContext(), FaceDetection.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                if ((!getForegroundApp().equals("com.example.apprestricters"))  &&  flag2 == 0 ) {
                    if ((!getForegroundApp().equals(current_app))) {
                        flag = 0;
                    }
                }
                if (getForegroundApp().equals("com.example.apprestricters")) {
                    flag = 2;
                }
            }
        };
        timer.schedule(timerTask, 0, 100);}
    public void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
    //Checking for apps in SharedPreference
    public boolean checkingApps(String name){
        SharedPreferences data = getSharedPreferences("Status", MODE_PRIVATE);
        String extract = data.getString(name, "");
        return extract.equals("0");
    }

    //Fetching currently running app propcess
    private String getForegroundApp() {
        String currentApp = "NULL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UsageStatsManager usm = (UsageStatsManager)
                    this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty() && mySortedMap != null) {
                    currentApp = Objects.requireNonNull(mySortedMap.get(mySortedMap.lastKey()))
                            .getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;//}
        }
        Log.d("Current Application", currentApp);
        return currentApp;
    }

}


