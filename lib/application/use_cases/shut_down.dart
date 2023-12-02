import 'package:injectable/injectable.dart';
import 'package:logger/logger.dart';
import 'package:shutdownserver/application/ports/shutdown_access.dart';
import 'package:shutdownserver/config.dart';

@injectable
final class ShutDown {
  final Logger _logger;
  final ShutdownAccess _shutdownAccess;
  final Duration _shutdownDelay;

  ShutDown(this._logger, this._shutdownAccess, Config config)
      : _shutdownDelay = config.shutdownDelay;

  Future<DateTime> call() async {
    _logger.i('Scheduling shutdown');
    final now = DateTime.now();

    try {
      await _shutdownAccess.shutDown(_shutdownDelay);
    } catch (e) {
      _logger.e('Could not schedule shutdown', error: e);
      rethrow;
    }

    return now.add(_shutdownDelay);
  }
}
