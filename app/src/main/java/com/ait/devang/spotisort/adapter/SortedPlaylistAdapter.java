package com.ait.devang.spotisort.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ait.devang.spotisort.R;
import com.ait.devang.spotisort.ResultActivity;
import com.ait.devang.spotisort.SongPlayer;
import com.bumptech.glide.Glide;

import java.util.List;

public class SortedPlaylistAdapter extends RecyclerView.Adapter<SortedPlaylistAdapter.ViewHolder> {

    private List<String> names;
    private List<String> uris;
    private List<String> dates;
    private List<String> albumCovers;
    private Context context;
    private int lastPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSongName;
        public TextView tvSongDate;
        public ImageView ivAlbumCover;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSongName = (TextView) itemView.findViewById(R.id.tvSongName);
            tvSongDate = (TextView) itemView.findViewById(R.id.tvSongDate);
            ivAlbumCover = itemView.findViewById(R.id.ivAlbumCover);
            cardView = (CardView) itemView.findViewById(R.id.song_card_view);
        }
    }

    public SortedPlaylistAdapter(List<String> names, List<String> uris, List<String> dates, List<String> albumCovers, Context context) {
        this.names = names;
        this.uris = uris;
        this.dates = dates;
        this.albumCovers = albumCovers;
        this.context = context;
    }

    @Override
    public SortedPlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sorted_song, parent, false);
        SortedPlaylistAdapter.ViewHolder vh = new SortedPlaylistAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final SortedPlaylistAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.tvSongName.setText(names.get(position));
        viewHolder.tvSongDate.setText(dates.get(position));
        if(albumCovers.get(position) != null){
            Glide.with(context).load(albumCovers.get(position)).into(viewHolder.ivAlbumCover);
        }
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongPlayer.getInstance().playSong(uris.get(position));
                ResultActivity.setPlayPauseView(false);
            }
        });
        setAnimation(viewHolder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
