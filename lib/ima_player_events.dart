// ignore_for_file: constant_identifier_names

part of ima_player;

enum ImaPlayerEvents {
  READY,
  BUFFERING,
  PLAYING,
  PAUSED;

  static ImaPlayerEvents? fromString(String? str) {
    for (final value in values) {
      if (value.name == str) {
        return value;
      }
    }

    return null;
  }
}
