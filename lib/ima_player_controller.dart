// ignore_for_file: constant_identifier_names

part of ima_player;

typedef ViewCreatedCallback = void Function();

class ImaPlayerController {
  final String videoUrl;
  final String? imaTag;
  final ImaPlayerOptions options;

  ImaPlayerController({
    required this.videoUrl,
    this.imaTag,
    this.options = const ImaPlayerOptions(),
  }) {
    attach();
  }

  MethodChannel? _methodChannel;
  EventChannel? _eventChannel;

  final _onPlayerEventController = StreamController<ImaPlayerEvents?>();
  late final onPlayerEvent = _onPlayerEventController.stream;

  StreamSubscription? _eventChannelListener;
  void attach() {
    _methodChannel = MethodChannel('gece.dev/imas_player_method_channel');
    _eventChannel = EventChannel('gece.dev/imas_player_event_channel');

    final stream = _eventChannel!.receiveBroadcastStream();

    _eventChannelListener = stream.listen(
      (event) {
        _onPlayerEventController.add(
          ImaPlayerEvents.fromString(event),
        );
      },
    );
  }

  Future<bool> initPlayer() async {
    final creationParams = {
      'ima_tag': imaTag,
      'is_muted': options.muted,
      'is_mixed': options.isMixWithOtherMedia,
      'auto_play': options.autoPlay,
      'video_url': videoUrl,
      'controller_auto_show': options.controllerAutoShow,
      'controller_hide_on_touch': options.controllerHideOnTouch,
      'show_playback_controls': options.showPlaybackControls,
    };
    final result =
        await _methodChannel?.invokeMethod('initialize', creationParams);
    return result ?? false;
  }

  Future<bool> play({String? videoUrl}) async {
    final result = await _methodChannel?.invokeMethod<bool>('play', videoUrl);
    return result ?? false;
  }

  Future<bool> pause() async {
    final result = await _methodChannel?.invokeMethod<bool>('pause');
    return result ?? false;
  }

  Future<bool> isPlaying() async {
    final result = await _methodChannel?.invokeMethod<bool>('isPlaying');
    return result ?? true;
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

  Future<bool> setSpeed(double speed) async {
    final result = await _methodChannel?.invokeMethod<bool>(
      'set_speed',
      speed,
    );

    return result ?? false;
  }

  Future<double> getSpeed() async {
    final result = await _methodChannel?.invokeMethod<double>(
      'get_speed',
    );

    return result ?? 1.0;
  }

  Future<Map<String, dynamic>> getEqualizerSettings() async {
    final info = await _methodChannel?.invokeMapMethod<String, dynamic>(
      'get_equalizer_info',
    );

    return Map<String, dynamic>.from(info ?? {});
  }

  Future<void> setEqualizerBand(int index, int bandLevel) async {
    print("---ima enter setEqualizerBand");
    final result = await _methodChannel?.invokeMethod(
        'set_equalizer_band', {'index': index, 'bandLevel': bandLevel});
    print("---ima result: $result");
    return result;
  }

  Future<ImaVideoInfo> getVideoInfo() async {
    final info = await _methodChannel?.invokeMapMethod<String, dynamic>(
      'get_video_info',
    );

    return ImaVideoInfo.fromJson(Map<String, dynamic>.from(info ?? {}));
  }

  void disposePlayer() {
    _methodChannel?.invokeMethod('disposePlayer');
    _eventChannelListener?.cancel();
    _onPlayerEventController.close();
  }
}
