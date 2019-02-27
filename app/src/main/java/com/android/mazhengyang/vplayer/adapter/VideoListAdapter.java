package com.android.mazhengyang.vplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mazhengyang.vplayer.R;
import com.android.mazhengyang.vplayer.model.IImage;
import com.android.mazhengyang.vplayer.model.IImageList;
import com.android.mazhengyang.vplayer.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "Vplayer." + VideoListAdapter.class.getSimpleName();

    private IImageList mAllImages;
    private Context context;
    private OnVideoItemClickListener onVideoItemClickListener;

    public interface OnVideoItemClickListener {
        void onVideoItemClick(IImage image);
    }

    public void setOnVideoItemClickListener(OnVideoItemClickListener listener) {
        this.onVideoItemClickListener = listener;
    }

    public VideoListAdapter(Context context) {
        this.context = context;
    }

    public void setImageList(IImageList list) {
        Log.d(TAG, "setImageList: ");
        mAllImages = list;
        notifyDataSetChanged();
    }

    public void reDraw(int position) {
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_video, parent, false);
        VideoItemViewHolder vh = new VideoItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mAllImages == null) {
            Log.d(TAG, "onBindViewHolder: mAllImages is null.");
            return;
        }

        Log.d(TAG, "onBindViewHolder: position=" + position + ", " + holder);

        IImage image = mAllImages.getImageAt(position);

        ImageView ivThumbnail = ((VideoItemViewHolder) holder).ivThumbnail;
        TextView tvTitle = ((VideoItemViewHolder) holder).tvTitle;
        TextView tvDuration = ((VideoItemViewHolder) holder).tvDuration;

        ivThumbnail.setImageDrawable(null);
        tvTitle.setText("");
        tvDuration.setText("");

        String title = image.getTitle();
        String duration = Util.formatTime(image.getDuration());
        Bitmap bitmap = image.getBitmap();

//        Log.d(TAG, "onBindViewHolder: title=" + title + ", duration=" + duration + ", " + bitmap);

        tvTitle.setText(title);
        tvDuration.setText(duration);
        if (bitmap != null && !bitmap.isRecycled()) {
            ivThumbnail.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "onBindViewHolder: bitmap isRecycled");
        }

//        Glide.with(context)
//                .load(bitmap)
//                .placeholder(R.drawable.ic_image_loading)
//                .error(R.drawable.ic_image_loadfail)
//                .into(ivThumbnail);
    }

//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
//        if (payloads.isEmpty()) {
//
//        } else {
//            onBindViewHolder(holder, position);
//        }
//    }

    @Override
    public int getItemCount() {
        if (mAllImages == null) {
            Log.d(TAG, "getItemCount: mAllImages is null.");
            return 0;
        }
        return mAllImages.getCount();
    }

    public class VideoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvDuration)
        TextView tvDuration;

        public VideoItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onVideoItemClickListener != null) {
                onVideoItemClickListener.onVideoItemClick(mAllImages.getImageAt(this.getPosition()));
            }
        }
    }
}
