package com.ait.devang.spotisort;

import com.spotify.sdk.android.player.Player;

public class SongPlayer {
    private static SongPlayer songPlayer = null;
    private Player mplayer;
    private boolean isPlaying;

    private SongPlayer(){
    }

    public static SongPlayer getInstance() {
        if(songPlayer == null){
            songPlayer = new SongPlayer();
        }
        return songPlayer;
    }

    public void setPlayer(Player mPlayer){
        this.mplayer = mPlayer;
    }

    public void playSong(String uri){
        this.mplayer.playUri(null, uri, 0, 0);
        this.isPlaying = true;
    }

    public void pauseSong(){
        this.mplayer.pause(null);
        this.isPlaying = false;
    }

    public void resumeSong(){
        this.mplayer.resume(null);
        this.isPlaying = true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
