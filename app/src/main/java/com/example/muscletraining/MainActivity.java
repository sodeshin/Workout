package com.example.muscletraining;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textView, finishText;
    int count = 0, goal, menu;
    boolean onoffflag = false, startflag = false;

    //arc
    private Arc arc;
    private float endAngle = 0;
    private float angleValue = 0;
    private SoundPool soundPool;
    private int soundOne, soundTwo, soundThree, soundFour, soundFive, soundSix, soundSeven, soundEight, soundNine, soundTen, soundEleven;



    private MediaPlayer mediaPlayer;
    private Button buttonStart, buttonReset, buttonFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        goal = intent.getIntExtra("KEY",0);//設定したkeyで取り出す
        menu = intent.getIntExtra("MENU",0);//複数いける


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        finishText = findViewById(R.id.text_finish);

        //効果音
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)//推奨はGameだが，bgmに対して小さすぎる
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();


        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(11)
                .build();

        // one.wav をロードしておく
        soundOne = soundPool.load(this, R.raw.a10, 1);

        // two.wav をロードしておく
        soundTwo = soundPool.load(this, R.raw.syuuryou, 1);
        soundThree = soundPool.load(this, R.raw.hajime, 1);
        soundFour = soundPool.load(this, R.raw.ganbatte, 1);
        soundFive = soundPool.load(this, R.raw.a20, 1);
        soundSix = soundPool.load(this, R.raw.atochoto, 1);
        soundSeven = soundPool.load(this, R.raw.a30, 1);
        soundEight = soundPool.load(this, R.raw.a40, 1);
        soundNine = soundPool.load(this, R.raw.a50, 1);
        soundTen = soundPool.load(this, R.raw.otsukaresamadeshita, 1);
        soundEleven = soundPool.load(this, R.raw.ganbattane, 1);


        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("debug","sampleId="+sampleId);
                Log.d("debug","status="+status);
            }
        });


        //arc
        arc = findViewById(R.id.arc);

        buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // play(ロードしたID, 左音量, 右音量, 優先度, ループ,再生速度)
                soundPool.play(soundThree, 1.0f, 1.0f, 0, 0, 1);

                //bgm
                audioPlay();

                startflag = true;
                buttonStart.setEnabled(false);
                buttonStart.setVisibility(View.GONE);//非表示（つめないのはINVISIBLE）

            }
        });

        buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer != null) {
                    audioStop();
                }

                // play(ロードしたID, 左音量, 右音量, 優先度, ループ,再生速度)
                soundPool.play(soundTwo, 1.0f, 1.0f, 0, 0, 1);

                //selectへ
                Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
                startActivity(intent);

            }
        });

