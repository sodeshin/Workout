package com.example.muscletraining;

import android.content.Context;
import androidx.room.Room;

public class MuscleSingleton {
    private static MuscleDatabase instance = null;

    public static MuscleDatabase getInstance(Context context) {
        if (instance != null) {
            return instance;

        }

        instance = Room.databaseBuilder(context,
                MuscleDatabase.class, "MuscleData").build();
        return instance;
    }
}
