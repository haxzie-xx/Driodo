package me.haxzie.driodo.DBEngine;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class DRoute {
    @PrimaryKey(autoGenerate = true)
    public int key;

    public String date;
    public int avg_speed;
    public int distance;
    public String route;

    public DRoute(String date, int avg_speed, int distance, String route) {
        this.date = date;
        this.avg_speed = avg_speed;
        this.distance = distance;
        this.route = route;
    }
}
