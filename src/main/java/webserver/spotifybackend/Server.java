package webserver.spotifybackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/spotifybackendapi")
public class Server {
    private final Spotify spotify;

    public final String clientId;
    public final String clientSecret;
    public final String redirectUriString;

    public Server(
            @Value("${SPOTIFY_CLIENT_ID}") String clientId,
            @Value("${SPOTIFY_CLIENT_SECRET}") String clientSecret,
            @Value("${SPOTIFY_REDIRECT_URI}") String redirectUriString
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUriString = redirectUriString;

        URI redirectUri = null;
        try {
            redirectUri = new URI(redirectUriString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String[] scopes = new String[6];
        scopes[0] = "user-read-private";
        scopes[1] = "user-read-email";
        scopes[2] = "user-top-read";
        scopes[3] = "user-read-playback-state";
        scopes[4] = "user-read-recently-played";
        scopes[5] = "user-library-read";

        this.spotify = new Spotify(clientId, clientSecret, redirectUri, scopes);
    }

    @GetMapping(value = "/get/spotifyUrl")
    public ResponseEntity<String> getSpotifyUrl() {
        return ResponseEntity.ok(spotify.getKeyUrl());
    }

    @GetMapping(value = "/get/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizationCodeCredentials> getTokens(@RequestHeader final String code) {
        return ResponseEntity.ok(spotify.getTokens(code));
    }

    @GetMapping(value = "/get/refresh/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthorizationCodeCredentials> refreshAccessToken(@RequestHeader final String refreshToken,
                                                                           @RequestHeader final String scopes) {
        return ResponseEntity.ok(spotify.refreshAccessToken(refreshToken, scopes));
    }

    @GetMapping(value = "/get/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUserProfile(@RequestHeader final String accessToken) {
        return ResponseEntity.ok(spotify.getUserProfile(accessToken));
    }

    @GetMapping(value = "/get/top/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Paging<Track>> getTopTracks(@RequestHeader final String accessToken) {
        return ResponseEntity.ok(spotify.getTopTracks(accessToken));
    }

    @GetMapping(value = "/get/top/artists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Paging<Artist>> getTopArtists(@RequestHeader final String accessToken) {
        return ResponseEntity.ok(spotify.getTopArtists(accessToken));
    }

    @GetMapping(value = "/get/now/playing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrentlyPlaying> getNowPlaying(@RequestHeader final String accessToken) {
        return ResponseEntity.ok(spotify.getCurrentlyPlaying(accessToken));
    }

    @GetMapping(value = "/get/now/recentlyPlayed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagingCursorbased<PlayHistory>> getRecentlyPlayed(@RequestHeader final String accessToken,
                                                                            @RequestHeader(required = false) final Integer before) {
        return ResponseEntity.ok(spotify.getRecentlyPlayed(accessToken, before));
    }

    @GetMapping(value = "/get/user/saved/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Paging<SavedTrack>> getSavedTracks(@RequestHeader final String accessToken,
                                                             @RequestHeader(required = false) final Integer page) {
        if (page == null) {
            return ResponseEntity.ok(spotify.getSavedTracks(accessToken, 0));
        }
        return ResponseEntity.ok(spotify.getSavedTracks(accessToken, page*50));
    }
}
