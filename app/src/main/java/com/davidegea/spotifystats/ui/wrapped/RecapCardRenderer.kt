package com.davidegea.spotifystats.ui.wrapped

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.davidegea.spotifystats.domain.model.Recap
import com.davidegea.spotifystats.ui.components.listeningTime
import com.davidegea.spotifystats.ui.components.rangeLabel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RecapCardRenderer {
    suspend fun share(context: Context, recap: Recap) {
        val file = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "recaps").apply { mkdirs() }
            directory.listFiles()?.filter { it.lastModified() < System.currentTimeMillis() - 86_400_000 }?.forEach { it.delete() }
            val output = File(directory, "wrapped-${System.currentTimeMillis()}.png")
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            try {
                val canvas = Canvas(bitmap)
                val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
                paint.shader = LinearGradient(0f, 0f, 1080f, 1920f, Color.rgb(10, 30, 36), Color.rgb(32, 70, 62), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, 1080f, 1920f, paint)
                paint.shader = null
                fun line(text: String, y: Float, size: Float, accent: Boolean = false) {
                    paint.color = if (accent) Color.rgb(130, 239, 173) else Color.WHITE
                    paint.textSize = size
                    paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
                    canvas.drawText(TextUtils.ellipsize(text, paint, 900f, TextUtils.TruncateAt.END).toString(), 90f, y, paint)
                }
                line("YOUR MUSIC STORY", 160f, 42f, true)
                line(rangeLabel(recap.range), 245f, 36f)
                line(recap.overview.totalPlays.toString(), 480f, 156f, true)
                line("recorded listening events", 550f, 42f)
                line(listeningTime(recap.overview.totalListeningMs), 695f, 85f)
                line("${recap.overview.uniqueTracks} songs · ${recap.overview.uniqueArtists} artists", 765f, 38f)
                line("01 / TOP SONG", 940f, 30f, true)
                line(recap.tracks.firstOrNull()?.name ?: "—", 1020f, 58f)
                line("02 / TOP ARTIST", 1160f, 30f, true)
                line(recap.artists.firstOrNull()?.name ?: "—", 1240f, 58f)
                line("03 / TOP ALBUM", 1380f, 30f, true)
                line(recap.albums.firstOrNull()?.name ?: "—", 1460f, 58f)
                line("${recap.discoveries} artists first recorded in this period", 1620f, 35f)
                line("SPOTIFY STATS · MADE ON MY DEVICE", 1790f, 28f, true)
                line("Based on imported history · unofficial app", 1845f, 26f)
                output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { bitmap.recycle() }
            output
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.recaps", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri("Wrapped", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your Wrapped"))
    }
}
