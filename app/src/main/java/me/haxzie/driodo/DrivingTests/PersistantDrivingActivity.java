package me.haxzie.driodo.DrivingTests;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.eyalbira.loadingdots.LoadingDots;
import com.google.android.gms.maps.model.LatLng;
import com.liuguangqiang.cookie.CookieBar;
import com.xw.repo.BubbleSeekBar;

import java.util.Random;
import java.util.logging.Logger;

import me.haxzie.driodo.R;

public class PersistantDrivingActivity extends AppCompatActivity {

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    private BubbleSeekBar mBubbleSeekbar;
    private TextView speedLimit;
    private int mlimit = 40;

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
                Log.d("PERSISTANT_DRIVE","Location is null");
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
                Intent i = new Intent(PersistantDrivingActivity.this, PersistantMapActivity.class);
                i.putExtra("START_LAT", 12.8653129);
                i.putExtra("START_LNG", 74.9241649);
                i.putExtra("END_LAT", 12.8653129);
                i.putExtra("END_LNG", 74.9241649);
                startActivity(i);
            }
        });

    }

    private void getCurrentLocation() {
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
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
            Log.d("PERSISTANT_DRIVE",String.format("getCurrentLocation(%f, %f)", location.getLatitude(),
                    location.getLongitude()));
        }
    }

    private LatLng getRandomLocation(double x0, double y0, int radius) {
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLongitude = new_x + x0;
        double foundLatitude = y + y0;
        return new LatLng(foundLatitude, foundLongitude);
    }

    private void setSpeedLimitText(int speed){
        if (speed <= 20){
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.textGrey));
            speedLimit.setTextColor(getResources().getColor(R.color.textGrey));
            speedLimit.setText(String.valueOf(speed));
        }else if(speed <= 60){
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.primaryDark));
            speedLimit.setTextColor(getResources().getColor(R.color.primaryDark));
            speedLimit.setText(String.valueOf(speed));
        }else {
            mBubbleSeekbar.setThumbColor(getResources().getColor(R.color.pink));
            speedLimit.setTextColor(getResources().getColor(R.color.pink));
            speedLimit.setText(String.valueOf(speed));
        }
    }

}
