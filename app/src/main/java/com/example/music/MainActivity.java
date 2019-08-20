package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.Service.MusicService;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int count = 3;
    private boolean isEnd = false;

    private Handler mHandler = new Handler();

    private static final String TAG = "MainActivity";
    private MusicService.MyBinder mMyBinder;

    private TextView play;
    private CircleImageView img;
    private boolean isImg = false;

    private ObjectAnimator mCircleAnimator;
    private TextView reset;
    private SeekBar seekBar;

    //进度条下面的当前进度文字，将毫秒化为m:ss格式
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    //“绑定”服务的intent
    Intent MediaServiceIntent;
    private TextView nowTime;
    private TextView allTime;
    private TextView timer;
    private TextView speed;
    private  static  Handler handler;
    private Thread myThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mCircleAnimator = ObjectAnimator.ofFloat(img, "rotation", 0.0f, 360.0f);
        mCircleAnimator.setDuration(3000);//转一圈的时间
        mCircleAnimator.setInterpolator(new LinearInterpolator());//插入器，可控制加速、减速
        mCircleAnimator.setRepeatCount(-1);//重复次数，-1为不停止
        mCircleAnimator.setRepeatMode(ObjectAnimator.RESTART);//重复模式 在重复次数>=0时生效

        MediaServiceIntent = new Intent(this, MusicService.class);
        MediaServiceIntent.putExtra("aaa","qwer");//可以传一些数据

        bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        handler = new Handler(){
                public void handleMessage(Message message){
                    switch (message.what){
                        case 0:
                            if (count == 0){
                                mMyBinder.stop();
                                mCircleAnimator.end();
                                isImg = false;
                                play.setText("播放");
                                timer.setText("3s定时已结束");
                                count = 3;
                            }else {
                                timer.setText(String.valueOf(count)+"s后停止");
                            }
                            break;
                        default:
                            break;
                    }
                }

        };

    }

    private void init() {
        play = findViewById(R.id.play);
        img = findViewById(R.id.img);
        reset = findViewById(R.id.reset);
        timer = findViewById(R.id.timer);
        speed = findViewById(R.id.speed);
        nowTime = findViewById(R.id.now_time);
        allTime = findViewById(R.id.all_time);
        //监听滚动条事件
        seekBar = (SeekBar) findViewById(R.id.seek_bar);


        play.setOnClickListener(this);
        reset.setOnClickListener(this);
        timer.setOnClickListener(this);
        speed.setOnClickListener(this);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MusicService.MyBinder) service;
            seekBar.setMax(mMyBinder.getProgress());

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //这里很重要，如果不判断是否来自用户操作进度条，会不断执行下面语句块里面的逻辑，然后就会卡顿卡顿
                    if(fromUser){
                        mMyBinder.seekToPositon(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            mHandler.post(mRunnable);
            Log.d(TAG, "Service与Activity已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View v) {
        //创建Intent对象向service传参
        Intent intent = new Intent();
        //设置class
        intent.setClass(MainActivity.this, MusicService.class);
        switch (v.getId()){
            case R.id.play:
                if (!isImg ){
                    if (mCircleAnimator.isPaused()){
                        mCircleAnimator.resume();
                    }else {
                        mCircleAnimator.start();
                    }
                    isImg = true;
                    play.setText("暂停");
                    mMyBinder.play();
//                    //设置携带的数据
//                    intent.putExtra("flg","start");
//                    //启动服务
//                    startService(intent);
                }else {
                    mCircleAnimator.pause();
                    isImg = false;
                    play.setText("继续");
                    mMyBinder.pause();
//                    //设置携带的数据
//                    intent.putExtra("flg","pause");
//                    //启动服务
//                    startService(intent);
                }
                break;
            case R.id.reset:
                mCircleAnimator.end();
                isImg = false;
                play.setText("播放");
                mMyBinder.stop();
//                //设置携带的数据
//                intent.putExtra("flg","reset");
//                //启动服务
//                startService(intent);
                break;
            case R.id.timer:
                    myThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!isEnd) {
                                try {
                                    Thread.sleep(1000);
                                    count--;
                                    if (count < 1) {
                                        isEnd = true;
                                    }
                                    Message msg = new Message();
                                    msg.what = 0;
                                    handler.sendMessage(msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    myThread.start();
//                mMyBinder.timer();
//                mCircleAnimator.end();
//                isImg = false;
//                play.setText("播放");
                break;
            case R.id.speed:
                mMyBinder.speed();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //我们的handler发送是定时1000s发送的，如果不关闭，MediaPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错误
        mHandler.removeCallbacks(mRunnable);

        mMyBinder.stop();
        unbindService(mServiceConnection);
    }

    /**
     * 更新ui的runnable
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mMyBinder.getPlayPosition());
            nowTime.setText(time.format(mMyBinder.getPlayPosition()) + "s");
            allTime.setText(time.format(mMyBinder.getProgress()) + "s");
            mHandler.postDelayed(mRunnable, 1000);
        }
    };
}
