part of ima_player;

class ImaVideoInfo {
  ImaVideoInfo.fromJson(Map<String, dynamic> json)
      : totalDuration = json['total_duration'] ?? 0.0,
        bufferedPosition = json['buffered_position'] ?? 0.0,
        currentPosition = json['current_position'] ?? 0.0,
        size = Size(
          (json['width'] ?? 0).toDouble(),
          (json['height'] ?? 0).toDouble(),
        );

  final double totalDuration;
  final double bufferedPosition;
  final double currentPosition;
  final Size size;

  @override
  String toString() =>
      'ImaPlayerInfo(totalDuration: $totalDuration, bufferedPosition: $bufferedPosition, currentPosition: $currentPosition, size: $size)';
}
