package com.glriverside.xgqin.ggmusic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     ItemListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class MediaInfoDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_AUDIO_TITLE = "audio_title";
    private static final String ARG_AUDIO_ARTIST = "audio_artist";
    private static final String ARG_AUDIO_THUMBNAIL = "audio_thumbnail";

    // TODO: Customize parameters
    public static MediaInfoDialogFragment newInstance(String title, String artist, String albumArt) {
        final MediaInfoDialogFragment fragment = new MediaInfoDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_AUDIO_TITLE, title);
        args.putString(ARG_AUDIO_ARTIST, artist);
        args.putString(ARG_AUDIO_THUMBNAIL, albumArt);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_media_toolbar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView ivAlbumArt = view.findViewById(R.id.iv_thumbnail);
        TextView tvTitle = view.findViewById(R.id.tv_bottom_title);
        TextView tvArtist = view.findViewById(R.id.tv_bottom_artist);

        Bundle args = getArguments();
        String title = args.getString(ARG_AUDIO_TITLE);
        String artist = args.getString(ARG_AUDIO_ARTIST);
        String albumArt = args.getString(ARG_AUDIO_THUMBNAIL);

        tvTitle.setText(title);
        tvArtist.setText(artist);
        Glide.with(getActivity()).load(albumArt).into(ivAlbumArt);
    }
}
