package com.example.muscletraining;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MuscleDao {
    @Query("SELECT * FROM MuscleData")
    List<MuscleEntity> getAll();

    @Query("SELECT * FROM MuscleData WHERE id IN (:ids)")
    List<MuscleEntity> loadAllByIds(int[] ids);

    @Query("SELECT * FROM MuscleData WHERE menu = :menu_id")
    List<MuscleEntity> loadDataMenu(int menu_id);


    @Insert
    void insert(MuscleEntity muscleEntity);

    @Delete
    void delete(MuscleEntity muscleEntity);
}
