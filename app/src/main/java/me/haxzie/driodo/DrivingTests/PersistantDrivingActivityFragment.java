package me.haxzie.driodo.DrivingTests;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.haxzie.driodo.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PersistantDrivingActivityFragment extends Fragment {

    public PersistantDrivingActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_persistant_driving, container, false);
    }
}
