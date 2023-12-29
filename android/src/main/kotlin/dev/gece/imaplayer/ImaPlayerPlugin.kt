package dev.gece.imaplayer

import android.os.Build
import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

/** ImaPlayerPlugin */
class ImaPlayerPlugin : FlutterPlugin {

    private var imasPlayer: ImaPlayerManager? = null
    private var methodChannel: MethodChannel? = null
    private var eventChannel: EventChannel? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory(
            "gece.dev/imaplayer_view", ImaPlayerViewFactory(binding.binaryMessenger)
        )
        // 获取 ImaPlayerManager 实例
        imasPlayer = ImaPlayerManager.getInstance(binding.applicationContext, binding.binaryMessenger)
        // 注册 Flutter 插件和 MethodChannel
        methodChannel = MethodChannel(binding.binaryMessenger, "gece.dev/imas_player_method_channel")
        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                // 处理不同的方法调用
                "initialize" -> {
                    val args = call.arguments as Map<String, Any>?
                    imasPlayer?.initialize(args, result)
                }
                "play" -> {
                    val videoUrl = call.arguments as String?
                    imasPlayer?.play(videoUrl, result)
                }
                "setMediaUrl" -> {
                    val videoUrl = call.arguments as String?
                    imasPlayer?.setMediaUrl(videoUrl, result)
                }
                "pause" -> imasPlayer?.pause(result)
                "stop" -> imasPlayer?.stop(result)
                "isPlaying" -> imasPlayer?.isPlaying(result)
                "seek_to" -> imasPlayer?.seekTo(call.arguments as Int?, result)
                "set_volume" -> imasPlayer?.setVolume(call.arguments as Double?, result)
                "get_volume" -> imasPlayer?.getVolume(result)
                "set_speed" -> imasPlayer?.setSpeed(call.arguments as Double?, result)
                "get_speed" -> imasPlayer?.getSpeed(result)
                "get_equalizer_info" -> imasPlayer?.getEqualizerSettings(result)
                "set_equalizer_band" -> imasPlayer?.setEqualizerSingleBand(call.arguments as Map<String, Any>?, result)
                "get_video_info" -> imasPlayer?.getVideoInfo(result)
                "disposePlayer" -> imasPlayer?.disposePlayer(result)
                else -> result.notImplemented()
            }
        }

        // 注册 EventChannel
        eventChannel = EventChannel(binding.binaryMessenger, "gece.dev/imas_player_event_channel")
        eventChannel?.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink?) {
                imasPlayer?.eventSink = events
            }

            override fun onCancel(arguments: Any?) {
                imasPlayer?.eventSink = null
            }
        })
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        // 清理资源
        methodChannel?.setMethodCallHandler(null)
        eventChannel?.setStreamHandler(null)
        imasPlayer = null
    }
}

class ImaPlayerViewFactory(private val messenger: BinaryMessenger) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return ImaPlayerView(
            context, id, args as Map<String, Any>, messenger
        )
    }
}
