import 'package:injectable/injectable.dart';
import 'package:shutdownserver/application/ports/shutdown_access.dart';
import 'package:shutdownserver/platform.dart';

@linuxEnv
@Injectable(as: ShutdownAccess)
class LinuxShutdownAccess implements ShutdownAccess {
  @override
  Future<void> shutDown(Duration delay) {
    // TODO: implement shutDown
    throw UnimplementedError();
  }
}
