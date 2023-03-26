import 'package:dotenv/dotenv.dart';
import 'package:injectable/injectable.dart';

@singleton
class Config {
  final Duration shutdownDelay;
  final String token;

  Config({
    required this.shutdownDelay,
    required this.token,
  });

  @factoryMethod
  factory Config.fromEnv() {
    final env = DotEnv(includePlatformEnvironment: true);
    env.load();
    return Config(
      shutdownDelay: Duration(
        seconds: int.parse(env['SHUTDOWN_DELAY_SECONDS']!),
      ),
      token: env['AUTH_TOKEN']!,
    );
  }
}
