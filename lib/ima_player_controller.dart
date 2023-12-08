// ignore_for_file: constant_identifier_names

part of ima_player;

typedef ViewCreatedCallback = void Function();

class ImaPlayerController {
  final String videoUrl;
  final String? imaTag;
  final ImaPlayerOptions options;
  final ImaAdsLoaderSettings adsLoaderSettings;

  ImaPlayerController({
    required this.videoUrl,
    this.imaTag,
    this.options = const ImaPlayerOptions(),
    this.adsLoaderSettings = const ImaAdsLoaderSettings(),
  }) {
    attach();
  }

  MethodChannel? _methodChannel;
  EventChannel? _eventChannel;

  final _onPlayerEventController = StreamController<ImaPlayerEvents?>();
  late final onPlayerEvent = _onPlayerEventController.stream;

  final _onAdsEventController = StreamController<ImaAdsEvents>();
  late final onAdsEvent = _onAdsEventController.stream;

  StreamSubscription? _eventChannelListener;
  void attach() {
    _methodChannel = MethodChannel('gece.dev/imas_player_method_channel');
    _eventChannel = EventChannel('gece.dev/imas_player_event_channel');

    final stream = _eventChannel!.receiveBroadcastStream();

    _eventChannelListener = stream.listen(
      (event) {
        if (event is Map && event.containsKey('type')) {
          final value = event["value"];

          switch (event['type']) {
            case 'ads':
              if (value is Map) {
                _onAdsEventController.addError(value, StackTrace.current);
              } else {
                _onAdsEventController.add(
                  ImaAdsEvents.fromString(
                    (value as String?)?.toUpperCase().replaceAll(' ', '_'),
                  ),
                );
              }

              break;

            case 'player':
              _onPlayerEventController.add(
                ImaPlayerEvents.fromString(value),
              );
              break;
          }
        }
      },
    );
  }

  Future<void> onViewCreated() async {
    await _methodChannel?.invokeMethod('view_created');
  }

  Future<void> initPlayer() async {
    final creationParams = {
      'ima_tag': imaTag,
      'is_muted': options.muted,
      'is_mixed': options.isMixWithOtherMedia,
      'auto_play': options.autoPlay,
      'video_url': videoUrl,
      'controller_auto_show': options.controllerAutoShow,
      'controller_hide_on_touch': options.controllerHideOnTouch,
      'show_playback_controls': options.showPlaybackControls,
      'ads_loader_settings': adsLoaderSettings.toJson(),
    };
    await _methodChannel?.invokeMethod('initialize', creationParams);
  }

  Future<bool> play({String? videoUrl}) async {
    final result = await _methodChannel?.invokeMethod<bool>('play', videoUrl);
    return result ?? false;
  }

  Future<bool> pause() async {
    final result = await _methodChannel?.invokeMethod<bool>('pause');
    return result ?? false;
  }

  Future<bool> stop() async {
    final result = await _methodChannel?.invokeMethod<bool>('stop');
    return result ?? false;
  }

  Future<bool> seekTo(Duration duration) async {
    final result = await _methodChannel?.invokeMethod<bool>(
        'seek_to',
        Platform.isAndroid
            ? duration.inMilliseconds
            : duration.inMilliseconds / 1000);

    return result ?? false;
  }

  Future<bool> skipAd() async {
    final result = await _methodChannel?.invokeMethod<bool>('skip_ad');
    return result ?? false;
  }

  Future<bool> setVolume(double volume) async {
    final result = await _methodChannel?.invokeMethod<bool>(
      'set_volume',
      volume,
    );

    return result ?? false;
  }

  Future<double> getVolume() async {
    final result = await _methodChannel?.invokeMethod<double>(
      'get_volume',
    );

    return result ?? 1.0;
  }

  Future<bool> setSpeed(double volume) async {
    final result = await _methodChannel?.invokeMethod<bool>(
      'set_speed',
      volume,
    );

    return result ?? false;
  }

  Future<double> getSpeed() async {
    final result = await _methodChannel?.invokeMethod<double>(
      'get_speed',
    );

    return result ?? 1.0;
  }

  Future<ImaVideoInfo> getVideoInfo() async {
    final info = await _methodChannel?.invokeMapMethod<String, dynamic>(
      'get_video_info',
    );

    return ImaVideoInfo.fromJson(Map<String, dynamic>.from(info ?? {}));
  }

  Future<ImaAdInfo> getAdInfo() async {
    final info = await _methodChannel?.invokeMapMethod<String, dynamic>(
      'get_ad_info',
    );

    return ImaAdInfo.fromJson(Map<String, dynamic>.from(info ?? {}));
  }

  void dispose() {
    _methodChannel?.invokeMethod('dispose');
    _eventChannelListener?.cancel();
    _onAdsEventController.close();
    _onPlayerEventController.close();
  }
}
