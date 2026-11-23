package com.example.dream_sentinel.system.spotify

import android.content.Context
import android.content.ComponentName
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.util.Log
import com.example.dream_sentinel.BuildConfig

/** Grants activity launches only to the Spotify service during explicit consent. */
internal class SpotifyBindingContext(
    base: Context,
    private val consentAllowed: () -> Boolean,
) : ContextWrapper(base.applicationContext) {
    private val bindings = mutableSetOf<ServiceConnection>()
    private var released = false

    // App Remote calls getApplicationContext() before binding its service.
    override fun getApplicationContext(): Context = this

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean =
        synchronized(bindings) {
            if (released) return@synchronized false
            val spotifyService = service.`package` == "com.spotify.music" &&
                service.action in setOf(
                    "com.spotify.mobile.appprotocol.action.BIND_PROTOCOL_SERVICE",
                    "com.spotify.mobile.appprotocol.action.START_APP_PROTOCOL_SERVICE",
                )
            val allowActivity = Build.VERSION.SDK_INT >= 34 && spotifyService && consentAllowed()
            val bindingFlags = if (allowActivity) flags or Context.BIND_ALLOW_ACTIVITY_STARTS else flags
            val bound = super.bindService(service, conn, bindingFlags)
            if (bound) bindings.add(conn)
            if (BuildConfig.DEBUG && spotifyService) {
                Log.d("DreamSpotify", "Spotify service binding: action=${service.action}, bound=$bound, consentLaunch=$allowActivity")
            }
            bound
        }

    override fun unbindService(conn: ServiceConnection) {
        synchronized(bindings) {
            // Timeout cleanup can precede the SDK's late disconnect callback.
            if (bindings.remove(conn)) super.unbindService(conn)
        }
    }

    override fun startService(service: Intent): ComponentName? = synchronized(bindings) {
        check(!released) { "Spotify connection is no longer active." }
        super.startService(service)
    }

    @androidx.annotation.RequiresApi(26)
    override fun startForegroundService(service: Intent): ComponentName? = synchronized(bindings) {
        check(!released) { "Spotify connection is no longer active." }
        super.startForegroundService(service)
    }

    internal fun releaseBindings() {
        synchronized(bindings) {
            released = true
            bindings.toList().forEach { connection ->
                runCatching { super.unbindService(connection) }
            }
            bindings.clear()
        }
    }
}
