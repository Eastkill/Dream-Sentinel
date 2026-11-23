package com.example.dream_sentinel.spotify

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dream_sentinel.system.spotify.SpotifyBindingContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifyBindingContextTest {
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) = Unit
        override fun onServiceDisconnected(name: ComponentName) = Unit
    }

    @Test
    fun actualSdkBindingReceivesTheConsentFlagOnNewAndroid() {
        val base = RecordingContext()
        val wrapped = SpotifyBindingContext(base) { true }
        assertTrue(bindThroughSdk(wrapped))
        val expected = 65 or if (Build.VERSION.SDK_INT >= 34) Context.BIND_ALLOW_ACTIVITY_STARTS else 0
        assertEquals(expected, base.lastFlags)
        assertEquals("com.spotify.mobile.appprotocol.action.BIND_PROTOCOL_SERVICE", base.lastAction)
        wrapped.releaseBindings()
        assertEquals(1, base.unbindCalls)
    }

    @Test
    fun silentConnectionDoesNotGrantActivityStarts() {
        val base = RecordingContext()
        val wrapped = SpotifyBindingContext(base) { false }
        assertTrue(bindThroughSdk(wrapped))
        assertEquals(65, base.lastFlags)
        wrapped.releaseBindings()
    }

    @Test
    fun permissionIsNotGrantedToAnotherPackageOrUnknownAction() {
        val base = RecordingContext()
        val wrapped = SpotifyBindingContext(base) { true }
        assertTrue(bindThroughSdk(wrapped, "example.other.service"))
        assertEquals(65, base.lastFlags)
        wrapped.bindService(Intent("unknown.action").setPackage("com.spotify.music"), connection, 65)
        assertEquals(65, base.lastFlags)
        wrapped.releaseBindings()
    }

    @Test
    fun releasePreventsLateBindingAndToleratesLateSdkDisconnect() {
        val base = RecordingContext()
        val wrapped = SpotifyBindingContext(base) { true }
        assertTrue(bindThroughSdk(wrapped))
        wrapped.releaseBindings()
        wrapped.unbindService(connection)
        wrapped.releaseBindings()
        assertEquals(1, base.unbindCalls)
        val service = Intent("com.spotify.mobile.appprotocol.action.BIND_PROTOCOL_SERVICE")
            .setPackage("com.spotify.music")
        assertFalse(wrapped.bindService(service, connection, 65))
        assertTrue(runCatching { bindThroughSdk(wrapped) }.isFailure)
        assertEquals(1, base.bindCalls)
        assertEquals(0, base.startCalls)
    }

    @Test
    fun releasedContextCannotStartTheSdkLegacyForegroundService() {
        val base = RecordingContext()
        val wrapped = SpotifyBindingContext(base) { true }
        wrapped.releaseBindings()
        val service = Intent("com.spotify.mobile.appprotocol.action.START_APP_PROTOCOL_SERVICE")
            .setPackage("com.spotify.music")
        assertTrue(runCatching { wrapped.startService(service) }.isFailure)
        if (Build.VERSION.SDK_INT >= 26) {
            assertTrue(runCatching { wrapped.startForegroundService(service) }.isFailure)
        }
        assertEquals(0, base.startCalls)
    }

    @Test
    fun consentAvailabilityIsCheckedAtBindingTime() {
        val base = RecordingContext()
        var allowed = true
        val wrapped = SpotifyBindingContext(base) { allowed }
        allowed = false
        assertTrue(bindThroughSdk(wrapped))
        assertEquals(65, base.lastFlags)
        wrapped.releaseBindings()
    }

    private fun bindThroughSdk(context: Context, packageName: String = "com.spotify.music"): Boolean {
        // Exercise the shipped AAR, including its getApplicationContext() call.
        val binder = Class.forName("com.spotify.android.appremote.internal.SpotifyServiceBinder")
        val method = binder.getDeclaredMethod(
            "bindService", Context::class.java, ServiceConnection::class.java, String::class.java,
        ).apply { isAccessible = true }
        return method.invoke(null, context, connection, packageName) as Boolean
    }

    private class RecordingContext : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        var bindCalls = 0
        var unbindCalls = 0
        var lastFlags = 0
        var lastAction: String? = null
        var startCalls = 0
        override fun getApplicationContext(): Context = this
        override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
            bindCalls++
            lastFlags = flags
            lastAction = service.action
            return true
        }
        override fun unbindService(conn: ServiceConnection) { unbindCalls++ }
        override fun startService(service: Intent): ComponentName {
            startCalls++
            return ComponentName("com.spotify.music", "TestService")
        }
        override fun startForegroundService(service: Intent): ComponentName = startService(service)
    }
}
