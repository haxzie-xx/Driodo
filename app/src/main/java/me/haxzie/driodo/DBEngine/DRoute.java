package me.haxzie.driodo.DBEngine;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class DRoute {
    @PrimaryKey(autoGenerate = true)
    public int key;

    public String type;
    public int speedlimit;
    public String date;
    public int avg_speed;
    public int distance;
    public String route;

    public DRoute(String type, String date, int avg_speed, int distance, int speedlimit, String route) {
        this.type = type;
        this.date = date;
        this.avg_speed = avg_speed;
        this.distance = distance;
        this.route = route;
        this.speedlimit = speedlimit;
    }
}
