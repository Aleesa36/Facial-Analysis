package com.example.apprestrictor;

import android.graphics.drawable.Drawable;

public class ItemPlace {
    Drawable appLogo;
    String appName, appStatus, packageName;

    ItemPlace(Drawable appLogo, String appName, String packageName, String appStatus){
        this.appLogo = appLogo;
        this.appName = appName;
        this.appStatus = appStatus;
        this.packageName = packageName;

    }
    //getters
    public String getPackageName(){return packageName;}
    public String getAppStatus(){return appStatus;}
    public String getAppName(){return appName;}

    //setters
    public void setAppStatus(String stats){ this.appStatus = stats;}
}
