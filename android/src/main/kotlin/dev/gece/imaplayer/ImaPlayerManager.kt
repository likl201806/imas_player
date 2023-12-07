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
class ImaPlayerManager private constructor(
    private val context: Context,
    private val messenger: BinaryMessenger
) : Player.Listener {
    // 定义 ExoPlayer 和其他相关属性
    public var eventSink: EventSink? = null

    // Video Player
    public lateinit var player: ExoPlayer

    // Passed arguments
    private var videoUrl: Uri? = null
    private var imaTag: Uri? = null
    private var isMuted: Boolean = true
    private var isMixed: Boolean = true
    private var autoPlay: Boolean = true

    companion object {
        private var instance: ImaPlayerManager? = null

        @RequiresApi(Build.VERSION_CODES.N)
        fun getInstance(
            context: Context,
            messenger: BinaryMessenger
        ): ImaPlayerManager {
            if (instance == null) {
                instance = ImaPlayerManager(context, messenger)
            }
            return instance!!
        }
    }

    fun initialize(args: Map<String, Any>?) {
        if (args != null){
            videoUrl = Uri.parse(args["video_url"] as String?)
            imaTag = Uri.parse(args["ima_tag"] as String?)
            isMuted = args["is_muted"] as Boolean? == true
            isMixed = args["is_mixed"] as Boolean? ?: true
            autoPlay = args["auto_play"] as Boolean? ?: true
        }else{
            isMuted = true
            isMixed = true
            autoPlay = true
        }

        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // Create an ExoPlayer and set it as the player for content and ads.
        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setDeviceVolumeControlEnabled(true)
            .build()

        player.playWhenReady = autoPlay
        player.setAudioAttributes(
            AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),
            !isMixed
        )

        if (isMuted) {
            player.volume = 0.0F
        }

        player.addListener(this)

        preparePlayer()
    }

    // 单例实现
    companion object {
        private var instance: ImasPlayer? = null

        @RequiresApi(Build.VERSION_CODES.N)
        fun getInstance(
            context: Context,
            args: Map<String, Any>?,
            messenger: BinaryMessenger
        ): ImasPlayer {
            if (instance == null) {
                instance = ImasPlayer(context, args, messenger)
            }
            return instance!!
        }
    }

    // 重写的 Player.Listener 方法
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            ExoPlayer.STATE_READY -> sendEvent("READY")
            ExoPlayer.STATE_BUFFERING -> sendEvent("BUFFERING")
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        sendEvent(if (isPlaying) "PLAYING" else "PAUSED")
    }

    private fun preparePlayer() {
        if (videoUrl != null && imaTag != null){
            val mediaItem = MediaItem.Builder().setUri(videoUrl)
                .setAdsConfiguration(imaTag?.let {
                    MediaItem.AdsConfiguration.Builder(it).build()
                }).build()

            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    public fun play(videoUrl: String?, result: MethodChannel.Result) {
        if (videoUrl != null) {
            this.videoUrl = Uri.parse(videoUrl)
            player.stop()
            player.clearMediaItems()
            preparePlayer()
        }

        player.playWhenReady = true
        result.success(true)
    }

    public fun pause(result: MethodChannel.Result) {
        player.pause()
        result.success(true)
    }

    public fun stop(result: MethodChannel.Result) {
        player.stop()
        result.success(true)
    }

    public fun seekTo(duration: Int?, result: MethodChannel.Result) {
        if (duration != null) {
            player.seekTo(duration.toLong())
        }

        result.success(duration != null)
    }

    public fun setVolume(value: Double?, result: MethodChannel.Result) {
        if (value != null) {
            player.volume = 0.0.coerceAtLeast(1.0.coerceAtMost(value)).toFloat()
        }

        result.success(value != null)
    }

    public fun getVolume(result: MethodChannel.Result) {
        val speed = player.volume.toDouble()
        result.success(speed)
    }

    public fun setSpeed(value: Double?, result: MethodChannel.Result) {
        if (value != null && value > 0.0) {
            // 确保速度值在有效范围内，大于 0
            val speed = 0.0.coerceAtLeast(value).toFloat()
            player.setPlaybackSpeed(speed)
            result.success(true)
        } else {
            result.success(false)
        }
    }

    public fun getSpeed(result: MethodChannel.Result) {
        val speed = player.playbackParameters.speed.toDouble()
        result.success(speed)
    }

    public fun viewCreated(result: MethodChannel.Result) {
        result.success(true)
    }

    public fun viewDispose(result: MethodChannel.Result) {
        result.success(true)
    }


    public fun getVideoInfo(result: MethodChannel.Result) {
        result.success(
            hashMapOf(
                "current_position" to if (player.isPlayingAd) 0.0 else roundForTwo(player.currentPosition.toDouble()),
                "total_duration" to roundForTwo(player.contentDuration.toDouble()),
                "is_playing" to (player.isPlaying && !player.isPlayingAd),
                "is_buffering" to (player.bufferedPercentage in 1..99),
                "height" to player.videoSize.height,
                "width" to player.videoSize.width
            )
        )
    }

    public fun roundForTwo(value: Double?): Double {
        return "%.1f".format((value ?: 0.0) / 1000).toDouble()
    }

    public fun sendEvent(value: Any?) {
        eventSink?.success(
            value
        )
    }
}
