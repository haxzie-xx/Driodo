package me.haxzie.driodo;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by User on 09-Aug-17.
 */

public class Driodo extends Application {
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
