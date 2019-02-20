package com.android.mazhengyang.vplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mazhengyang.vplayer.R;
import com.android.mazhengyang.vplayer.bean.VideoBean;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = VideoListAdapter.class.getSimpleName();

    private List<VideoBean> videoList = new ArrayList<>();
    private Context context;
    private OnVideoItemClickListener onVideoItemClickListener;

    public interface OnVideoItemClickListener {
        void onVideoItemClick(View view, int position);
    }

    public void setOnVideoItemClickListener(OnVideoItemClickListener listener) {
        this.onVideoItemClickListener = listener;
    }

    public VideoListAdapter(Context context) {
        this.context = context;
    }

    public void add(VideoBean videoBean) {
        int i = getItemCount();
        videoList.add(i, videoBean);
        this.notifyItemInserted(i);
        i++;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_video, parent, false);
        VideoItemViewHolder vh = new VideoItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoBean videoBean = videoList.get(position);

        ImageView ivThumbnail = ((VideoItemViewHolder) holder).ivThumbnail;
        TextView tvTitle = ((VideoItemViewHolder) holder).tvTitle;
        TextView tvDuration = ((VideoItemViewHolder) holder).tvDuration;
        ivThumbnail.setImageDrawable(null);

        tvTitle.setText(videoBean.getDisplayName());
        tvDuration.setText(videoBean.getPath());
        Glide.with(context)
                .load(videoBean.getPath())
                .placeholder(R.drawable.ic_image_loading)
                .error(R.drawable.ic_image_loadfail)
                .into(ivThumbnail);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
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
                onVideoItemClickListener.onVideoItemClick(v, this.getPosition());
            }
        }
    }
}
