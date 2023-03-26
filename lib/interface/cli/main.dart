import 'package:args/command_runner.dart';
import 'package:shutdownserver/application/app.dart';
import 'package:shutdownserver/init.dart';
import 'package:shutdownserver/interface/api/main.dart';

Future<void> main(List<String> args) async {
  final runner = CommandRunner('server', '')
    ..addCommand(_RunApi())
    ..addCommand(_ShutDown());

  await runner.run(args);
}

class _RunApi extends Command<void> {
  @override
  String get name => 'run-api';

  @override
  String get description => 'Serves the API';

  @override
  Future<void> run() async {
    await initialized((Api api) => api.serve());
  }
}

class _ShutDown extends Command<void> {
  @override
  String get name => 'shutdown';

  @override
  String get description => 'Schedules a shutdown';

  @override
  Future<void> run() async {
    await initialized((Application app) => app.shutDown());
  }
}
