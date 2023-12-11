package dev.gece.imaplayer
import androidx.media3.exoplayer.ExoPlayer
import android.media.audiofx.Equalizer

class ImaPlayerEqualizer(private val player: ExoPlayer) {
    private var equalizer: Equalizer? = null

    init {
        initializeEqualizer()
    }

    private fun initializeEqualizer() {
        val audioSessionId = player.audioSessionId
        if (audioSessionId > 0) {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        }
    }

    fun getEqualizerSettings(): Map<String, Any>? {
        equalizer?.let { eq ->
            val settings = mutableMapOf<String, Any>()
            val numberOfBands = eq.numberOfBands
            settings["numberOfBands"] = numberOfBands

            for (i in 0 until numberOfBands) {
                val bandFreqRange = eq.getBandFreqRange(i.toShort())
                val bandLevel = eq.getBandLevel(i.toShort())
                settings["band_frequency_range_$i "] = bandFreqRange
                settings["band_level$i"] = bandLevel
            }
            return settings
        }
        return null
    }

    fun release() {
        equalizer?.release()
    }
}