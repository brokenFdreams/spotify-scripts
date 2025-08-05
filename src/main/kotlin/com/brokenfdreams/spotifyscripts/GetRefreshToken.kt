package com.brokenfdreams.spotifyscripts

import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.enums.AuthorizationScope
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import java.net.URI
import java.util.Scanner

fun main() {
    val clientId = System.getenv("SPOTIFY_CLIENT_ID")
        ?: throw IllegalArgumentException("SPOTIFY_CLIENT_ID must be provided as argument or environment variable")
    val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
        ?: throw IllegalArgumentException("SPOTIFY_CLIENT_SECRET must be provided as argument or environment variable")
    val redirectUri = URI("http://127.0.0.1:8888/callback")

    val spotifyApi = SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build()

    try {
        val uriRequest: AuthorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .scope(
                AuthorizationScope.USER_FOLLOW_READ,
                AuthorizationScope.USER_LIBRARY_READ,
                AuthorizationScope.PLAYLIST_MODIFY_PUBLIC,
                AuthorizationScope.PLAYLIST_MODIFY_PRIVATE
            )
            .build()

        println("1. Open this URL in your browser and authorize:")
        println(uriRequest.execute())

        print("2. After authorization, enter the code from the URL (code=...): ")
        val code = Scanner(System.`in`).nextLine()

        val credentials = spotifyApi.authorizationCode(code)
            .build()
            .execute()
        println("Refresh Token: ${credentials.refreshToken}")
        println("Add this refresh token to GitHub Secrets as SPOTIFY_REFRESH_TOKEN")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}