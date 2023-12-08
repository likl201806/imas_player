package dev.gece.imaplayer

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink

/** ImaPlugin */
class ImaPlayerPlugin : FlutterPlugin {

    private var imasPlayer: ImaPlayerManager? = null
    private var methodChannel: MethodChannel? = null
    private var eventChannel: EventChannel? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory(
            "gece.dev/imaplayer_view", ImaPlayerViewFactory(binding.binaryMessenger)
        )

        imasPlayer = ImaPlayerManager.getInstance(binding.applicationContext, binding.binaryMessenger)
        // 创建并设置 MethodChannel
        methodChannel = MethodChannel(binding.binaryMessenger, "gece.dev/imas_player_method_channel")
        methodChannel?.setMethodCallHandler { call, result ->
            // 根据方法名调用 ImasPlayer 的相应方法
            when (call.method) {
                "initialize" -> {
                    val args = call.arguments as Map<String, Any>?
                    imasPlayer?.initialize(args, result)
                }
                "play" -> imasPlayer?.play(call.arguments as String?, result)
                "pause" -> imasPlayer?.pause(result)
                "stop" -> imasPlayer?.stop(result)
                "view_created" -> imasPlayer?.viewCreated(result)
                "seek_to" -> {
                    val args = call.arguments as Map<String, Any>?
                    imasPlayer?.seekTo(args, result)
                }
                "set_volume" -> imasPlayer?.setVolume(call.arguments as Double?, result)
                "get_volume" -> imasPlayer?.getVolume(result)
                "set_speed" -> imasPlayer?.setSpeed(call.arguments as Double?, result)
                "get_speed" -> imasPlayer?.getSpeed(result)
                "get_video_info" -> imasPlayer?.getVideoInfo(result)
                "dispose" -> imasPlayer?.viewDispose(result)
                else -> result.notImplemented()
            }
        }

        // 创建并设置 EventChannel
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
        methodChannel?.setMethodCallHandler(null)
        eventChannel?.setStreamHandler(null)
        imasPlayer = null
    }
}

