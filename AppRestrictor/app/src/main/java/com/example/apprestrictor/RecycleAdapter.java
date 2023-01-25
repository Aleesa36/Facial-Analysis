package com.example.apprestrictor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {
    Context context;
    ArrayList<ItemPlace> appData;
    public RecycleAdapter(Context context,  ArrayList<ItemPlace> appData){
        this.context = context;
        this.appData = appData;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(context).inflate(R.layout.app_cardview, parent, false);
        return (new ViewHolder(viewItem));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (!appData.get(position).getPackageName().equals("com.example.apprestrictor")){
            holder.appIcon.setImageDrawable(appData.get(position).appLogo);
            holder.appInitial.setText(appData.get(position).appName);

            if(appData.get(position).getAppStatus().equals("0")){ holder.appStat.setText(R.string.State0);}
            if(appData.get(position).getAppStatus().equals("1")){ holder.appStat.setText(R.string.State1);}

            holder.appPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //app status
                    switch(appData.get(position).getAppStatus()) {
                        case "0":
                            holder.appStat.setText(R.string.State1);
                            appData.get(position).setAppStatus("1");
                            remove(appData.get(position).packageName);
                            Toast.makeText(context, appData.get(position).packageName, Toast.LENGTH_SHORT).show();
                            break;
                        case "1":
                            holder.appStat.setText(R.string.State0);
                            appData.get(position).setAppStatus("0");
                            protect(appData.get(position).packageName, "0");
                            break;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return appData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView appInitial, appStat;
        ImageView appIcon;
        RelativeLayout appPlace;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            appInitial = itemView.findViewById(R.id.appName);
            appStat = itemView.findViewById(R.id.appStatus);
            appIcon = itemView.findViewById(R.id.appLogo);
            appPlace = itemView.findViewById(R.id.relLay);
        }
    }
    //Using SharedPreferences to save
    public void protect(String packageName, String state){
        SharedPreferences statusStorage = context.getSharedPreferences("Status", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = statusStorage.edit();
        edit.putString(packageName, state);
        edit.apply();
        Toast.makeText(context, "Stored", Toast.LENGTH_SHORT).show();
    }
    //Removing app from SharedPreferences
    public void remove(String packageName){
        SharedPreferences statusStorage = context.getSharedPreferences("Status", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = statusStorage.edit();
        edit.remove(packageName);
        edit.apply();
    }
}
