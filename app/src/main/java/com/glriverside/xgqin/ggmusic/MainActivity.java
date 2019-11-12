package com.glriverside.xgqin.ggmusic;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ContentResolver mContentResolver;
    private ListView mPlaylist;
    private MediaCursorAdapter mCursorAdapter;

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " = ? " +
            " AND " + MediaStore.Audio.Media.MIME_TYPE + " LIKE ? ";
    private final String[] SELECTION_ARGS = {
            Integer.toString(1),
            "audio/mpeg"
    };

    public static final int UPDATE_PROGRESS = 1;
    public static final String DATA_URI = "com.glriverside.xgqin.ggmusic.DATA_URI";
    public static final String TITLE = "com.glriverside.xgqin.ggmusic.TITLE";
    public static final String ARTIST = "com.glriverside.xgqin.ggmusic.ARTIST";
    public static final String ACTION_MUSIC_START = "com.glriverside.xgqin.ggmusic.ACTION_MUSIC_START";
    public static final String ACTION_MUSIC_STOP = "com.glriverside.xgqin.ggmusic.ACTION_MUSIC_STOP";

    private static final String TAG = MainActivity.class.getSimpleName();

    private Boolean mPlayStatus = true;
    private ImageView ivPlay;
    private BottomNavigationView navigation;
    private TextView tvBottomTitle;
    private TextView tvBottomArtist;
    private ImageView ivAlbumThumbnail;

    private ProgressBar pbProgress;

    private MediaPlayer mMediaPlayer = null;

    private ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cursor = mCursorAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(i)) {

                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                Long albumId = cursor.getLong(albumIdIndex);
                String data = cursor.getString(dataIndex);

                Uri dataUri = Uri.parse(data);

                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(MainActivity.this, dataUri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                navigation.setVisibility(View.VISIBLE);

                if (tvBottomTitle != null) {
                    tvBottomTitle.setText(title);
                }
                if (tvBottomArtist != null) {
                    tvBottomArtist.setText(artist);
                }

                Uri albumUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        albumId);

                cursor = mContentResolver.query(
                        albumUri,
                        null,
                        null,
                        null,
                        null
                );

                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int albumArtIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                    String albumArt = cursor.getString(albumArtIndex);
                    Glide.with(MainActivity.this).load(albumArt).into(ivAlbumThumbnail);
                    cursor.close();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaylist = findViewById(R.id.lv_playlist);

        mContentResolver = getContentResolver();
        mCursorAdapter = new MediaCursorAdapter(MainActivity.this);
        mPlaylist.setAdapter(mCursorAdapter);

        navigation = findViewById(R.id.navigation);
        LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_media_toolbar, navigation, true);

        ivPlay = navigation.findViewById(R.id.iv_play);
        tvBottomTitle = navigation.findViewById(R.id.tv_bottom_title);
        tvBottomArtist = navigation.findViewById(R.id.tv_bottom_artist);
        ivAlbumThumbnail = navigation.findViewById(R.id.iv_thumbnail);
        pbProgress = navigation.findViewById(R.id.progress);

        if (ivPlay != null) {
            ivPlay.setOnClickListener(MainActivity.this);
        }

        navigation.setVisibility(View.GONE);

        mPlaylist.setOnItemClickListener(itemClickListener);

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            Log.d(TAG, "MediaPlayer instance created!");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //
            } else {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } else {
            initPlaylist();
        }
    }


    private void initPlaylist() {
        Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                SELECTION_ARGS,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            mCursorAdapter.swapCursor(cursor);
            mCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    @Override
    protected void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPlaylist();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                mPlayStatus = !mPlayStatus;
                Log.d(TAG, "play status changed");
                if (mPlayStatus == true) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.start();
                    }
                    ivPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                } else {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.pause();
                    }
                    ivPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }
                break;
        }
    }
}