//書き方混在
        buttonFinish = findViewById(R.id.button_finish);
        buttonFinish.setVisibility(View.GONE);
        MuscleDatabase db = MuscleSingleton.getInstance(getApplicationContext());
        buttonFinish.setOnClickListener(new ButtonClickListener(this, db, menu, goal));

    }


    private class ButtonClickListener implements View.OnClickListener {
        private Activity activity;
        private MuscleDatabase db;
        private  int menu;
        private  int num_train;

        private ButtonClickListener(Activity activity, MuscleDatabase db, int menu, int num_train) {
            this.activity = activity;
            this.db = db;
            this.menu = menu;
            this.num_train = num_train;
        }

        @Override
        public void onClick(View view) {
            new MainActivity.DataStoreAsyncTask(db, activity,menu,num_train).execute();

            Intent intents = new Intent(getApplicationContext(), SelectActivity.class);
            soundPool.play(soundEleven, 1.0f, 1.0f, 0, 0, 1);
            startActivity(intents);

        }
    }
    //DB操作
    private static class DataStoreAsyncTask extends AsyncTask<Void, Void, Integer> {
        private WeakReference<Activity> weakActivity;
        private MuscleDatabase db;
        private int menu;
        private int num_train;

        public DataStoreAsyncTask(MuscleDatabase db, Activity activity, int menu, int num_train) {
            this.db = db;
            weakActivity = new WeakReference<>(activity);
            this.menu = menu;
            this.num_train = num_train;
        }
        @Override
        protected Integer doInBackground(Void... params) {
            MuscleDao muscleTrainDao = db.muscleDao();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
            String str = sdf.format(timestamp);

            MuscleEntity muscleEntitytmp = new MuscleEntity(str,menu,num_train);

            muscleTrainDao.insert(muscleEntitytmp);//追加

            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            Activity activity = weakActivity.get();
            if(activity == null) {
                return;
            }
        }
    }
    //////////////

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        Sensor accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
    }

    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(startflag){
            float sensorX, sensorY, sensorZ;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                sensorX = event.values[0];
                sensorY = event.values[1];
                sensorZ = event.values[2];

                //String strTmp = " 目標： " + goal+ "回 / 現在： " + count + "回";
                //strTmp = " X： " + sensorX + "\nY:" + sensorY + "\nZ:" + sensorZ;
                //textView.setText(strTmp);

                if(menu == 1){//腹筋
                    if(sensorY > 7.8 && !onoffflag){
                        count = count + 1;
                        onoffflag = true;

                        angleValue = angleValue + (float)1/goal * 100;//パーセント
                        endAngle = angleValue*360/100;
                        AnimationArc animation = new AnimationArc(arc, (int)endAngle);
                        // アニメーションの起動期間を設定
                        //animation.setDuration(animationPeriod);//これいれると滑らかに
                        arc.startAnimation(animation);

                        if (count != (goal - 5) && count != goal) {
                            switch (count){
                                case 5:
                                    soundPool.play(soundFour, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 10:
                                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 20:
                                    soundPool.play(soundFive, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 30:
                                    soundPool.play(soundSeven, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 40:
                                    soundPool.play(soundEight, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 50:
                                    soundPool.play(soundNine, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                            }
                        }else if(count == goal){
                            startflag = false;
                            //規定回数終了
                            if (mediaPlayer != null) {
                                audioStop();
                            }

                            // play(ロードしたID, 左音量, 右音量, 優先度, ループ,再生速度)
                            soundPool.play(soundTen, 1.0f, 1.0f, 0, 0, 1);

                            //ボタン表示
                            buttonFinish.setVisibility(View.VISIBLE);
                            buttonReset.setEnabled(false);
                            finishText.setText("目標達成！お疲れ様でした");


                        }else if(count == (goal-5)){
                            if(count >= 5 && count % 10 != 0){//かぶるのを防ぐ
                                soundPool.play(soundSix, 1.0f, 1.0f, 0, 0, 1);
                            }
                        }

                    }else if(sensorY <= 3){
                        onoffflag = false;
                    }
                } else if (menu == 2) {//スクワット
                    if(sensorY < 7.5 && onoffflag){//flag1と逆
                        count = count + 1;
                        onoffflag = false;

                        angleValue = angleValue + (float)1/goal * 100;//パーセント
                        endAngle = angleValue*360/100;
                        AnimationArc animation = new AnimationArc(arc, (int)endAngle);
                        arc.startAnimation(animation);

                        if (count != (goal - 5) && count != goal) {
                            switch (count){
                                case 5:
                                    soundPool.play(soundFour, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 10:
                                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 20:
                                    soundPool.play(soundFive, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 30:
                                    soundPool.play(soundSeven, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 40:
                                    soundPool.play(soundEight, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                                case 50:
                                    soundPool.play(soundNine, 1.0f, 1.0f, 0, 0, 1);
                                    break;
                            }
                        }else if(count == goal){
                            startflag = false;
                            //規定回数終了
                            if (mediaPlayer != null) {
                                audioStop();
                            }

                            // play(ロードしたID, 左音量, 右音量, 優先度, ループ,再生速度)
                            soundPool.play(soundTen, 1.0f, 1.0f, 0, 0, 1);

                            //ボタン表示
                            buttonFinish.setVisibility(View.VISIBLE);
                            buttonReset.setEnabled(false);
                            finishText.setText("目標達成！お疲れ様でした");

                        }else if(count == (goal-5)){
                            if(count >= 5 && count % 10 != 0){//かぶるのを防ぐ
                                soundPool.play(soundSix, 1.0f, 1.0f, 0, 0, 1);
                            }
                        }

                    }else if(sensorY >= 11.5){
                        onoffflag = true;
                    }
                }

                String strTmp = " 目標： " + goal+ "回 \n 現在： " + count + "回";
                //strTmp = " X： " + sensorX + "Y:" + sensorY + "Z:" + sensorZ;
                textView.setText(strTmp);

            }
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean audioSetup(){
        boolean fileCheck = false;

        // rawにファイルがある場合
        mediaPlayer = MediaPlayer.create(this, R.raw.rash);
        fileCheck = true;


        return fileCheck;
    }

    private void audioPlay() {

        if (mediaPlayer == null) {
            // audio ファイルを読出し
            if (audioSetup()){
                //Toast.makeText(getApplication(), "Rread audio file", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplication(), "Error: read audio file", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else{
            // 繰り返し再生する場合
            mediaPlayer.stop();
            mediaPlayer.reset();
            // リソースの解放
            mediaPlayer.release();
        }

        // 再生する
        mediaPlayer.start();

        // 終了を検知するリスナー
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.d("debug","end of audio");
//                audioStop();
//            }
//        });
        // lambda
        mediaPlayer.setOnCompletionListener( mp -> {
            Log.d("debug","end of audio");
            audioStop();
        });

    }

    private void audioStop() {
        // 再生終了
        mediaPlayer.stop();
        // リセット
        mediaPlayer.reset();
        // リソースの解放
        mediaPlayer.release();

        mediaPlayer = null;
    }

    public void onBackPressed() {
        if (mediaPlayer != null) {
            audioStop();
        }

        // play(ロードしたID, 左音量, 右音量, 優先度, ループ,再生速度)
        soundPool.play(soundTwo, 1.0f, 1.0f, 0, 0, 1);

        //selectへ
        Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
        startActivity(intent);

    }
}
