package dev.gece.imaplayer

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
//import androidx.media3.common.Player
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
//import androidx.media3.ui.PlayerView
import io.flutter.plugin.common.BinaryMessenger
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
        playerView.controllerAutoShow = args["controller_auto_show"] as Boolean? ?: true
        playerView.controllerHideOnTouch = args["controller_hide_on_touch"] as Boolean? ?: true
        playerView.useController = args["show_playback_controls"] as Boolean? ?: true
        val imaManager = ImaPlayerManager.getInstance(context, messenger)
        playerView.player = imaManager.player
        if (playerView.player == null) {
            println("---android player is null")
        }
    }
}



