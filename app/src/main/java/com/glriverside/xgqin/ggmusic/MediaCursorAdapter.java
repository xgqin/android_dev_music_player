package com.glriverside.xgqin.ggmusic;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MediaCursorAdapter extends CursorAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private static final int NORMAL_LENGTH = 20;

    private static final String TAG = MediaCursorAdapter.class.getSimpleName();

    public MediaCursorAdapter(Context context) {
        super(context, null, 0);
        mContext = context;

        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View itemView = mLayoutInflater.inflate(R.layout.list_item, viewGroup, false);

        if (itemView != null) {
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvTitle = itemView.findViewById(R.id.tv_title);
            viewHolder.tvArtist = itemView.findViewById(R.id.tv_artist);
            viewHolder.tvOrder = itemView.findViewById(R.id.tv_order);
            viewHolder.divider = itemView.findViewById(R.id.divider);
            itemView.setTag(viewHolder);

            return itemView;
        }

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);


        String title = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);


        int titleLength = title.length();
        int position = cursor.getPosition();
        int count = cursor.getCount();

        if (viewHolder != null) {
            viewHolder.tvTitle.setText(title);
            if (titleLength > NORMAL_LENGTH) {
                String reTitle = title.substring(0, NORMAL_LENGTH/2) +
                        "..." + title.substring(titleLength-NORMAL_LENGTH/2, titleLength);
                viewHolder.tvTitle.setText(reTitle);
            }

            viewHolder.tvArtist.setText(artist);
            viewHolder.tvOrder.setText(Integer.toString(position+1));
            viewHolder.divider.setVisibility(View.VISIBLE);

            if (position == count - 1) {
                viewHolder.divider.setVisibility(View.INVISIBLE);
            }
        }
    }

    public class ViewHolder {
        TextView tvTitle;
        TextView tvArtist;
        TextView tvOrder;
        View divider;
    }
}
