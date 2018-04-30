package me.haxzie.driodo.DBEngine;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;

import com.directions.route.Route;

import java.util.List;

import me.haxzie.driodo.Home.Adapters.RouteListAdapter;

public class ReadDBAsync extends AsyncTask<Void, Void, Void> {

    private Context ctx;
    private RoomDB roomdb;
    private RouteListAdapter adapter;
    private List<DRoute> rts;
    private LinearLayout noItemView;

    public ReadDBAsync(Context ctx, RoomDB roomdb, RouteListAdapter adapter, LinearLayout noItemView) {
        this.ctx = ctx;
        this.roomdb = roomdb;
        this.adapter = adapter;
        this.noItemView = noItemView;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        rts = roomdb.routeDAO().getAll();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (rts.size() > 0)
            noItemView.setVisibility(View.GONE);
        else
            noItemView.setVisibility(View.VISIBLE);

        adapter.updateRoutes(rts);
        adapter.notifyDataSetChanged();
    }
}
