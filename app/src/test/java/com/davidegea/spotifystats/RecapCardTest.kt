package com.davidegea.spotifystats

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import com.davidegea.spotifystats.domain.model.*
import com.davidegea.spotifystats.ui.wrapped.RecapCardRenderer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = SpotifyStatsApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RecapCardTest {
    @Test fun createsPortraitPngAndSharesOnlyAContentUri() = runBlocking {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            RecapCardRenderer.share(activity, Recap(
                TimeRange(1_788_220_800_000, 1_790_812_799_999),
                OverviewStats(1847, 335_640_000, 312, 264),
                listOf(TrackRanking(1, "After Hours", "The Weeknd", 47, 15_480_000)),
                listOf(ArtistRanking(1, "The Weeknd", 182, 47_000_000)),
                listOf(AlbumRanking(1, "After Hours", "The Weeknd", 130, 38_000_000)),
                73,
            ))
            val chooser = shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_CHOOSER, chooser.action)
            @Suppress("DEPRECATION") val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            @Suppress("DEPRECATION") val uri = send.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)!!
            assertEquals("content", uri.scheme)
            assertEquals("image/png", send.type)
            assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            val image = File(activity.cacheDir, "recaps").listFiles()!!.maxBy { it.lastModified() }
            val bitmap = BitmapFactory.decodeFile(image.path)
            assertEquals(1080, bitmap.width); assertEquals(1920, bitmap.height)
            bitmap.recycle()
            File("build/reports").mkdirs()
            image.copyTo(File("build/reports/share-card.png"), overwrite = true)
        }
    }
}
