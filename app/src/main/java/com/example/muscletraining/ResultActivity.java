package com.example.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    private TextView textView, textMax;
    private SoundPool soundPool;
    private int soundOne, soundTwo, soundThree, soundFour;
    private MediaPlayer mediaPlayer;

    private Button buttonHome, buttonLoad;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //BGM
        audioPlay();


        //グラフ描画
        mChart = findViewById(R.id.barChart);


        ///////////////////////////
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
                .setMaxStreams(10)
                .build();

        // one.wav をロードしておく
        soundOne = soundPool.load(this, R.raw.ganbattane, 1);
        soundTwo = soundPool.load(this, R.raw.otsukaresamadeshita, 1);


        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("debug","sampleId="+sampleId);
                Log.d("debug","status="+status);
            }
        });

        //soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);

        /////////////////////////////

        buttonHome = findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioStop();

                //selectへ
                Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
                startActivity(intent);

            }
        });
        Button buttonHukin = findViewById(R.id.button_hukin);
        Button buttonSukuwato= findViewById(R.id.button_sukuwato);
        TextView textData = findViewById(R.id.text_data);
        MuscleDatabase db = MuscleSingleton.getInstance(getApplicationContext());
        buttonHukin.setOnClickListener(new ButtonClickListener(this, db, textData, 1));
        buttonSukuwato.setOnClickListener(new ButtonClickListener(this, db, textData,2));

        textMax = findViewById(R.id.text_max);


    }

    private class ButtonClickListener implements View.OnClickListener {
        private Activity activity;
        private MuscleDatabase db;
        private TextView tv;
        private int menu;

        private ButtonClickListener(Activity activity, MuscleDatabase db, TextView tv, int menu) {
            this.activity = activity;
            this.db = db;
            this.tv = tv;
            this.menu = menu;
        }

        @Override
        public void onClick(View view) {
            new DataStoreAsyncTask(db, activity, tv, menu).execute();

        }

    }

    private class DataStoreAsyncTask extends AsyncTask<Void, Void, Integer> {
        private WeakReference<Activity> weakActivity;
        private MuscleDatabase db;
        private TextView textView;
        private StringBuilder sb;
        private int menu;
        private int[] buff;

        public DataStoreAsyncTask(MuscleDatabase db, Activity activity, TextView textView, int menu) {
            this.db = db;
            weakActivity = new WeakReference<>(activity);
            this.textView = textView;
            this.menu = menu;
        }
        @Override
        protected Integer doInBackground(Void... params) {
            MuscleDao muscleTrainDao = db.muscleDao();

            sb = new StringBuilder();
            List<MuscleEntity> atList = muscleTrainDao.loadDataMenu(menu);//

            int buff_length = atList.size();
            buff = new int[buff_length];
            int i =0;
            for (MuscleEntity at: atList) {
                sb.append(at.getAccessTime()).append(" ").append(at.getMenu()).append(" ").append(at.getNum_train()).append("\n");
                buff[i] = at.getNum_train();
                i++;
                //ひとまず回数だけ渡す（ここ改善の余地あり）
            }

            /*
            //delete
            for (MuscleEntity at: atList) {
                muscleTrainDao.delete(at);
            }
            */

            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            Activity activity = weakActivity.get();
            if(activity == null) {
                return;
            }
            //textView.setText(sb.toString());//一応確保してある

            // add data
            setData(buff);
            mChart.animateX(0);

            int max = 0;
            for(int i = 0; i < buff.length; i++){
                if(buff[i] > max){
                    max = buff[i];
                }
            }
            textMax.setText("これまでの最高："+ max + "回");//

        }
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
        //selectへ
        Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
        startActivity(intent);

    }

    private void setData(int[] num_train) {
        // Entry()を使ってLineDataSetに設定できる形に変更してarrayを新しく作成

        ArrayList<Entry> values = new ArrayList<>();
        //int data[] = {22, 11, 33, 67, 100, 83};



        //for (int i = 0; i < data.length; i++) {
         //   values.add(new Entry(i, data[i], null, null));
        //}

        for (int i = 0; i < num_train.length; i++) {
            values.add(new Entry(i, num_train[i], null, null));
        }

        // Grid背景色
        mChart.setDrawGridBackground(true);

        // no description text
        mChart.getDescription().setEnabled(false);//消す
        //mChart.getDescription().setText("aa");

        // Grid縦軸を破線
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(1);
        //xAxis.setAxisMaximum(num_train.length);
        //消した
        xAxis.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        // Y軸最大最小設定
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        // Grid横軸を破線
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);





        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {

            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "回数");

            set1.setDrawIcons(false);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(0f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            set1.setFillColor(Color.BLUE);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData lineData = new LineData(dataSets);

            // set data
            mChart.setData(lineData);
        }
    }

}