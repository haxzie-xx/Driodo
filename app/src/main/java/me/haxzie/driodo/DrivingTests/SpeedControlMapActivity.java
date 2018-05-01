package me.haxzie.driodo.DrivingTests;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.liuguangqiang.cookie.CookieBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.grabner.circleprogress.CircleProgressView;
import me.haxzie.driodo.Data;
import me.haxzie.driodo.MapUtils;
import me.haxzie.driodo.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static me.haxzie.driodo.MapUtils.bitmapDescriptorFromVector;
import static me.haxzie.driodo.MapUtils.getRandomLocation;

public class SpeedControlMapActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener, LocationListener, GpsStatus.Listener {

    private GoogleMap mMap;
    private int routeRadius;
    private LatLng start_dest, end_dest;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    private float routeDistance;
    private SharedPreferences sharedPreferences;


    private boolean isLoadingRoute = false;
    private TextView txtRouteDistance;
    private int wayPoints, wayDistance, wayTime;
    private LatLng relocateLocation, relocateEndLocation;
    private Marker prevMarker;
    private LinearLayout distanceLayout, btnReset;
    private FloatingActionButton startBtn;
    private boolean isDriving;
    private FrameLayout driveView;
    private CircleProgressView speedometer;
    private TextView txtCurrentSpeed;
    private boolean onStartLine = false;
    private int speedLimit;
    private boolean isAlertEnabled;
    private MediaPlayer mp;
    private LocationManager mLocationManager;
    private int prevSpeed;
    private int defenciveThreshold;
    private Data data;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;
    private Context ctx;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        routeRadius = prefs.getInt("ROUTE_RADIUS", 10);
        speedLimit = prefs.getInt("SPEED_LIMIT", 60);
        defenciveThreshold  = prefs.getInt("THRESHOLD", 12);
        isAlertEnabled = prefs.getBoolean("IS_ALERT_ENABLED", true);
        this.ctx = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mp = MediaPlayer.create(this, R.raw.over_speed);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void showGpsDisabledDialog() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_control_map);
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
        distanceLayout = findViewById(R.id.distance_layout);
        startBtn = findViewById(R.id.btn_start);
        btnReset = findViewById(R.id.btn_reset);
        driveView = findViewById(R.id.drive_view);
        txtCurrentSpeed = findViewById(R.id.current_speed);
        speedometer = findViewById(R.id.speedo_meter);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (relocateLocation != null) {
                    start_dest = relocateLocation;
                    end_dest = getRandomLocation(start_dest, routeRadius * 1000);
                    startRoute();
                } else if (start_dest != null) {
                    end_dest = getRandomLocation(start_dest, routeRadius * 1000);
                    startRoute();
                }
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRouteLoading(false);
                showButtons(false);
                isDriving = true;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
    }

    private void showButtons(boolean toShow) {
        if (!toShow) {
            startBtn.setVisibility(View.GONE);
            btnReset.setVisibility(View.GONE);
            distanceLayout.setVisibility(View.GONE);
            driveView.setVisibility(View.VISIBLE);
        } else {
            startBtn.setVisibility(View.VISIBLE);
            btnReset.setVisibility(View.VISIBLE);
            distanceLayout.setVisibility(View.VISIBLE);
            driveView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(SpeedControlMapActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

        }
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
        } else {
            setCurrentLocation(location);
        }

        setSpeedData(location);
        checkStartOrEndLocation(location);

    }

    private void setSpeedData(Location location) {
        if (location.hasSpeed()) {
            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6);
            if (location.getSpeed() * 3.6 > speedLimit && (isDriving)) {
                showOverSpeedDialog();
            }
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.25f), s.length(), s.length(), 0);
            txtCurrentSpeed.setText(s);
            int cSpeed = (int) (location.getSpeed() * 3.6);
            if (isDriving) speedometer.setValue(cSpeed%100);
            if (prevSpeed > cSpeed && ((prevSpeed - cSpeed) > defenciveThreshold)) {
                new CookieBar.Builder(this)
                        .setTitle("Sudden Acceleration Alert!")
                        .setTitleColor(R.color.white)
                        .setMessage("You are moving too fast, slow Down...")
                        .setMessageColor(R.color.white)
                        .setBackgroundColor(R.color.pink)
                        .setIcon(R.drawable.ic_error_outline_white_24dp)
                        .show();
                mp.start();

            } else if (cSpeed - prevSpeed > defenciveThreshold){
                new CookieBar.Builder(this)
                        .setTitle("Sudden Breaking Alert!")
                        .setTitleColor(R.color.white)
                        .setMessage("You are Breaking too fast, take it easy...")
                        .setMessageColor(R.color.white)
                        .setBackgroundColor(R.color.pink)
                        .setIcon(R.drawable.ic_error_outline_white_24dp)
                        .show();
                mp.start();
            }

            prevSpeed = cSpeed;
        }
    }

    private void showOverSpeedDialog() {
        if (isAlertEnabled) {
            new CookieBar.Builder(this)
                    .setTitle("OverSpeed Alert!")
                    .setTitleColor(R.color.white)
                    .setMessage("You are moving too fast, slow Down...")
                    .setMessageColor(R.color.white)
                    .setBackgroundColor(R.color.pink)
                    .setIcon(R.drawable.ic_error_outline_white_24dp)
                    .show();
            mp.start();
        } else return;

    }

    private void checkStartOrEndLocation(Location location) {
        //to check the start and finishing points
        if (relocateLocation != null && relocateEndLocation != null) {
            if (isDriving) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        relocateEndLocation.latitude, relocateEndLocation.longitude, results);
                if (results[0] <= 10.0f) {
                    onStartLine = false;
                    showCompletedDialog();

                }

            } else if (!onStartLine) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        relocateLocation.latitude, relocateLocation.longitude, results);
                if (results[0] <= 100.0f) {
                    onStartLine = true;
                    Toast.makeText(this, "You are in start line", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


    public void showCompletedDialog() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.drive_complete_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.save_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        dialog.show();
    }

    /**
     * starts routing for the start dest and the end dest
     */
    private void startRoute() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(start_dest, end_dest)
                .build();
        routing.execute();
    }

    /**
     * method to shoe/hide the route loading progress bar
     *
     * @param toShow
     */
    private void showRouteLoading(boolean toShow) {
        if (toShow) {
            isLoadingRoute = true;
            findViewById(R.id.route_loader).setVisibility(View.VISIBLE);
        } else {
            isLoadingRoute = false;
            findViewById(R.id.route_loader).setVisibility(View.GONE);
        }
    }

    /**
     * set the current location and random location
     *
     * @param location
     */
    private void setCurrentLocation(Location location) {
        start_dest = new LatLng(location.getLatitude(), location.getLongitude());
        end_dest = MapUtils.getRandomLocation(start_dest, routeRadius * 1000);
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
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {
        showRouteLoading(true);
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        showRouteLoading(false);
        if (route == null)
            return;

        //Move the camera to the starting position
        CameraUpdate center = CameraUpdateFactory.newLatLng(route.get(0).getPoints().get(0));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
        mMap.moveCamera(center);
        mMap.moveCamera(zoom);

        //remove if any previous polyLines are added
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

            setRouteDistance(route.get(i).getDistanceValue());

        }

        //getting start and end of the route from polylines
        List<LatLng> routePoints = route.get(route.size() - 1).getPoints();
        wayPoints = routePoints.size();
        wayDistance = route.get(0).getDistanceValue();
        wayTime = route.get(0).getDurationValue();

        showMapDifficulty(MapUtils.getMapDifficulty(wayPoints, wayDistance, wayTime));


        //change the relocate location to start point and endingpoint
        relocateLocation = routePoints.get(0);
        relocateEndLocation = routePoints.get(routePoints.size() - 1);

        setMarkers();

    }

    private void showMapDifficulty(int mapDifficulty) {

        String title = "", message = "";

        CookieBar.Builder cb = new CookieBar.Builder(this);
        switch (mapDifficulty){
            case 1:
                cb.setBackgroundColor(R.color.primaryDark);
                title = "Easy Route";
                message = "This route has been predicted to be easy to drive";
                break;
            case 2:
                cb.setBackgroundColor(R.color.blue);
                title = "Medium Route";
                message = "This route has been predicted to be Medium difficulty to drive";
                break;
            case 3:
                cb.setBackgroundColor(R.color.pink);
                title = "Hard Route";
                message = "This route has been predicted to be hard to drive";
                break;
        }
        cb.setTitle(title)
                .setMessage(message)
                .setTitleColor(R.color.white)
                .setMessageColor(R.color.white)
                .show();
    }

    private void setMarkers() {
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(relocateLocation);
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_placeholder_blue));
        mMap.addMarker(options);

        // Remove the previous end Marker
        if (prevMarker != null)
            prevMarker.remove();
        options = new MarkerOptions();
        options.position(relocateEndLocation);
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_placeholder));
        prevMarker = mMap.addMarker(options);
    }

    private void setRouteDistance(int distanceValue) {
        routeDistance = distanceValue / 1000;
        txtRouteDistance.setText(String.valueOf(Math.round(routeDistance)) + " km");
    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
            if (relocateLocation != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(relocateLocation)
                        .zoom(15).build();
                //Zoom in and animate the camera.
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isDriving) {
            isDriving = false;
            showButtons(true);
        } else
            super.onBackPressed();
    }

    @Override
    public void onGpsStatusChanged(int i) {

    }
}
