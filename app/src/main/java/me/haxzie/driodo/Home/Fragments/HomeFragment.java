package me.haxzie.driodo.Home.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import me.haxzie.driodo.DrivingTests.PersistantDrivingActivity;
import me.haxzie.driodo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }

    private LinearLayout persistantTest;

    public static HomeFragment newInstance(){
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home2, container, false);
        persistantTest = v.findViewById(R.id.persistant_test);
        persistantTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PersistantDrivingActivity.class));
            }
        });
        return v;
    }

}
