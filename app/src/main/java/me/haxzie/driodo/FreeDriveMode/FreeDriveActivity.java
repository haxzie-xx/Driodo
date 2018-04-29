package me.haxzie.driodo.FreeDriveMode;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.eyalbira.loadingdots.LoadingDots;
import com.google.gson.Gson;
import com.liuguangqiang.cookie.CookieBar;

import java.util.Locale;

import me.haxzie.driodo.Data;
import me.haxzie.driodo.GpsServices;
import me.haxzie.driodo.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FreeDriveActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

    private TextView satelliteCount;
    private TextView satelliteTotal;
    private TextView status;
    private TextView accuracy;
    private TextView currentSpeed;
    private TextView maxSpeed;
    private TextView averageSpeed;
    private TextView distance;
    private Chronometer time;
    private Data data;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;
    private LocationManager mLocationManager;
    private FloatingActionButton fab;
    private LoadingDots loadingDots;
    private SharedPreferences sharedPreferences;
    private boolean firstfix;
    private MediaPlayer mp;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_drive);

        data = new Data(onGpsServiceUpdate);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mp = MediaPlayer.create(this, R.raw.over_speed);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        satelliteCount =  findViewById(R.id.satellite_count);
        satelliteTotal = findViewById(R.id.satellite_total);
//        status =  findViewById(R.id.status);
//        accuracy =  findViewById(R.id.accuracy);
        maxSpeed =  findViewById(R.id.max_speed);
        averageSpeed =  findViewById(R.id.average_speed);
        distance =  findViewById(R.id.distance);
        time =  findViewById(R.id.chrono);
        currentSpeed =  findViewById(R.id.current_speed);
        loadingDots =  findViewById(R.id.loading_dots);


        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {
                double maxSpeedTemp = data.getMaxSpeed();
                double distanceTemp = data.getDistance();
                double averageTemp;

                averageTemp = data.getAverageSpeedMotion();

                String speedUnits;
                String distanceUnits;

                speedUnits = "km/h";
                if (distanceTemp <= 1000.0) {
                    distanceUnits = "m";
                } else {
                    distanceTemp /= 1000.0;
                    distanceUnits = "km";
                }

                SpannableString s = new SpannableString(String.format("%.0f", maxSpeedTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                maxSpeed.setText(s);

                s = new SpannableString(String.format("%.0f", averageTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                averageSpeed.setText(s);

                s = new SpannableString(String.format("%.3f", distanceTemp) + distanceUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 2, s.length(), 0);
                distance.setText(s);

            }
        };

        time.setText("00:00:00");
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;

            @Override
            public void onChronometerTick(Chronometer chrono) {
                long time;
                if (data.isRunning()) {
                    time = SystemClock.elapsedRealtime() - chrono.getBase();
                    data.setTime(time);
                } else {
                    time = data.getTime();
                }

                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chrono.setText(hh + ":" + mm + ":" + ss);

                if (data.isRunning()) {
                    chrono.setText(hh + ":" + mm + ":" + ss);
                } else {
                    if (isPair) {
                        isPair = false;
                        chrono.setText(hh + ":" + mm + ":" + ss);
                    } else {
                        isPair = true;
                        chrono.setText("");
                    }
                }

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!data.isRunning()) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                    data.setRunning(true);
                    time.setBase(SystemClock.elapsedRealtime() - data.getTime());
                    time.start();
                    data.setFirstTime(true);
                    startService(new Intent(getBaseContext(), GpsServices.class));
                } else {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    data.setRunning(false);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                }
            }
        });
    }

    private void showLoading(boolean toShow) {
        if (toShow) {
            loadingDots.setVisibility(View.VISIBLE);
            currentSpeed.setVisibility(View.GONE);
            findViewById(R.id.speed_unit).setVisibility(View.GONE);
        }else {
            loadingDots.setVisibility(View.GONE);
            currentSpeed.setVisibility(View.VISIBLE);
            findViewById(R.id.speed_unit).setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        firstfix = true;
        if (!data.isRunning()) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("data", "");
            data = gson.fromJson(json, Data.class);
        }
        if (data == null) {
            data = new Data(onGpsServiceUpdate);
        } else {
            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
        }

        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showGpsDisabledDialog();
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("FreeDriveActivity", "No GPS location provider found. GPS data display will not be available.");
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog();
        }

        mLocationManager.addGpsStatusListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefsEditor.putString("data", json);
        prefsEditor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), GpsServices.class));
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    showGpsDisabledDialog();
                    return;
                }
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
                satelliteCount.setText(String.valueOf(satsUsed));
                satelliteTotal.setText(String.valueOf(satsInView));
                if (satsUsed == 0) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    data.setRunning(false);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                    fab.setVisibility(View.INVISIBLE);
                    //accuracy.setText("");
                    showLoading(true);
                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    public void showGpsDisabledDialog(){

        new AlertDialog.Builder(FreeDriveActivity.this)
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

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy()) {
            SpannableString s = new SpannableString(String.format("%.0f", location.getAccuracy()) + "m");
            s.setSpan(new RelativeSizeSpan(0.75f), s.length()-1, s.length(), 0);
            //accuracy.setText(s);

            if (firstfix){
                //status.setText("");
                fab.setVisibility(View.VISIBLE);
                firstfix = false;
            }
        }else{
            firstfix = true;
        }

        if (location.hasSpeed()) {
            showLoading(false);
            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6);
            if (location.getSpeed()*3.6 > 50){
                showOverSpeedDialog();
            }
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.25f), s.length(), s.length(), 0);
            currentSpeed.setText(s);
        }
    }

    public void resetData(){
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        time.stop();
        maxSpeed.setText("00");
        averageSpeed.setText("00");
        distance.setText("00");
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);
    }

    public void showOverSpeedDialog(){
        new CookieBar.Builder(FreeDriveActivity.this)
                .setTitle("OverSpeed Alert!")
                .setTitleColor(R.color.white)
                .setMessage("You are moving too fast, slow Down...")
                .setMessageColor(R.color.white)
                .setBackgroundColor(R.color.pink)
                .setIcon(R.drawable.ic_error_outline_white_24dp)
                .show();
        mp.start();
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
