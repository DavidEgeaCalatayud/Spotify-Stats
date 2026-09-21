package com.davidegea.spotifystats.ui.wrapped

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.davidegea.spotifystats.R
import com.davidegea.spotifystats.domain.model.Recap
import com.davidegea.spotifystats.ui.components.listeningTime
import com.davidegea.spotifystats.ui.components.rangeLabel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RecapCardRenderer {

    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val CONTENT_LEFT = 90f
    private const val CONTENT_WIDTH = 900f
    private const val CACHE_MAX_AGE_MS = 86_400_000L

    private enum class StoryCard(
        val fileSuffix: String,
        val topColor: Int,
        val bottomColor: Int,
    ) {
        Overview(
            fileSuffix = "overview",
            topColor = Color.rgb(10, 30, 36),
            bottomColor = Color.rgb(32, 70, 62),
        ),
        Track(
            fileSuffix = "track",
            topColor = Color.rgb(22, 24, 49),
            bottomColor = Color.rgb(53, 42, 92),
        ),
        Artist(
            fileSuffix = "artist",
            topColor = Color.rgb(43, 20, 37),
            bottomColor = Color.rgb(91, 38, 69),
        ),
        Story(
            fileSuffix = "story",
            topColor = Color.rgb(47, 31, 12),
            bottomColor = Color.rgb(97, 69, 27),
        ),
    }

    suspend fun share(
        context: Context,
        recap: Recap,
    ) {
        val file = renderSequence(context, recap).first()
        val uri = contentUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Wrapped", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.wrapped_share_chooser),
            ),
        )
    }

    suspend fun shareSequence(
        context: Context,
        recap: Recap,
    ) {
        val files = renderSequence(context, recap)
        val uris = ArrayList(files.map { contentUri(context, it) })
        val first = uris.first()

        val intent = buildSequenceShareIntent(uris)
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.wrapped_share_chooser),
            ),
        )
    }

    internal fun buildSequenceShareIntent(
        uris: ArrayList<Uri>,
    ): Intent {
        require(uris.isNotEmpty()) { "At least one story URI is required" }
        return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/png"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            clipData = ClipData.newRawUri("Wrapped", uris.first()).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    internal suspend fun renderSequence(
        context: Context,
        recap: Recap,
    ): List<File> = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "recaps").apply { mkdirs() }
        cleanOldCards(directory)

        val generation = System.currentTimeMillis().toString()
        StoryCard.entries.map { card ->
            val output = File(
                directory,
                "wrapped-" + generation + "-" + card.fileSuffix + ".png",
            )
            renderCard(
                context = context,
                recap = recap,
                card = card,
                output = output,
            )
            output
        }
    }

    private fun renderCard(
        context: Context,
        recap: Recap,
        card: StoryCard,
        output: File,
    ) {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            drawBackground(canvas, paint, card)
            when (card) {
                StoryCard.Overview -> drawOverview(context, recap, canvas, paint)
                StoryCard.Track -> drawTrack(context, recap, canvas, paint)
                StoryCard.Artist -> drawArtist(context, recap, canvas, paint)
                StoryCard.Story -> drawStory(context, recap, canvas, paint)
            }
            output.outputStream().use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun drawBackground(
        canvas: Canvas,
        paint: TextPaint,
        card: StoryCard,
    ) {
        paint.shader = LinearGradient(
            0f,
            0f,
            WIDTH.toFloat(),
            HEIGHT.toFloat(),
            card.topColor,
            card.bottomColor,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
        paint.shader = null
    }

    private fun drawOverview(
        context: Context,
        recap: Recap,
        canvas: Canvas,
        paint: TextPaint,
    ) {
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_overview),
            150f,
            34f,
            accent = true,
        )
        line(
            canvas,
            paint,
            rangeLabel(recap.range, context.getString(R.string.period_all_time)),
            235f,
            34f,
        )
        line(
            canvas,
            paint,
            recap.overview.totalPlays.toString(),
            520f,
            164f,
            accent = true,
        )
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_recorded_events),
            595f,
            42f,
        )
        line(
            canvas,
            paint,
            listeningTime(recap.overview.totalListeningMs),
            780f,
            86f,
        )
        line(
            canvas,
            paint,
            context.getString(
                R.string.wrapped_card_songs_artists,
                recap.overview.uniqueTracks,
                recap.overview.uniqueArtists,
            ),
            860f,
            40f,
        )
        line(
            canvas,
            paint,
            context.getString(
                R.string.wrapped_card_discoveries_count,
                recap.discoveries,
            ),
            1080f,
            46f,
            accent = true,
        )
        footer(context, canvas, paint)
    }

    private fun drawTrack(
        context: Context,
        recap: Recap,
        canvas: Canvas,
        paint: TextPaint,
    ) {
        val track = recap.tracks.firstOrNull()
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_top_song_story),
            150f,
            34f,
            accent = true,
        )
        line(
            canvas,
            paint,
            track?.name ?: "—",
            500f,
            76f,
        )
        track?.artistName?.let {
            line(canvas, paint, it, 610f, 46f, accent = true)
        }
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_plays, track?.plays ?: 0),
            845f,
            50f,
        )
        line(
            canvas,
            paint,
            context.getString(
                R.string.wrapped_card_listening_time,
                listeningTime(track?.listeningMs ?: 0),
            ),
            930f,
            42f,
        )
        line(
            canvas,
            paint,
            rangeLabel(recap.range, context.getString(R.string.period_all_time)),
            1120f,
            34f,
        )
        footer(context, canvas, paint)
    }

    private fun drawArtist(
        context: Context,
        recap: Recap,
        canvas: Canvas,
        paint: TextPaint,
    ) {
        val artist = recap.artists.firstOrNull()
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_top_artist_story),
            150f,
            34f,
            accent = true,
        )
        line(
            canvas,
            paint,
            artist?.name ?: "—",
            520f,
            82f,
        )
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_plays, artist?.plays ?: 0),
            760f,
            52f,
            accent = true,
        )
        line(
            canvas,
            paint,
            context.getString(
                R.string.wrapped_card_listening_time,
                listeningTime(artist?.listeningMs ?: 0),
            ),
            850f,
            42f,
        )
        line(
            canvas,
            paint,
            rangeLabel(recap.range, context.getString(R.string.period_all_time)),
            1080f,
            34f,
        )
        footer(context, canvas, paint)
    }

    private fun drawStory(
        context: Context,
        recap: Recap,
        canvas: Canvas,
        paint: TextPaint,
    ) {
        val album = recap.albums.firstOrNull()
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_discovery_story),
            150f,
            34f,
            accent = true,
        )
        line(
            canvas,
            paint,
            recap.discoveries.toString(),
            500f,
            164f,
            accent = true,
        )
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_discoveries_label),
            575f,
            42f,
        )
        line(
            canvas,
            paint,
            context.getString(
                R.string.wrapped_card_top_album_story,
                album?.name ?: "—",
            ),
            820f,
            50f,
        )
        album?.artistName?.let {
            line(canvas, paint, it, 905f, 40f)
        }
        line(
            canvas,
            paint,
            rangeLabel(recap.range, context.getString(R.string.period_all_time)),
            1110f,
            34f,
        )
        footer(context, canvas, paint)
    }

    private fun footer(
        context: Context,
        canvas: Canvas,
        paint: TextPaint,
    ) {
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_brand),
            1780f,
            28f,
            accent = true,
        )
        line(
            canvas,
            paint,
            context.getString(R.string.wrapped_card_disclaimer),
            1840f,
            26f,
        )
    }

    private fun line(
        canvas: Canvas,
        paint: TextPaint,
        text: String,
        y: Float,
        size: Float,
        accent: Boolean = false,
    ) {
        paint.color = if (accent) Color.rgb(130, 239, 173) else Color.WHITE
        paint.textSize = size.coerceAtLeast(26f)
        paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        val safeText = TextUtils.ellipsize(
            text,
            paint,
            CONTENT_WIDTH,
            TextUtils.TruncateAt.END,
        ).toString()
        canvas.drawText(safeText, CONTENT_LEFT, y, paint)
    }

    private fun contentUri(
        context: Context,
        file: File,
    ): Uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".recaps",
        file,
    )

    private fun cleanOldCards(directory: File) {
        val cutoff = System.currentTimeMillis() - CACHE_MAX_AGE_MS
        directory.listFiles()
            .orEmpty()
            .filter { it.lastModified() < cutoff }
            .forEach(File::delete)
    }
}
