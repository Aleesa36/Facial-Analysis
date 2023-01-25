package com.example.apprestrictor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ItemPlace> appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        installedApps();
        RecycleAdapter adapter = new RecycleAdapter(getApplicationContext(), appData);
        recyclerView.setAdapter(adapter);

        //foreground services
        Intent serviceIntent = new Intent(this, foregroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
        //Checking for permission
        if (!hasUsageStatsPermission(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    //Getting installed apps
    public void installedApps(){
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo i : packages){
            if(pm.getLaunchIntentForPackage(i.packageName) != null ){
                String name = pm.getApplicationLabel(i).toString();
                Drawable icon = pm.getApplicationIcon(i);
                String appPackage = i.packageName;
                String stats = "1";

                //loading sharedPreferences
                SharedPreferences data = getSharedPreferences("Status", MODE_PRIVATE);
                String state = data.getString(appPackage, "");
                if(state.equals("0")){
                    stats = "0";
                } if(state.equals("1")) {
                    stats = "1";
                }
                appData.add(new ItemPlace(icon, name, appPackage, stats));
            }
        }
    }

    //Checking usage access permission
    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(),
                context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}