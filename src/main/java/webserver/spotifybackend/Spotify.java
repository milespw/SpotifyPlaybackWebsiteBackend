package webserver.spotifybackend;

import org.apache.hc.core5.http.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.IRequest;
import se.michaelthelin.spotify.requests.data.player.GetCurrentUsersRecentlyPlayedTracksRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;

public class Spotify {
    private final String clientId;
    private final String clientSecret;
    private final URI redirectUri;
    private final String[] scopes;

    public Spotify(String clientId, String clientSecret, URI redirectUri, String[] scopes) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
    }

    protected SpotifyApi getClientCredentialsApi() {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
    }

    protected SpotifyApi getPKCEApi(String accessToken) {
        return new SpotifyApi.Builder().setAccessToken(accessToken).build();
    }

    public String getKeyUrl() {
        return this.getClientCredentialsApi().authorizationCodeUri().scope(this.getScopes()).build().execute().toString();
    }

    private String getScopes() {
        if (this.scopes == null) {
            return "";
        }
        return String.join(",", this.scopes);
    }

    private static String[] convertScopesToArray(String scopes) {
        return scopes.split(" ");
    }

    private boolean scopesMatch(String scopes) {
        String[] scopesArray = convertScopesToArray(scopes);

        if (scopesArray.length != this.scopes.length) {
            return false;
        }

        Arrays.sort(scopesArray);
        Arrays.sort(this.scopes);

        return Arrays.equals(scopesArray, this.scopes);
    }

    public AuthorizationCodeCredentials getTokens(String authorizationCode) {
        try {
            return this.getClientCredentialsApi().authorizationCode(authorizationCode).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AuthorizationCodeCredentials refreshAccessToken(String refreshToken, String scopes) {
        if (!(this.scopesMatch(scopes))) {
            return null;
        }

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(this.clientId)
                .setClientSecret(this.clientSecret)
                .setRefreshToken(refreshToken)
                .build();
        try {
            return spotifyApi.authorizationCodeRefresh().build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public User getUserProfile(String accessToken) {
        try {
            return this.getPKCEApi(accessToken).getCurrentUsersProfile().build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public Paging<Track> getTopTracks(String accessToken) {
        try {
            return this.getPKCEApi(accessToken).getUsersTopTracks().limit(50).time_range("short_term").build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public Paging<Artist> getTopArtists(String accessToken) {
        try {
            return this.getPKCEApi(accessToken).getUsersTopArtists().limit(25).time_range("short_term").build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public CurrentlyPlaying getCurrentlyPlaying(String accessToken) {
        try {
            return this.getPKCEApi(accessToken).getUsersCurrentlyPlayingTrack().build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public PagingCursorbased<PlayHistory> getRecentlyPlayed(String accessToken, Integer before) {
        try {
            GetCurrentUsersRecentlyPlayedTracksRequest.Builder
                     builder = this.getPKCEApi(accessToken).getCurrentUsersRecentlyPlayedTracks().limit(50);
             if (before != null) {
                 Date beforeDate = new Date(before);
                 builder.before(beforeDate);
             }
             return builder.build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }

    public Paging<SavedTrack> getSavedTracks(String accessToken, int offset) {
        try {
            return this.getPKCEApi(accessToken).getUsersSavedTracks().limit(50).offset(offset).build().execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }
    }
}
