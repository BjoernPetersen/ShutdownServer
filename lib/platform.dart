import 'dart:io' as dart_io;

import 'package:injectable/injectable.dart';

const linuxEnv = Environment('linux');
const windowsEnv = Environment('windows');

enum Platform {
  linux(linuxEnv),
  windows(windowsEnv),
  ;

  final Environment environment;

  const Platform(this.environment);

  static Platform determine() {
    if (dart_io.Platform.isLinux) {
      return Platform.linux;
    } else if (dart_io.Platform.isWindows) {
      return Platform.windows;
    } else {
      throw UnsupportedError('Current platform is not supported');
    }
  }
}
