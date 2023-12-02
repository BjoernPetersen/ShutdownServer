import 'package:dotenv/dotenv.dart';
import 'package:injectable/injectable.dart';

@singleton
final class Config {
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
        seconds: env.requireInt(
          'SHUTDOWN_DELAY_SECONDS',
          defaultValue: 30,
        ),
      ),
      token: env.requireString('AUTH_TOKEN'),
    );
  }
}

sealed class ConfigError {
  final String key;

  ConfigError(this.key);
}

final class MissingConfigError extends ConfigError {
  MissingConfigError(super.key);

  @override
  String toString() {
    return 'Missing config key: $key';
  }
}

final class InvalidConfigError extends ConfigError {
  final String value;

  InvalidConfigError(super.key, this.value);

  @override
  String toString() {
    return 'Invalid value for config key "$key": "$value"';
  }
}

extension on DotEnv {
  String requireString(String key) {
    final value = this[key];
    if (value == null || value.isEmpty) {
      throw MissingConfigError(key);
    }
    return value;
  }

  int requireInt(
    String key, {
    int? defaultValue,
  }) {
    final stringValue = this[key];

    if (stringValue == null || stringValue.isEmpty) {
      if (defaultValue != null) {
        return defaultValue;
      } else {
        throw MissingConfigError(key);
      }
    }

    final value = int.tryParse(stringValue);
    if (value == null) {
      throw InvalidConfigError(key, stringValue);
    }
    return value;
  }
}
