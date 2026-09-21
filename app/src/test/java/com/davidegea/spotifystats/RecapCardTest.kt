package com.davidegea.spotifystats

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import com.davidegea.spotifystats.domain.model.AlbumRanking
import com.davidegea.spotifystats.domain.model.ArtistRanking
import com.davidegea.spotifystats.domain.model.OverviewStats
import com.davidegea.spotifystats.domain.model.Recap
import com.davidegea.spotifystats.domain.model.TimeRange
import com.davidegea.spotifystats.domain.model.TrackRanking
import com.davidegea.spotifystats.ui.wrapped.RecapCardRenderer
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = SpotifyStatsApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RecapCardTest {

    private fun recap() = Recap(
        TimeRange(1_788_220_800_000, 1_790_812_799_999),
        OverviewStats(1847, 335_640_000, 312, 264),
        listOf(
            TrackRanking(
                1,
                "After Hours",
                "The Weeknd",
                47,
                15_480_000,
            ),
        ),
        listOf(
            ArtistRanking(
                1,
                "The Weeknd",
                182,
                47_000_000,
            ),
        ),
        listOf(
            AlbumRanking(
                1,
                "After Hours",
                "The Weeknd",
                130,
                38_000_000,
            ),
        ),
        73,
    )

    @Test
    fun createsPortraitPngAndSharesOnlyAContentUri() {
        runBlocking {
            Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            RecapCardRenderer.share(activity, recap())

            val chooser = shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_CHOOSER, chooser.action)
            @Suppress("DEPRECATION")
            val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            @Suppress("DEPRECATION")
            val uri = send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
            assertEquals(Intent.ACTION_SEND, send.action)
            assertEquals("content", uri.scheme)
            assertEquals("image/png", send.type)
            assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)

            val image = File(activity.cacheDir, "recaps")
                .listFiles()
                .orEmpty()
                .firstOrNull { it.name.contains("overview") }
            assertNotNull("Expected a generated overview image", image)
            assertPortrait(requireNotNull(image))

            File("build/reports").mkdirs()
            image.copyTo(
                File("build/reports/share-card.png"),
                overwrite = true,
            )
            }
        }
    }

    @Test
    fun rendersAndSharesFourCardStorySequenceUsingContentUris() {
        runBlocking {
            Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            val files = RecapCardRenderer.renderSequence(activity, recap())

            assertEquals(4, files.size)
            assertTrue(files.map(File::getName).any { it.contains("overview") })
            assertTrue(files.map(File::getName).any { it.contains("track") })
            assertTrue(files.map(File::getName).any { it.contains("artist") })
            assertTrue(files.map(File::getName).any { it.contains("story") })
            files.forEach(::assertPortrait)

            RecapCardRenderer.shareSequence(activity, recap())

            val chooser = shadowOf(activity).nextStartedActivity
            assertEquals(Intent.ACTION_CHOOSER, chooser.action)
            @Suppress("DEPRECATION")
            val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            assertEquals(Intent.ACTION_SEND_MULTIPLE, send.action)
            assertEquals("image/png", send.type)
            assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)

            @Suppress("DEPRECATION")
            val uris = send.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            assertNotNull(uris)
            assertEquals(4, uris!!.size)
            assertTrue(uris.all { it.scheme == "content" })
            assertEquals(4, send.clipData?.itemCount)
            }
        }
    }

    private fun assertPortrait(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.path)
        assertNotNull("Expected a decodable PNG: " + file.name, bitmap)
        assertEquals(1080, bitmap.width)
        assertEquals(1920, bitmap.height)
        bitmap.recycle()
    }
}
