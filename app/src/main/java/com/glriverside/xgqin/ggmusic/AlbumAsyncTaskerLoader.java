package com.glriverside.xgqin.ggmusic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

public class AlbumAsyncTaskerLoader extends AsyncTaskLoader<Cursor> {
    private ContentResolver mContentResolver;
    private Context mContext;
    private Cursor mCursor;

    private Uri albumUri;
    private Cursor cursor;

    public AlbumAsyncTaskerLoader(@NonNull Context context, Bundle args) {
        super(context);
        mContext = context;
        mContentResolver = context.getContentResolver();
        String sAlbumUri = args.getString(MainActivity.ALBUM_ART);
        albumUri = Uri.parse(sAlbumUri);
    }

    @Nullable
    @Override
    public Cursor loadInBackground() {
        cursor = mContentResolver.query(
                albumUri,
                null,
                null,
                null,
                null
        );

        return cursor;
    }
}
