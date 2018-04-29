package me.haxzie.driodo.DBEngine;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InsertDBAsync extends AsyncTask<Void, Void, Void> {
    private Context ctx;
    private RoomDB roomdb;
    private String date, route;
    private int avg_speed, distance;

    public InsertDBAsync(Context ctx, RoomDB roomdb, String route, int avg_speed, int distance) {
        this.ctx = ctx;
        this.roomdb = roomdb;
        this.date = date;
        this.route = route;
        this.avg_speed = avg_speed;
        this.distance = distance;
        this.date = new SimpleDateFormat("dd/MM/yy hh:mm a").format(Calendar.getInstance().getTime());

    }

    @Override
    protected Void doInBackground(Void... voids) {

        DRoute routeToAdd = new DRoute(date, avg_speed, distance, route);
        roomdb.routeDAO().insert(routeToAdd);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i("driodo", "Item Inserted");
    }
}
