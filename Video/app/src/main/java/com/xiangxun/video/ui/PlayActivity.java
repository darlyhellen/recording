package com.xiangxun.video.ui;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.xiangxun.video.R;


/**
 * @Author maimingliang@gmail.com
 * <p/>
 * Created by maimingliang on 2016/9/25.
 */
public class PlayActivity extends Activity implements OnPreparedListener, OnErrorListener {


    private VideoView videoView;

    private MediaController mediaco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        videoView = (VideoView) findViewById(R.id.videoview);
        mediaco = new MediaController(this);
        String videoPath = getIntent().getStringExtra("path");
        videoView.setVideoPath(videoPath);
        videoView.setMediaController(mediaco);
        videoView.setOnPreparedListener(this);
        videoView.setOnErrorListener(this);

        mediaco.setMediaPlayer(videoView);
        //让VideiView获取焦点
        videoView.requestFocus();

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
    }
}
