package me.haxzie.driodo.DrivingTests;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eyalbira.loadingdots.LoadingDots;
import com.google.android.gms.maps.model.LatLng;
import com.liuguangqiang.cookie.CookieBar;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import at.grabner.circleprogress.CircleProgressView;
import me.haxzie.driodo.FreeDriveMode.FreeDriveActivity;
import me.haxzie.driodo.MapUtils;
import me.haxzie.driodo.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PersistantDrivingActivity extends AppCompatActivity {


    private TextView speedLimit;
    private int mlimit = 0;
    private CircleProgressView speedMeter;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persistant_driving);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs =  getSharedPreferences("SETTINGS", MODE_PRIVATE);
        mlimit = prefs.getInt("SPEED_LIMIT", 50);

        speedLimit = findViewById(R.id.speed_limit);
        speedLimit.setText(String.valueOf(mlimit));
        speedMeter = findViewById(R.id.speed_meter);
        speedMeter.setValue(mlimit);
        speedLimit.setText(String.valueOf(mlimit));


        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PersistantDrivingActivity.this, PersistantMapActivity.class));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


}
