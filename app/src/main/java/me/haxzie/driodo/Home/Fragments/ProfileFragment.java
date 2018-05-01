package me.haxzie.driodo.Home.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import me.haxzie.driodo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private String phoneNumber;
    private boolean isSpeedAlertEnabled;
    private int speedLimit;
    private int routeRadius;
    private LinearLayout logout;


    private TextView alertStatus, txtSpeedLimit, txtPhoneNumber, txtRouteRadius, txtDefensiveThreshold;
    private AppCompatSeekBar sbSpeedLimit, sbRouteRadius, sbDefensiveThreshold;
    private Switch alertSwitch;
    private SharedPreferences.Editor editor;

    private String IS_ALERT_ENABLED = "IS_ALERT_ENABLED";
    private String APP_SETTINGS = "SETTINGS";
    private String SPEED_LIMIT = "SPEED_LIMIT";
    private String ROUTE_RADIUS = "ROUTE_RADIUS";
    private String THRESHOLD = "THRESHOLD";
    private int threshold;

    @Override
    public void onStart() {
        super.onStart();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        editor.commit();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        editor.commit();
        super.onPause();
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        editor = getContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).edit();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        alertSwitch = view.findViewById(R.id.overspeed_switch);
        alertStatus = view.findViewById(R.id.overspeed_status);
        txtSpeedLimit = view.findViewById(R.id.speed_limit);
        sbSpeedLimit = view.findViewById(R.id.speed_limit_seek);
        sbRouteRadius = view.findViewById(R.id.route_radius_seek);
        txtRouteRadius = view.findViewById(R.id.route_radius);
        txtDefensiveThreshold = view.findViewById(R.id.defensive_threshold);
        sbDefensiveThreshold = view.findViewById(R.id.defensive_threshold_seek);

        SharedPreferences prefs = getContext().getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        prefs = getContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        isSpeedAlertEnabled = prefs.getBoolean(IS_ALERT_ENABLED, true);
        speedLimit = prefs.getInt(SPEED_LIMIT, 50);
        routeRadius = prefs.getInt(ROUTE_RADIUS, 10);
        threshold = prefs.getInt(THRESHOLD, 12);



        //set the speed alert status
        alertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    alertStatus.setText("Enabled");
                    editor.putBoolean(IS_ALERT_ENABLED, true);
                    editor.commit();
                } else {
                    alertStatus.setText("Disabled");
                    editor.putBoolean(IS_ALERT_ENABLED, false);
                    editor.commit();
                }
            }
        });
        if (isSpeedAlertEnabled) {
            alertSwitch.setChecked(true);
        } else {
            alertSwitch.setChecked(false);
        }

        //set speed alert
        txtSpeedLimit.setText(String.valueOf(speedLimit)+"km/h");
        sbSpeedLimit.setProgress(speedLimit);
        sbSpeedLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtSpeedLimit.setText(i + " km/h");
                editor.putInt(SPEED_LIMIT, i).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //set route radius
        txtRouteRadius.setText(String.valueOf(routeRadius)+" km");
        sbRouteRadius.setProgress( (int)(routeRadius/1.5));
        sbRouteRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtRouteRadius.setText((int)(i/1.5) + " km");
                editor.putInt(ROUTE_RADIUS, (int)(i/1.5)).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtDefensiveThreshold.setText(String.valueOf(threshold)+" km/h");
        sbDefensiveThreshold.setProgress((threshold*4));
        sbDefensiveThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtDefensiveThreshold.setText(String.valueOf(i/4) + " km/h");
                editor.putInt(THRESHOLD, i/4).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

}
