package dev.gece.imaplayer

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.AdsManager
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


@RequiresApi(Build.VERSION_CODES.N)
internal class ImaPlayerView(
    private var context: Context,
    private var id: Int,
    private var args: Map<String, Any>,
    private var messenger: BinaryMessenger
) : PlatformView, Player.Listener {
    // Video Player
    private val playerView: PlayerView

    override fun getView(): View {
        return playerView
    }

    override fun dispose() {
        playerView.removeAllViews()
    }

    init {
        playerView = PlayerView(context)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.setShowShuffleButton(false)
        playerView.setShowSubtitleButton(false)
        playerView.setShowVrButton(false)
        playerView.controllerAutoShow = args["controller_auto_show"] as Boolean? ?: true
        playerView.controllerHideOnTouch = args["controller_hide_on_touch"] as Boolean? ?: true
        playerView.useController = args["show_playback_controls"] as Boolean? ?: true
        val imaManager = ImaPlayerManager.getInstance(context, messenger)
        playerView.player = imaManager.player
    }
}



