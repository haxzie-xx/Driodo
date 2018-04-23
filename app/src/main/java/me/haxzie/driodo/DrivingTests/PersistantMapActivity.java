package me.haxzie.driodo.DrivingTests;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.liuguangqiang.cookie.CookieBar;

import java.util.ArrayList;
import java.util.List;

import me.haxzie.driodo.MapUtils;
import me.haxzie.driodo.R;

public class PersistantMapActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private LatLng start_dest, end_dest;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};
    private LatLng relocateLocation;
    private LinearLayout randomizeRouteBtn;
    private TextView txtRouteDistance;
    private float routeDistance;
    private Marker prevMarker;

    @Override
    protected void onStart() {
        super.onStart();
        Bundle b = getIntent().getExtras();
        start_dest = new LatLng(b.getDouble("START_LAT"), b.getDouble("START_LNG"));
        end_dest = new LatLng(b.getDouble("END_LAT"), b.getDouble("END_LNG"));
        relocateLocation = start_dest;
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
        randomizeRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end_dest = MapUtils.getRandomLocation(start_dest, 15000);
                startRoute();
            }
        });
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


        // Add a marker and move the camera
        MarkerOptions marker = new MarkerOptions()
                .position(start_dest)
                .title("Your location");
        marker.icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin));

        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start_dest));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(start_dest)
                .zoom(17).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //create the new route
        startRoute();

    }

    private void startRoute(){
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


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);


            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            routeDistance = route.get(i).getDistanceValue()/1000;
            txtRouteDistance.setText(String.valueOf(Math.round(routeDistance))+" km");
        }

        //getting start and end of the route from polylines
        List<LatLng> routePoints = route.get(route.size()-1).getPoints();
        Toast.makeText(this, "Total turns: "+routePoints.size(), Toast.LENGTH_LONG).show();
        //change the relocate location to start point
        relocateLocation = routePoints.get(0);
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(routePoints.get(0));
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_placeholder));

        mMap.addMarker(options);

        // End marker
        if (prevMarker != null)
            prevMarker.remove();

        options = new MarkerOptions();
        options.position(routePoints.get(routePoints.size()-1));
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_flag));
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
                    .zoom(17).build();
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
}
