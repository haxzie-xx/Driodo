package me.haxzie.driodo.Home.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.xw.repo.BubbleSeekBar;

import me.haxzie.driodo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private String phoneNumber;
    private boolean isSpeedAlertEnabled;
    private int speedLimit;
    private int routeRadius;


    private TextView alertStatus, txtSpeedLimit, txtPhoneNumber, txtRouteRadius;
    private AppCompatSeekBar sbSpeedLimit, sbRouteRadius;
    private Switch alertSwitch;
    private SharedPreferences.Editor editor;

    private String IS_ALERT_ENABLED = "IS_ALERT_ENABLED";
    private String APP_SETTINGS = "SETTINGS";
    private String SPEED_LIMIT = "SPEED_LIMIT";
    private String ROUTE_RADIUS = "ROUTE_RADIUS";

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
        txtPhoneNumber = view.findViewById(R.id.phone_number);
        txtSpeedLimit = view.findViewById(R.id.speed_limit);
        sbSpeedLimit = view.findViewById(R.id.speed_limit_seek);
        sbRouteRadius = view.findViewById(R.id.route_radius_seek);
        txtRouteRadius = view.findViewById(R.id.route_radius);

        SharedPreferences prefs = getContext().getSharedPreferences("USER", Context.MODE_PRIVATE);
        phoneNumber = prefs.getString("PHONE", null);
        prefs = getContext().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        isSpeedAlertEnabled = prefs.getBoolean(IS_ALERT_ENABLED, true);
        speedLimit = prefs.getInt(SPEED_LIMIT, 50);
        routeRadius = prefs.getInt(ROUTE_RADIUS, 10);

        Log.i("driodo", "p: "+phoneNumber+" alert: "+isSpeedAlertEnabled+" sl: "+speedLimit+" rr: "+routeRadius);


        //set phone number
        txtPhoneNumber.setText(phoneNumber);

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


    }

}
