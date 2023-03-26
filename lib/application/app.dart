import 'package:injectable/injectable.dart';
import 'package:shutdownserver/application/use_cases/cancel_shutdown.dart';
import 'package:shutdownserver/application/use_cases/shut_down.dart';

@injectable
class Application {
  final CancelShutdown cancelShutdown;
  final ShutDown shutDown;

  Application(
    this.cancelShutdown,
    this.shutDown,
  );
}
