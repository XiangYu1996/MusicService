package com.example.music.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.music.R;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer = null;
    private MyBinder myBinder = new MyBinder();

    //设置服务操作的代码号
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String PAUSE = "pause";

    public MusicService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder{
        /**
         * 播放音乐
         */
        public void play() {
            if (mediaPlayer != null){
                mediaPlayer.start();
            }else {
                mediaPlayer = MediaPlayer.create(MusicService.this, R.raw.music01);
                mediaPlayer.start();
            }
        }

        /**
         * 暂停播放
         */
        public void pause() {
                mediaPlayer.pause();
        }
        public void stop() {
            if(mediaPlayer != null )
            {
                mediaPlayer.stop();
                //释放资源
                mediaPlayer.release();
                mediaPlayer = null ;
            }
        }

        /**
         * 获取歌曲长度
         **/
        public int getProgress() {
            return mediaPlayer == null ? 0:mediaPlayer.getDuration();
        }

        /**
         * 获取播放位置
         */
        public int getPlayPosition() {

            return mediaPlayer == null ? 0:mediaPlayer.getCurrentPosition();
        }
        /**
         * 播放指定位置
         */
        public void seekToPositon(int msec) {
            mediaPlayer.seekTo(msec);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        mediaPlayer=MediaPlayer.create(MusicService.this, R.raw.music01);
        mediaPlayer.setOnCompletionListener(this);

    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //获得intent中携带的标记
//        String flg = intent.getStringExtra("flg");
//        switch (flg){
//            case START:
//                if (mediaPlayer != null){
//                    mediaPlayer.start();
//                }else {
//                    mediaPlayer = MediaPlayer.create(MusicService.this, R.raw.music01);
//                    mediaPlayer.start();
//                }
//                break;
//            case STOP:
//                if(mediaPlayer != null && mediaPlayer.isPlaying())
//                {
//                    mediaPlayer.stop();
//                    //释放资源
//                    mediaPlayer.release();
//                    mediaPlayer = null ;
//                }
//                break;
//            case PAUSE:
//                    mediaPlayer.pause();
//                    break;
//
//        }
//        return super.onStartCommand(intent, flags, startId);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if (mediaPlayer != null){
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
