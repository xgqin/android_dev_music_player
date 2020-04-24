package com.glriverside.xgqin.ggmusic;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

public class MediaAsyncTaskLoader extends AsyncTaskLoader<Cursor> {

    private ContentResolver mContentResolver;
    private Context mContext;
    private Cursor mCursor;

    private final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " = ? " +
            " AND " + MediaStore.Audio.Media.MIME_TYPE + " LIKE ? ";
    private final String[] SELECTION_ARGS = {
            Integer.toString(1),
            "audio/mpeg"
    };
    public MediaAsyncTaskLoader(@NonNull Context context) {
        super(context);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public Cursor loadInBackground() {
        mCursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                SELECTION_ARGS,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );

        return mCursor;
    }
}
