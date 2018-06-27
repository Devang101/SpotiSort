package com.ait.devang.spotisort;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ait.devang.spotisort.adapter.PlaylistAdapter;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.client.Response;

public class MainActivity extends Activity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "TYPE YOUR CLIENT ID HERE";
    private static final String REDIRECT_URI = "release-date-sorter://callback";
    private static final int AUTH_REQUEST_CODE = 1337;

    public static final String SORTED_URIS = "SORTED_URIS";
    public static final String SORTED_NAMES = "SORTED_NAMES";
    public static final String SORTED_DATES = "SORTED_DATES";
    public static final String SORTED_IMAGE_URLS = "SORTED_IMAGE_URLS";

    private Player mPlayer;
    private PlaylistAdapter playlistAdapter;
    private LinearLayout layoutContent;
    private List<PlaylistSimple> playlists;
    private SpotifyService spotify;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutContent = (LinearLayout) findViewById(
                R.id.layoutContent);
        sendAuthenticationRequest();
    }

    private void sendAuthenticationRequest() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private", "playlist-modify-public", "playlist-modify-private", "user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, AUTH_REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == AUTH_REQUEST_CODE) {
            authenticateUser(resultCode, intent);
        } else if (requestCode == PlaylistAdapter.REQUEST_NEW_PLAYLIST_NAME) {
            switch (resultCode) {
                case RESULT_OK:
                    showProgressDialog();
                    String playlistClicked = intent.getStringExtra(PlaylistAdapter.PLAYLIST_NAME_KEY);
                    final String newPlaylistName = intent.getStringExtra(CreateSortedPlaylistActivity.KEY_NEW_SORTED_PLAYLIST_NAME);
                    if (playlists != null) {
                        findPlaylistToSort(playlistClicked, newPlaylistName);
                    }
                    break;
                case RESULT_CANCELED:
                    showSnackBarMessage(getString(R.string.txt_playlist_creation_cancel));
                    break;
            }
        }
    }

    private void findPlaylistToSort(String playlistClicked, String newPlaylistName) {
        String playListID = null;
        String userID = null;
        int totalTracks = 0;
        for (PlaylistSimple p : playlists) {
            if (p.name.equals(playlistClicked)) {
                playListID = p.id;
                userID = p.owner.id;
                totalTracks = p.tracks.total;
                break;
            }
        }
        final String finalPlayListID = playListID;
        final int finalTotalTracks = totalTracks;
        Map<Track, DateAndImageURLHolder> emptyMap = new HashMap<Track, DateAndImageURLHolder>();
        getPlaylistTracks(userID, finalPlayListID, emptyMap, finalTotalTracks, 0, newPlaylistName) ;
    }

    private void authenticateUser(int resultCode, Intent intent) {
        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
            setUpSpotifyAPI(response);
            loadUsersPlaylists();
            setUpPlayer(response);
        }
    }

    private void setUpPlayer(AuthenticationResponse response) {
        Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(MainActivity.this);
                mPlayer.addNotificationCallback(MainActivity.this);
                SongPlayer.getInstance().setPlayer(mPlayer);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    private void loadUsersPlaylists() {
        spotify.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d("MainActivity", "Failed to get playlists");

            }

            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                playlists = playlistSimplePager.items;
                playlistAdapter = new PlaylistAdapter(playlists, MainActivity.this);
                RecyclerView recyclerViewPlaces = (RecyclerView) findViewById(
                        R.id.recyclerViewPlaylists);
                recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerViewPlaces.setAdapter(playlistAdapter);
            }
        });
    }

    private void setUpSpotifyAPI(AuthenticationResponse response) {
        String accessToken = response.getAccessToken();
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        spotify = api.getService();
    }

    public void getPlaylistTracks(final String userID, final String playListID, final Map<Track, DateAndImageURLHolder> tracksToReleaseDate, final int totalTracks, final int offset, final String newPlaylistName) {

        spotify.getPlaylistTracks(userID, playListID, new SpotifyCallback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                int numTracks = playlistTrackPager.items.size();
                for (final PlaylistTrack pt : playlistTrackPager.items) {
                    String albumID = pt.track.album.id;
                    List<Image> images = pt.track.album.images;
                    final String albumCoverUrl;
                    if(images.size()>0){
                        albumCoverUrl = images.get(0).url;
                    }
                    else{
                        albumCoverUrl = null;
                    }
                    if(albumID == null){
                        numTracks--;
                        Log.d("MainActivity", "AlbumID is null");
                    }
                    if(albumCoverUrl == null){
                        Log.d("MainActivity", "AlbumCoverUrl is null");
                    }
                    final int finalNumTracks = numTracks;
                    spotify.getAlbum(albumID, new SpotifyCallback<Album>() {
                        @Override
                        public void failure(SpotifyError spotifyError) {
                            Log.d("MainActivity", "Failed to get album: " + spotifyError.getMessage());
                            sort(tracksToReleaseDate, finalNumTracks - 1, newPlaylistName);
                        }

                        @Override
                        public void success(Album album, Response response) {
                            String release_date = album.release_date;
                            if (release_date.length() == 4) {
                                release_date += "-01-01";
                            } else if (release_date.length() == 7) {
                                release_date += "-01";
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date songReleaseDate = null;
                            try {
                                songReleaseDate = sdf.parse(release_date);
                            } catch (ParseException e) {
                                Log.d("MainActivity", "Couldn't parse date");
                                e.printStackTrace();
                            }
                            DateAndImageURLHolder dateAndImageURLHolder = new DateAndImageURLHolder(songReleaseDate, albumCoverUrl);
                            tracksToReleaseDate.put(pt.track, dateAndImageURLHolder);
                            Log.d("MainActivity", tracksToReleaseDate.size()+" "+ finalNumTracks);
                            sort(tracksToReleaseDate, finalNumTracks, newPlaylistName);
                        }
                    });
                }
            }

            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d("MainActivity", "Failed to get tracks: " + spotifyError.getErrorDetails().message + " " +
                        spotifyError.getLocalizedMessage());
            }
        });
    }


    private void sort(Map<Track, DateAndImageURLHolder> trackDateAndImageURLHolderMap, int finalNumTracks, String newPlaylistName) {
        if (trackDateAndImageURLHolderMap.size() == finalNumTracks) {
            //sort
            Comparator<Map.Entry<Track, DateAndImageURLHolder>> DateComparator = new Comparator<Map.Entry<Track, DateAndImageURLHolder>>() {
                @Override
                public int compare(Map.Entry<Track, DateAndImageURLHolder> e1, Map.Entry<Track, DateAndImageURLHolder> e2) {
                    Date v1 = e1.getValue().getDate();
                    Date v2 = e2.getValue().getDate();
                    return v1.compareTo(v2);
                }
            };
            Set<Map.Entry<Track, DateAndImageURLHolder>> entries = trackDateAndImageURLHolderMap.entrySet();
            List<Map.Entry<Track, DateAndImageURLHolder>> listOfEntries = new ArrayList<Map.Entry<Track, DateAndImageURLHolder>>(entries);
            Collections.sort(listOfEntries, DateComparator);
            final ArrayList<String> sortedListTrackURIs = new ArrayList<String>();
            final ArrayList<String> sortedListTrackNames = new ArrayList<String>();
            final ArrayList<String> sortedListTrackDates = new ArrayList<String>();
            final ArrayList<String> sortedListCoverUrls = new ArrayList<String>();

            for (Map.Entry<Track, DateAndImageURLHolder> entry : listOfEntries) {
                sortedListTrackURIs.add(entry.getKey().uri);
                sortedListTrackNames.add(entry.getKey().name);
                sortedListTrackDates.add(new SimpleDateFormat("MM-dd-yyyy").format(entry.getValue().getDate()));
                sortedListCoverUrls.add(entry.getValue().getImageUrl());

            }
            Log.d("MainActivity", Arrays.toString(new ArrayList[]{sortedListTrackNames}));
            progressDialog.dismiss();
            showResultActivity(newPlaylistName, sortedListTrackURIs, sortedListTrackNames, sortedListTrackDates, sortedListCoverUrls);
        }
    }

    private void showResultActivity(String newPlaylistName, ArrayList<String> sortedListTrackURIs, ArrayList<String> sortedListTrackNames, ArrayList<String> sortedListTrackDates, ArrayList<String> sortedListCoverUrls) {
        Intent result = new Intent(MainActivity.this, ResultActivity.class);
        result.putStringArrayListExtra(SORTED_URIS , sortedListTrackURIs);
        result.putStringArrayListExtra(SORTED_NAMES , sortedListTrackNames);
        result.putStringArrayListExtra(SORTED_DATES , sortedListTrackDates);
        result.putStringArrayListExtra(SORTED_IMAGE_URLS, sortedListCoverUrls);
        result.putExtra(CreateSortedPlaylistActivity.KEY_NEW_SORTED_PLAYLIST_NAME, newPlaylistName);
        MainActivity.this.startActivity(result);
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(layoutContent,
                message,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.action_hide, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        }).show();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage(MainActivity.this.getString(R.string.progress_message));
        }
        progressDialog.show();
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login Failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    private class DateAndImageURLHolder{
        private Date date;
        private String imageUrl;

        public DateAndImageURLHolder(Date date, String albumCoverUrl) {
            this.date = date;
            this.imageUrl = albumCoverUrl;
        }

        public Date getDate() {
            return date;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
