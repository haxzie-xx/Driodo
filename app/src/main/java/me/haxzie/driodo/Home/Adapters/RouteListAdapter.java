package me.haxzie.driodo.Home.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import me.haxzie.driodo.DBEngine.DRoute;
import me.haxzie.driodo.DBEngine.ReadDBAsync;
import me.haxzie.driodo.R;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteViewHolder>{


    List<DRoute> routes;
    Context ctx;

    public RouteListAdapter(Context ctx, List<DRoute> routes) {
        this.ctx = ctx;
        this.routes = routes;
    }

    public RouteListAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void updateRoutes(List<DRoute> routes) {
        this.routes = routes;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_list_item, parent, false);
        return new RouteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        DRoute route = routes.get(position);
        holder.speedometer.setValue(route.speedlimit);
        holder.speedLimit.setText(String.valueOf(route.speedlimit));
        holder.drive_type.setText(route.type);
        holder.avg_speed.setText(String.valueOf(route.avg_speed)+" km/h");
        holder.date.setText(route.date);
        holder.distance.setText(String.valueOf(route.distance)+" km");
    }

    @Override
    public int getItemCount() {
        if (routes!=null)
            return routes.size();
        else return 0;
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {

        TextView speedLimit, avg_speed, drive_type, distance, date;
        CircleProgressView speedometer;

        public RouteViewHolder(View itemView) {
            super(itemView);

            speedLimit = itemView.findViewById(R.id.speed_limit);
            avg_speed = itemView.findViewById(R.id.avg_speed);
            drive_type = itemView.findViewById(R.id.drive_type);
            distance = itemView.findViewById(R.id.distance);
            date = itemView.findViewById(R.id.date);
            speedometer = itemView.findViewById(R.id.speedo_meter);
        }
    }
}
