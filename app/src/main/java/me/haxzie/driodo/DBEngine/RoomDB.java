package me.haxzie.driodo.DBEngine;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DRoute.class}, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    public abstract RouteDAO routeDAO();

    public static RoomDB getInstance(Context ctx) {
        return Room.databaseBuilder(ctx.getApplicationContext(), RoomDB.class, "droute").build();
    }
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }
}
