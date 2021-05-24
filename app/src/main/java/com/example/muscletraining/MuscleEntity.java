package com.example.muscletraining;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "MuscleData")
public class MuscleEntity {

        @PrimaryKey(autoGenerate = true) private int id;
        //カラム private->public
        public String muscleEntity;//日時
        public int menu;
        public  int num_train;//回数


        public MuscleEntity(String muscleEntity, int menu, int num_train) {

            this.muscleEntity = muscleEntity;
            this.menu = menu;
            this.num_train = num_train;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setAccessTime(String muscleEntity) {
            this.muscleEntity = muscleEntity;
        }

        public String getAccessTime() {
            return muscleEntity;
        }

        public int getMenu() {
        return menu;
    }

        public int getNum_train(){ return num_train;}

}
