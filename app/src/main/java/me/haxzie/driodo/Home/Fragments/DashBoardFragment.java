package me.haxzie.driodo.Home.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import me.haxzie.driodo.DBEngine.ReadDBAsync;
import me.haxzie.driodo.DBEngine.RoomDB;
import me.haxzie.driodo.Home.Adapters.RouteListAdapter;
import me.haxzie.driodo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashBoardFragment extends Fragment {


    RecyclerView recycler;
    RouteListAdapter routeListAdapter;
    LinearLayout noItemView;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    public static  DashBoardFragment newInstance(){
        return new DashBoardFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dash_board, container, false);

        noItemView = v.findViewById(R.id.no_item_view);
        recycler = v.findViewById(R.id.recycler);
        routeListAdapter = new RouteListAdapter(this.getContext());
        recycler.setLayoutManager(new LinearLayoutManager(this.getContext().getApplicationContext()));
        recycler.setAdapter(routeListAdapter);
        new ReadDBAsync(this.getContext(), RoomDB.getInstance(this.getContext()), routeListAdapter, noItemView).execute();


        return v;
    }

}
