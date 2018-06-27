package com.ait.devang.spotisort;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ait.devang.spotisort.adapter.PlaylistAdapter;

public class CreateSortedPlaylistActivity extends AppCompatActivity {
    public static final String KEY_NEW_SORTED_PLAYLIST_NAME = "KEY_NEW_SORTED_PLAYLIST_NAME";

    private EditText etNewSortedPlaylistName;
    private String playlistClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sorted_playlist);
        playlistClicked = getIntent().getStringExtra(PlaylistAdapter.PLAYLIST_NAME_KEY);
        setupUI();
    }

    private void setupUI() {
        etNewSortedPlaylistName = (EditText) findViewById(R.id.etNewSortedPlaylistName);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnPlaylistName();
            }
        });
    }

    private void returnPlaylistName() {
        Intent intentResult = new Intent();
        intentResult.putExtra(PlaylistAdapter.PLAYLIST_NAME_KEY, playlistClicked);
        intentResult.putExtra(KEY_NEW_SORTED_PLAYLIST_NAME, etNewSortedPlaylistName.getText().toString());
        setResult(RESULT_OK, intentResult);
        finish();
    }
}
