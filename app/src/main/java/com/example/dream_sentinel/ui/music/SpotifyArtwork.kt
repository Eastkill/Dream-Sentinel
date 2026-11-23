package com.example.dream_sentinel.ui.music

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyProblem
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite

@Composable
internal fun SpotifyArtwork(artwork: Bitmap?, modifier: Modifier = Modifier) {
    if (artwork != null) {
        Image(artwork.asImageBitmap(), contentDescription = "Current Spotify artwork", modifier = modifier.size(80.dp))
    } else {
        Text("Spotify", color = SideColorWhite, modifier = modifier.size(80.dp))
    }
}

@Composable
internal fun SpotifyContent(
    state: SpotifyUiState,
    unlocked: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPlaylistChanged: (String) -> Unit,
    onPlayPlaylist: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    if (!unlocked) {
        Text("Unlock your device to use Spotify.", color = MainColorWhite)
        return
    }
    val uriHandler = LocalUriHandler.current
    val spotify = state.spotify
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Spotify: ${spotify.connection.name.lowercase()}", color = MainColorWhite)
        Text("Allows playback control and reading the current track and artwork. This app does not store Spotify access or refresh tokens.", color = SideColorWhite)
        if (spotify.connection == SpotifyConnection.CONNECTED) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpotifyArtwork(state.artwork)
                Column {
                    Text(spotify.track?.title ?: "Reading playback state…", color = MainColorWhite)
                    Text(spotify.track?.artist.orEmpty(), color = SideColorWhite)
                    Text(
                        when (spotify.isPaused) {
                            true -> "Paused"
                            false -> "Playing in Spotify"
                            null -> "Playback state unknown"
                        },
                        color = SideColorWhite,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPrevious, enabled = state.canControl && spotify.canSkipPrevious) {
                    Text("Previous")
                }
                OutlinedButton(onClick = onNext, enabled = state.canControl && spotify.canSkipNext) {
                    Text("Next")
                }
            }
            Button(
                onClick = if (spotify.isPaused == false) onPause else onResume,
                enabled = state.canControl && spotify.isPaused != null,
            ) { Text(if (spotify.isPaused == false) "Pause" else "Resume") }
        }
        if (spotify.connection != SpotifyConnection.CONNECTED) {
            Button(
                onClick = onConnect,
                enabled = spotify.connection != SpotifyConnection.UNCONFIGURED &&
                    spotify.connection != SpotifyConnection.CONNECTING,
            ) { Text("Connect Spotify") }
        }
        if (spotify.connectionRequested) {
            TextButton(onClick = onDisconnect) { Text("Disconnect controls") }
        }
        OutlinedTextField(
            value = state.playlistInput,
            onValueChange = onPlaylistChanged,
            label = { Text("Spotify playlist link or URI") },
            isError = state.playlistError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        state.playlistError?.let { Text(it, color = MainColorWhite) }
        Button(onClick = onPlayPlaylist, enabled = state.canControl) { Text("Play playlist") }
        val message = state.actionError ?: spotify.failure?.message
        message?.let { Text(it, color = MainColorWhite) }
        if (spotify.connection == SpotifyConnection.UNCONFIGURED) {
            Text("Spotify is not configured in this build.", color = SideColorWhite)
        }
        if (spotify.failure?.problem == SpotifyProblem.NOT_INSTALLED) {
            TextButton(onClick = { uriHandler.openUri("https://play.google.com/store/apps/details?id=com.spotify.music") }) {
                Text("Install Spotify")
            }
        }
        spotify.track?.uri?.takeIf { it.startsWith("spotify:track:") || it.startsWith("spotify:episode:") }
            ?.let { trackUri ->
                val type = trackUri.substringAfter("spotify:").substringBefore(':')
                val id = trackUri.substringAfterLast(':')
                if (Regex("[A-Za-z0-9]+").matches(id)) {
                    TextButton(onClick = { uriHandler.openUri("https://open.spotify.com/$type/$id") }) {
                        Text("Open in Spotify")
                    }
                }
            }
        Text("Music is played by Spotify. Disconnecting controls does not pause it.", color = SideColorWhite)
        Text("The sleep timer and local sound sliders do not control Spotify.", color = SideColorWhite)
        TextButton(onClick = { uriHandler.openUri("https://www.spotify.com/account/apps/") }) {
            Text("Manage Spotify access")
        }
    }
}
