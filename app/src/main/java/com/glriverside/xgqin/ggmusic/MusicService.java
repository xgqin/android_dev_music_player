package com.glriverside.xgqin.ggmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service{

    MediaPlayer mMediaPlayer;

    public MusicService() {
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String data = intent.getStringExtra(MainActivity.DATA_URI);

        Uri dataUri = Uri.parse(data);

        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(getApplicationContext(), dataUri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

