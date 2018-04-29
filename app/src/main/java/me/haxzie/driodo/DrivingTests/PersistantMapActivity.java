package me.haxzie.driodo.DrivingTests;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.liuguangqiang.cookie.CookieBar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.haxzie.driodo.DBEngine.InsertDBAsync;
import me.haxzie.driodo.DBEngine.RoomDB;
import me.haxzie.driodo.Data;
import me.haxzie.driodo.GpsServices;
import me.haxzie.driodo.MapUtils;
import me.haxzie.driodo.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PersistantMapActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener, LocationListener, GpsStatus.Listener {

    private GoogleMap mMap;
    private LatLng start_dest, end_dest;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    private LatLng relocateLocation;
    private LinearLayout randomizeRouteBtn;
    private TextView txtRouteDistance;
    private float routeDistance;
    private Marker prevMarker;
    private Data data;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;
    private LocationManager mLocationManager;
    private SharedPreferences sharedPreferences;
    private MediaPlayer mp;
    private TextView avgSpeed;
    private Chronometer time;
    private TextView currentSpeed;
    private ImageButton startTest;
    private at.grabner.circleprogress.CircleProgressView speedoMeter;
    private boolean firstfix;
    private LinearLayout btnStart;
    private FrameLayout startDriveLayout;
    private CardView drivingConsole;
    private FrameLayout difficultyLayout;
    private TextView txtDifficulty;
    private boolean onStartLine = false;
    private boolean onFinishLine = false;
    private boolean isOnDrivingMode = false;
    private boolean isDriving = false;
    private int wayPoints = 0, wayDistance = 0, wayTime = 0;
    private Context ctx;
    //test
    private JSONArray dataArray;
    private LatLng relocateEndLocation;

    private int speedLimit, routeRadius;
    private Route route;
    private int avg_speed;

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        speedLimit = prefs.getInt("SPEED_LIMIT", 50);
        routeRadius = prefs.getInt("ROUTE_RADIUS", 10);
        initializeSpeedoMeter();
        this.ctx = this;

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(PersistantMapActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persistant_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtRouteDistance = findViewById(R.id.route_distance);
        randomizeRouteBtn = findViewById(R.id.btn_reset);
        btnStart = findViewById(R.id.btn_start);
        startDriveLayout = findViewById(R.id.start_drive_layout);
        drivingConsole = findViewById(R.id.drive_console);
        difficultyLayout = findViewById(R.id.difficulty_layout);
        txtDifficulty = findViewById(R.id.txt_difficulty);


        randomizeRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end_dest = MapUtils.getRandomLocation(start_dest, routeRadius*1000);
                startRoute();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomizeRouteBtn.setVisibility(View.GONE);
                difficultyLayout.setVisibility(View.GONE);
                txtDifficulty.setVisibility(View.GONE);
                slideUp(drivingConsole);
                drivingConsole.setVisibility(View.VISIBLE);
                isOnDrivingMode = true;
            }
        });

        //dataArray = new JSONArray();
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
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
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
    public void onBackPressed() {
        if (isDriving) {
            new AlertDialog.Builder(this)
                    .setTitle("Abort Session?")
                    .setTitle("All your current progress will be lost. Do you want to abort?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isDriving = false;
                            isOnDrivingMode = false;
                            slideDown(drivingConsole);
                            randomizeRouteBtn.setVisibility(View.VISIBLE);
                            startDriveLayout.setVisibility(View.VISIBLE);
                            resetData();
                        }
                    }).setNegativeButton("NO", null)
                    .setCancelable(false)
                    .show();
        } else if (isOnDrivingMode) {
            isOnDrivingMode = false;
            isDriving = false;
            slideDown(drivingConsole);
            randomizeRouteBtn.setVisibility(View.VISIBLE);
            startDriveLayout.setVisibility(View.VISIBLE);
            resetData();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), GpsServices.class));

    }

    public void showGpsDisabledDialog() {

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

    void initializeSpeedoMeter() {
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mp = MediaPlayer.create(this, R.raw.over_speed);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        avgSpeed = findViewById(R.id.average_speed);
        time = findViewById(R.id.chrono);
        currentSpeed = findViewById(R.id.current_speed);
        speedoMeter = findViewById(R.id.speedo_meter);
        startTest = findViewById(R.id.start_button);

        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {
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

                SpannableString s = new SpannableString(String.format("%.0f", averageTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                avgSpeed.setText(s);

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

        startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!data.isRunning()) {
                    isDriving = true;
                    startTest.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                    data.setRunning(true);
                    time.setBase(SystemClock.elapsedRealtime() - data.getTime());
                    time.start();
                    data.setFirstTime(true);
                    startService(new Intent(getBaseContext(), GpsServices.class));
                } else {
                    isDriving = false;
                    startTest.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    data.setRunning(false);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                }
            }
        });


    }

    private void showLoading(boolean toShow) {
        if (toShow) {
            speedoMeter.setValue(20);
            speedoMeter.spin();
            currentSpeed.setText("00");
        } else {
            speedoMeter.stopSpinning();
        }
    }

    public void resetData() {
        startTest.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
        time.stop();
        avgSpeed.setText("00");
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e("PERSISTANT_MAP", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("PERSISTANT_MAP", "Can't find style. Error: ", e);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);

    }

    private void startRoute() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start_dest, end_dest)
                .build();
        routing.execute();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        e.printStackTrace();
    }

    @Override
    public void onRoutingStart() {
        new CookieBar.Builder(this)
                .setTitle("Routing")
                .setMessage("Creating the route..")
                .setTitleColor(R.color.white)
                .setMessageColor(R.color.white)
                .setBackgroundColor(R.color.primaryDark)
                .show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (route != null)
            this.route = route.get(0);
        else{
            new CookieBar.Builder(this)
                    .setTitle("Aww Snap!")
                    .setMessage("Something went wrong")
                    .setTitleColor(R.color.white)
                    .setMessageColor(R.color.white)
                    .setBackgroundColor(R.color.pink)
                    .show();
            return;
        }

        new CookieBar.Builder(this)
                .setTitle("Routing Success!")
                .setMessage("Your Route is ready")
                .setTitleColor(R.color.white)
                .setMessageColor(R.color.white)
                .setBackgroundColor(R.color.primaryDark)
                .setIcon(R.drawable.ic_check_white_48dp)
                .show();

        CameraUpdate center = CameraUpdateFactory.newLatLng(start_dest);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mMap.moveCamera(center);
        mMap.moveCamera(zoom);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {


            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);


            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            routeDistance = route.get(i).getDistanceValue() / 1000;
            txtRouteDistance.setText(String.valueOf(Math.round(routeDistance)) + " km");
        }

        //getting start and end of the route from polylines
        List<LatLng> routePoints = route.get(route.size() - 1).getPoints();
        wayPoints = routePoints.size();
        wayDistance = route.get(0).getDistanceValue();
        wayTime = route.get(0).getDurationValue();


        //change the relocate location to start point and endingpoint
        relocateLocation = routePoints.get(0);
        relocateEndLocation = routePoints.get(routePoints.size() - 1);

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(routePoints.get(0));
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_placeholder_blue));

        mMap.addMarker(options);

        // End marker
        if (prevMarker != null)
            prevMarker.remove();


        options = new MarkerOptions();
        options.position(relocateEndLocation);
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_placeholder));
        prevMarker = mMap.addMarker(options);

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_relocate, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.relocate) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(relocateLocation)
                    .zoom(15).build();
            //Zoom in and animate the camera.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onGpsStatusChanged(int i) {

    }

    private void setCurrentLocation(Location location) {
        this.start_dest = new LatLng(location.getLatitude(), location.getLongitude());
        this.end_dest = MapUtils.getRandomLocation(start_dest, routeRadius*1000);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (start_dest == null) {
            setCurrentLocation(location);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(start_dest));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(start_dest)
                    .zoom(17).build();
            //Zoom in and animate the camera.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //create the new route
            startRoute();
        }


        //to check the start and finishing points
        if (relocateLocation != null && relocateEndLocation != null) {
            if (isDriving) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        relocateEndLocation.latitude, relocateEndLocation.longitude, results);
                if (results[0] <= 10.0f) {
                    onStartLine = false;
                    showCompletedDialog(route, (int)data.getAverageSpeed(), (int)routeDistance);

                }

            } else if (!onStartLine){
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        relocateLocation.latitude, relocateLocation.longitude, results);
                if (results[0] <= 100.0f) {
                    onStartLine = true;
                    Toast.makeText(this, "You are in start line", Toast.LENGTH_LONG).show();
                }

            }
        }

        if (location.hasAccuracy()) {
            SpannableString s = new SpannableString(String.format("%.0f", location.getAccuracy()) + "m");
            s.setSpan(new RelativeSizeSpan(0.75f), s.length() - 1, s.length(), 0);
            //accuracy.setText(s);

            if (firstfix) {
                //status.setText("");
                firstfix = false;
            }
        } else {
            firstfix = true;
        }

        if (location.hasSpeed()) {
            showLoading(false);
            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6);
            if (location.getSpeed() * 3.6 > speedLimit && (isOnDrivingMode || isDriving)) {
                showOverSpeedDialog();
            }
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.25f), s.length(), s.length(), 0);
            currentSpeed.setText(s);
        }
    }

    public void showOverSpeedDialog() {
        new CookieBar.Builder(this)
                .setTitle("OverSpeed Alert!")
                .setTitleColor(R.color.white)
                .setMessage("You are moving too fast, slow Down...")
                .setMessageColor(R.color.white)
                .setBackgroundColor(R.color.pink)
                .setIcon(R.drawable.ic_error_outline_white_24dp)
                .show();
        mp.start();
    }

    // slide the view from below itself to the current position
    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(final View view) {
//        TranslateAnimation animate = new TranslateAnimation(
//                0,                 // fromXDelta
//                0,                 // toXDelta
//                0,                 // fromYDelta
//                view.getHeight() + 50); // toYDelta
//        animate.setDuration(500);
//        animate.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                view.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        animate.setFillAfter(true);
//        view.startAnimation(animate);

        view.setVisibility(View.GONE);
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

    public void showCompletedDialog(final Route route, final int avg_speed, final int wayDistance) {
        Gson gson = new Gson();
        final String sRoute = gson.toJson(route).toString();

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.drive_complete_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.save_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new InsertDBAsync(ctx, RoomDB.getInstance(ctx), sRoute, avg_speed, wayDistance ).execute();
                finish();
            }
        });
        dialog.show();
    }
}
