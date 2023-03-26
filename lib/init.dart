import 'dart:async';

import 'package:get_it/get_it.dart';
import 'package:injectable/injectable.dart';
import 'package:shutdownserver/init.config.dart';

@InjectableInit(
  throwOnMissingDependencies: true,
)
Future<void> initialized<T extends Object>(
  FutureOr<void> Function(T) withT,
) async {
  final getIt = GetIt.asNewInstance().init();
  await withT(getIt());
}
