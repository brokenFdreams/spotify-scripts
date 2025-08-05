package com.brokenfdreams.spotifyscripts

import com.google.gson.JsonArray
import org.slf4j.LoggerFactory
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.enums.ModelObjectType
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified
import se.michaelthelin.spotify.model_objects.specification.Artist
import se.michaelthelin.spotify.model_objects.specification.Playlist
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {
    val logger = LoggerFactory.getLogger("SpotifyNewReleases")

    val clientId = System.getenv("SPOTIFY_CLIENT_ID")
        ?: throw IllegalArgumentException("SPOTIFY_CLIENT_ID not set")
    val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        ?: throw IllegalArgumentException("SPOTIFY_CLIENT_SECRET not set")
    val refreshToken = System.getenv("SPOTIFY_REFRESH_TOKEN")
        ?: throw IllegalArgumentException("SPOTIFY_REFRESH_TOKEN not set")
    val userId = System.getenv("SPOTIFY_USERNAME")
        ?: throw IllegalArgumentException("SPOTIFY_USERNAME not set")
    val playlistName = "New Releases - ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}"

    val spotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRefreshToken(refreshToken)
        .build()

    try {
        val credentials = spotifyApi.authorizationCodeRefresh().build().execute()
        spotifyApi.accessToken = credentials.accessToken

        val artists = getFollowedArtists(spotifyApi)
        logger.info("Found ${artists.size} followed artists.")

        val newTracks = getNewReleases(spotifyApi, artists)
        logger.info("Found ${newTracks.size} new tracks.")

        if (newTracks.isNotEmpty()) {
            val playlist = createOrUpdatePlaylist(spotifyApi, userId, playlistName, newTracks)
            logger.info("Playlist created: ${playlist.externalUrls["spotify"]}")
        } else {
            logger.warn("No new releases found for this week.")
        }
    } catch (e: Exception) {
        logger.error("Error: ${e.message}")
        throw e
    }
}

fun getFollowedArtists(spotifyApi: SpotifyApi): List<Artist> {
    val artists = mutableListOf<Artist>()
    var result = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
        .limit(50)
        .build()
        .execute()
    artists.addAll(result.items)
    var after: String? = result.cursors?.firstOrNull()?.after
    while (after != null) {
        result = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
            .limit(50)
            .after(after)
            .build()
            .execute()
        artists.addAll(result.items)
        after = result.cursors?.firstOrNull()?.after
    }
    return artists
}

fun getNewReleases(spotifyApi: SpotifyApi, artists: List<Artist>, days: Long = 7): List<TrackSimplified> {
    val cutoffDate = LocalDate.now().minusDays(days)

    return artists.flatMap { artist -> getAuthorNewReleases(spotifyApi, artist, cutoffDate) }
        .distinctBy { it.uri }
}

fun getAuthorNewReleases(
    spotifyApi: SpotifyApi,
    artist: Artist,
    cutoffDate: LocalDate
): List<TrackSimplified> {
    var offset = 0
    val limit = 50
    val newTracks = mutableListOf<TrackSimplified>()
    var isFinished = false
    while (!isFinished) {
        val albums = spotifyApi.getArtistsAlbums(artist.id)
            .include_groups("album,single")
            .limit(limit)
            .offset(offset)
            .build()
            .execute()
        isFinished = albums.next == null

        albums.items
            .filter { album ->
                val releaseDate = album.releaseDate?.let {
                    try {
                        LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (_: Exception) {
                        null
                    }
                }
                releaseDate != null && releaseDate >= cutoffDate
            }
            .flatMap { album -> getAlbumTracks(spotifyApi, album, artist) }
            .apply { newTracks.addAll(this) }
        offset += limit
    }
    return newTracks
}

fun getAlbumTracks(
    spotifyApi: SpotifyApi,
    album: AlbumSimplified,
    artist: Artist,
): List<TrackSimplified> {
    val limit = 50
    var offset = 0
    val newTracks = mutableListOf<TrackSimplified>()
    var isFinished = false
    while (!isFinished) {
        val tracks = spotifyApi.getAlbumsTracks(album.id)
            .limit(50)
            .offset(offset)
            .build()
            .execute()
        isFinished = tracks.next == null

        tracks.items
            .filter { track ->
                track.artists.map { it.id }.contains(artist.id)
            }.apply { newTracks.addAll(this) }
        offset += limit
    }
    return newTracks
}

fun createOrUpdatePlaylist(
    spotifyApi: SpotifyApi,
    userId: String,
    playlistName: String,
    tracks: List<TrackSimplified>
): Playlist {
    val playlist = spotifyApi.createPlaylist(userId, playlistName)
        .public_(false)
        .description("Weekly new releases from followed artists")
        .build()
        .execute()

    tracks.map { it.uri }.chunked(100)
        .forEach { chunk ->
            val jsonArray = JsonArray().apply {
                chunk.forEach { uri -> add(uri) }
            }
            spotifyApi.addItemsToPlaylist(playlist.id, jsonArray)
                .build()
                .execute()
        }

    return playlist
}