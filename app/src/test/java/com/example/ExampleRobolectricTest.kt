package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import com.example.ui.audio.CyberpunkAudioEngine
import com.example.ui.audio.SoundEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("APS Miner", appName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cyberpunk audio engine mute and sound effect state management`() {
        val testScope = TestScope(UnconfinedTestDispatcher())
        val audioEngine = CyberpunkAudioEngine(testScope)

        // Default state: not muted
        assertFalse(audioEngine.isMuted())

        // Set muted
        audioEngine.setMuted(true)
        assertTrue(audioEngine.isMuted())

        // Sound effect triggers when muted without crash
        audioEngine.playUiSound(SoundEffect.CLICK)
        audioEngine.playUiSound(SoundEffect.OVERCLOCK)

        // Set unmuted
        audioEngine.setMuted(false)
        assertFalse(audioEngine.isMuted())

        // Start and stop ambient loop safely
        audioEngine.startAmbientMiningLoop(isOverclocked = false)
        audioEngine.setOverclockAmbient(true)
        audioEngine.setOverclockAmbient(false)
        audioEngine.stopAmbientMiningLoop()

        // App lifecycle transitions
        audioEngine.pauseAmbientOnAppPause()
        audioEngine.resumeAmbientOnAppResume()

        // Clean release
        audioEngine.release()
    }

    @Test
    fun `preferences manager sound muted persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.isSoundMuted = true
        assertTrue(prefs.isSoundMuted)

        prefs.isSoundMuted = false
        assertFalse(prefs.isSoundMuted)
    }
}

