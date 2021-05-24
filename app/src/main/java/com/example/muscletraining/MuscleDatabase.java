package com.example.muscletraining;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MuscleEntity.class}, version = 1, exportSchema = false)
public abstract class MuscleDatabase extends RoomDatabase {
    public abstract MuscleDao muscleDao();
}