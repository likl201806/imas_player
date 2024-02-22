package dev.gece.imaplayer

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
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
    private val playerView: PlayerView = ImaPlayerManager.getInstance(context, messenger).getPlayerView()

    override fun getView(): View {
        return playerView
    }

    override fun dispose() {
        println("---play playview dispose")
        // 在适当的时机释放PlayerView
        ImaPlayerManager.getInstance(context, messenger).releasePlayerView()
    }
}



