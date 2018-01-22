package me.haxzie.driodo;

import android.support.multidex.MultiDexApplication;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by User on 09-Aug-17.
 */

public class Driodo extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/varela_round.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
