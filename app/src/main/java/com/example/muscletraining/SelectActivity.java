package com.example.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.content.res.AssetFileDescriptor;
import android.widget.Toast;
import android.os.Process;
import android.media.AudioAttributes;

import android.widget.ImageButton;

import java.util.Locale;

public class SelectActivity extends AppCompatActivity {
    private TextView textView,textMenu;
    private int transValue = 0;

    private MediaPlayer mediaPlayer;

    private SoundPool soundPool;
    private int soundOne, soundTwo;
    private int menu;
    private boolean menuFlag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

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
                .setMaxStreams(10)
                .build();

        // one.wav をロードしておく
        soundOne = soundPool.load(this, R.raw.junbiha, 1);
        soundTwo = soundPool.load(this, R.raw.yomikomikanryou, 1);


        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("debug","sampleId="+sampleId);
                Log.d("debug","status="+status);
            }
        });

        //bgm
        audioPlay();

        Button mainActivitySwitchButton = (Button)findViewById(R.id.button_train);
        mainActivitySwitchButton.setEnabled(false);
        mainActivitySwitchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("KEY", transValue);//第一引数key、第二引数渡したい値
                intent.putExtra("MENU", menu);
                if (mediaPlayer != null){
                    audioStop();
                }

                //音が少し変
                soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);

                startActivity(intent);
            }
        });
        Button recordActivitySwitchButton = (Button)findViewById(R.id.button_check);
        recordActivitySwitchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                if (mediaPlayer != null){
                    audioStop();
                }
                //soundPool.play(soundTwo, 1.0f, 1.0f, 0, 0, 1);
                Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
                startActivity(intent);
            }
        });


        textView = findViewById(R.id.text_num);

        // SeekBar
        SeekBar seekBar = findViewById(R.id.seekBar);
        // 初期値
        seekBar.setProgress(10);
        // 最大値
        seekBar.setMax(100);
        textView.setText("トレーニング回数：10回");
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    //ツマミがドラッグされると呼ばれる
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // この場合、Locale.USが汎用的に推奨される
                        String str = String.format(Locale.US, "トレーニング回数：%d 回",progress);
                        textView.setText(str);
                        transValue = progress;

                        if(transValue == 0 || !menuFlag){
                            mainActivitySwitchButton.setEnabled(false);
                        }else {
                            mainActivitySwitchButton.setEnabled(true);
                        }
                    }

                    //ツマミがタッチされた時に呼ばれる
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    //ツマミがリリースされた時に呼ばれる
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                });

        //image button
        ImageButton imageButton_hukkin = findViewById(R.id.image_button1);
        ImageButton imageButton_sukuwatto = findViewById(R.id.image_button2);
        textMenu = findViewById(R.id.text_menu);
        menu = 0;
        menuFlag = false;

        imageButton_hukkin.setOnClickListener( v ->  {
            textMenu.setText("メニュー：腹筋");
            menu = 1;
            menuFlag = true;

            if(transValue == 0 || !menuFlag){
                mainActivitySwitchButton.setEnabled(false);
            }else {
                mainActivitySwitchButton.setEnabled(true);
            }
            //imageButton_hukkin.setPressed(true);
            //imageButton_sukuwatto.setPressed(false);
        });

        imageButton_sukuwatto.setOnClickListener( v ->  {
            textMenu.setText("メニュー：スクワット");
            menu = 2;
            menuFlag = true;

            if(transValue == 0 || !menuFlag){
                mainActivitySwitchButton.setEnabled(false);
            }else {
                mainActivitySwitchButton.setEnabled(true);
            }
            //imageButton_sukuwatto.setPressed(true);
            //imageButton_hukkin.setPressed(false);

        });

    }


    private boolean audioSetup(){
        boolean fileCheck = false;

        // rawにファイルがある場合
        mediaPlayer = MediaPlayer.create(this, R.raw.winer);
        // 音量調整を端末のボタンに任せる
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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

    @Override
    public void onBackPressed() {
        if (mediaPlayer != null) {
            audioStop();
        }
        //ホーム画面に戻るインテントを起動
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //ホーム画面に移動
        SelectActivity.this.startActivity(homeIntent);
        //タスクキル（残ってると音が被ったりする）
        Process.killProcess(Process.myPid());
    }
}