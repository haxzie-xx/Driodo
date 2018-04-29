package me.haxzie.driodo.DBEngine;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RouteDAO {

    @Query("SELECT * FROM droute")
    List<DRoute> getAll();

    @Insert
    void insert(DRoute dRoute);

    @Delete
    void delete(DRoute droute);
}
