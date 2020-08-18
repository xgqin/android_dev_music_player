package com.glriverside.xgqin.ggmusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ContentResolver mContentResolver;
    private ListView mPlaylist;
    private MediaCursorAdapter mCursorAdapter;

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String ALBUM_ART = "com.glriverside.xgqin.ggmusic.ALBUM_ART_URI";
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

    private MusicService mService;
    private boolean mBound = false;

    private MusicReceiver musicReceiver;

    private Disposable musicProgressDisposable;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) iBinder;

            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

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

                Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
                serviceIntent.putExtra(MainActivity.DATA_URI, data);
                serviceIntent.putExtra(MainActivity.TITLE, title);
                serviceIntent.putExtra(MainActivity.ARTIST, artist);

                startForegroundService(serviceIntent);


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

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    try {
                        int width = (int) MainActivity.this.getResources().getDisplayMetrics().density * 40;
                        int height = (int) MainActivity.this.getResources().getDisplayMetrics().density * 40;

                        Bitmap albumBitmap = mContentResolver.loadThumbnail(albumUri, new Size(width, height), null);
                        Glide.with(MainActivity.this).load(albumBitmap).into(ivAlbumThumbnail);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    Bundle args = new Bundle();
                    args.putString(ALBUM_ART, albumUri.toString());

                    getSupportLoaderManager().initLoader(1, args, loaderCallbacks);
                }
            }
        }
    };


    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            switch (id) {
                case 0:
                    return new MediaAsyncTaskLoader(MainActivity.this);
                case 1:
                    return new AlbumAsyncTaskerLoader(MainActivity.this, args);
            }
            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            switch (loader.getId()) {
                case 0:
                    mCursorAdapter.swapCursor(cursor);
                    mCursorAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int albumArtIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                        String albumArt = cursor.getString(albumArtIndex);

                        Log.d(TAG, "albumArt: " + albumArt);

                        Glide.with(MainActivity.this).load(albumArt).into(ivAlbumThumbnail);
                    }
                    break;
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //
            } else {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } else {
            getSupportLoaderManager().initLoader(0, null, loaderCallbacks);
        }

        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MUSIC_START);
        intentFilter.addAction(ACTION_MUSIC_STOP);
        registerReceiver(musicReceiver, intentFilter);

    }

    @Override
    protected void onStart() {

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(mConnection);
        mBound = false;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(musicReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportLoaderManager().initLoader(0, null, loaderCallbacks);
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
                    mService.play();
                    ivPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                } else {
                    mService.pause();
                    ivPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                }
                break;
        }
    }

    public class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_MUSIC_START)) {
                if (mService != null) {
                    pbProgress.setMax(mService.getDuration());
                    Log.d(TAG, "Duration: " + mService.getDuration());
                    Observable.create(new ObservableOnSubscribe<Integer>() {
                        @Override
                        public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                            int i = 0;
                            while (true) {
                                emitter.onNext(i++);
                                Thread.sleep(10);
                            }
                        }
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Integer>() {
                                @Override
                                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                                    musicProgressDisposable = d;
                                }

                                @Override
                                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                                    if (mService != null && mService.isPlaying()) {
                                        int position = mService.getCurrentPosition();
                                        pbProgress.setProgress(position);
                                    }
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            } else if (intent.getAction().equals(ACTION_MUSIC_STOP)) {
                if (musicProgressDisposable != null) {
                    musicProgressDisposable.dispose();
                }
            }
        }
    }
}
