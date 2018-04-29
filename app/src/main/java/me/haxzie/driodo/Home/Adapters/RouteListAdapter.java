package me.haxzie.driodo.Home.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteViewHolder>{


    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {

        TextView
        public RouteViewHolder(View itemView) {
            super(itemView);
        }
    }
}
