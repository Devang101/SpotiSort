package com.ait.devang.spotisort;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ait.devang.spotisort.adapter.SortedPlaylistAdapter;
import com.ohoussein.playpause.PlayPauseView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private SortedPlaylistAdapter sortedPlaylistAdapter;
    private static PlayPauseView playPauseView;
    private TextView tvNewSortedPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ArrayList<String> names = getIntent().getStringArrayListExtra(MainActivity.SORTED_NAMES);
        ArrayList<String> uris = getIntent().getStringArrayListExtra(MainActivity.SORTED_URIS);
        ArrayList<String> dates = getIntent().getStringArrayListExtra(MainActivity.SORTED_DATES);
        ArrayList<String> coverURLs = getIntent().getStringArrayListExtra(MainActivity.SORTED_IMAGE_URLS);
        String newSortedPlaylistName = getIntent().getStringExtra(CreateSortedPlaylistActivity.KEY_NEW_SORTED_PLAYLIST_NAME);
        setUpUI(names, uris, dates, coverURLs, newSortedPlaylistName);
    }

    private void setUpUI(ArrayList<String> names, ArrayList<String> uris, ArrayList<String> dates, ArrayList<String> coverURLs, String newSortedPlaylistName) {
        setUpPlaylistRecycler(names, uris, dates, coverURLs);
        setUpPlaylistName(newSortedPlaylistName);
        setUpPlayPauseButton();
    }

    private void setUpPlayPauseButton() {
        playPauseView = (PlayPauseView) findViewById(R.id.play_pause_view);
        playPauseView.setVisibility(View.INVISIBLE);
        playPauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongPlayer player = SongPlayer.getInstance();
                if(player.isPlaying() && !playPauseView.isPlay()){
                    player.pauseSong();
                    playPauseView.toggle();
                }
                else if(!player.isPlaying() && playPauseView.isPlay()){
                    player.resumeSong();
                    playPauseView.toggle();
                }
            }
        });
    }

    private void setUpPlaylistRecycler(ArrayList<String> names, ArrayList<String> uris, ArrayList<String> dates, ArrayList<String> coverURLs) {
        sortedPlaylistAdapter = new SortedPlaylistAdapter(names, uris, dates, coverURLs, this);
        RecyclerView recyclerViewPlaces = (RecyclerView) findViewById(
                R.id.recyclerViewSortedPlaylist);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlaces.setAdapter(sortedPlaylistAdapter);
    }

    private void setUpPlaylistName(String newSortedPlaylistName) {
        tvNewSortedPlaylist = findViewById(R.id.tvSortPlaylistName);
        tvNewSortedPlaylist.setText(newSortedPlaylistName);
    }

    public static void setPlayPauseView(boolean isPlay){
        playPauseView.setVisibility(View.VISIBLE);
        playPauseView.change(isPlay);
    }
}
