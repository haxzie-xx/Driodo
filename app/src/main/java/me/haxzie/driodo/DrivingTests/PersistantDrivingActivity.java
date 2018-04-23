package me.haxzie.driodo.DrivingTests;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eyalbira.loadingdots.LoadingDots;
import com.google.android.gms.maps.model.LatLng;
import com.liuguangqiang.cookie.CookieBar;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.haxzie.driodo.FreeDriveMode.FreeDriveActivity;
import me.haxzie.driodo.MapUtils;
import me.haxzie.driodo.R;

public class PersistantDrivingActivity extends AppCompatActivity {

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    private BubbleSeekBar mBubbleSeekbar;
    private TextView speedLimit;
    private int mlimit = 40;
    private Location mCurrentLocation = null;
    private LatLng mDestinationLocation = null;
    private LoadingDots mLoadingDots[];
    private ImageView mTickMars[];
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d("PERSISTANT_DRIVE", String.format("%f, %f", location.getLatitude(), location.getLongitude()));
                mLocationManager.removeUpdates(mLocationListener);
            } else {
                Log.d("PERSISTANT_DRIVE", "Location is null");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persistant_driving);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mBubbleSeekbar = findViewById(R.id.speed_limitter);
        speedLimit = findViewById(R.id.speed_limit);
        speedLimit.setText(String.valueOf(40));

        mLoadingDots = new LoadingDots[3];
        mLoadingDots[0] = findViewById(R.id.loading_dots1);
        mLoadingDots[1] = findViewById(R.id.loading_dots2);
        mLoadingDots[2] = findViewById(R.id.loading_dots3);

        mTickMars = new ImageView[3];
        mTickMars[0] = findViewById(R.id.ic_tick_1);
        mTickMars[1] = findViewById(R.id.ic_tick_2);
        mTickMars[2] = findViewById(R.id.ic_tick_3);

        mBubbleSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                setSpeedLimitText(progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                setSpeedLimitText(progress);
                mlimit = progress;
                if (progress <= 10) {
                    bubbleSeekBar.setProgress(40.0f);
                    new CookieBar.Builder(PersistantDrivingActivity.this)
                            .setTitle("Too low!")
                            .setTitleColor(R.color.white)
                            .setMessage("The coosen speed is too low, please choose above 10km/hr")
                            .setMessageColor(R.color.white)
                            .setBackgroundColor(R.color.pink)
                            .show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCurrentLocation != null && mDestinationLocation != null){
                    Intent i = new Intent(PersistantDrivingActivity.this, PersistantMapActivity.class);
                    i.putExtra("START_LAT", mCurrentLocation.getLatitude());
                    i.putExtra("START_LNG", mCurrentLocation.getLongitude());
                    i.putExtra("END_LAT", mDestinationLocation.latitude);
                    i.putExtra("END_LNG", mDestinationLocation.longitude);
                    startActivity(i);
                }

            }
        });

        startLoadingData();

    }

    private void startLoadingData() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled){
            loadingCompleted(0);
            loadingStarted(1);

            getCurrentLocation();
        }
    }

    private void getCurrentLocation(){
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;

        if (!(isGPSEnabled || isNetworkEnabled))
            new CookieBar.Builder(this)
                    .setBackgroundColor(R.color.pink)
                    .setTitleColor(R.color.white)
                    .setTitle("Location not available")
                    .show();
        else {
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null) {
            Log.d("PERSISTANT_DRIVE", String.format("getCurrentLocation(%f, %f)", location.getLatitude(),
                    location.getLongitude()));
            locationReceived(location);

        }

    }

    private void loadingCompleted(int id) {

        mLoadingDots[id].setVisibility(View.GONE);
        mTickMars[id].setVisibility(View.VISIBLE);

    }

    private void loadingStarted(int id) {

        mLoadingDots[id].setVisibility(View.VISIBLE);
        mTickMars[id].setVisibility(View.GONE);

    }

    public void locationReceived(Location location){
        this.mCurrentLocation = location;
        Log.i("CURRENT_LOCATION", String.format("CurrentLocation(%f, %f)",location.getLatitude(), location.getLongitude()));
        loadingCompleted(1);
        loadingStarted(2);
        this.mDestinationLocation = MapUtils.getRandomLocation(new LatLng(location.getLatitude(), location.getLongitude()), 10000);
        loadingCompleted(2);
        viewStartButton();

    }

    private void viewStartButton() {
        findViewById(R.id.main_loading_dots).setVisibility(View.GONE);
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
    }
    private void setSpeedLimitText(int speed) {
        if (speed <= 20) {
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.textGrey));
            speedLimit.setTextColor(getResources().getColor(R.color.textGrey));
            speedLimit.setText(String.valueOf(speed));
        } else if (speed <= 60) {
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.primaryDark));
            speedLimit.setTextColor(getResources().getColor(R.color.primaryDark));
            speedLimit.setText(String.valueOf(speed));
        } else {
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.pink));
            speedLimit.setTextColor(getResources().getColor(R.color.pink));
            speedLimit.setText(String.valueOf(speed));
        }
    }

    public void showGpsDisabledDialog(){

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Enable GPS")
                .setMessage("Please enable GPS and Location services to use this feature")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).show();
    }

}
