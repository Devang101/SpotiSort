package com.ait.devang.spotisort.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ait.devang.spotisort.CreateSortedPlaylistActivity;
import com.ait.devang.spotisort.MainActivity;
import com.ait.devang.spotisort.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    public static final int REQUEST_NEW_PLAYLIST_NAME = 101;
    public static final String PLAYLIST_NAME_KEY = "PLAYLIST_NAME_KEY";

    private List<PlaylistSimple> playlistList;
    private Context context;
    private int lastPosition = -1;
    private ProgressDialog progressDialog;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPlaylistName;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPlaylistName = (TextView) itemView.findViewById(R.id.tvPlaylistName);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }

    public PlaylistAdapter(List<PlaylistSimple> playlistList, Context context) {
        this.playlistList = playlistList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_playlist, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.tvPlaylistName.setText(playlistList.get(position).name);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playlistClicked = ((TextView)v.findViewById(R.id.tvPlaylistName)).getText().toString();
                showCreateNewSortedPlaylistActivity(playlistClicked);
            }
        });
        setAnimation(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    private void showCreateNewSortedPlaylistActivity(String playlistClicked) {
        Intent intentStart = new Intent(context, CreateSortedPlaylistActivity.class);
        intentStart.putExtra(PLAYLIST_NAME_KEY, playlistClicked);
        ((MainActivity)context).startActivityForResult(intentStart, REQUEST_NEW_PLAYLIST_NAME);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}

