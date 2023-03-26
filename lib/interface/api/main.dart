import 'dart:convert';
import 'dart:io';

import 'package:injectable/injectable.dart';
import 'package:shelf_plus/shelf_plus.dart';
import 'package:shutdownserver/application/app.dart';
import 'package:shutdownserver/interface/api/auth.dart';
import 'package:shutdownserver/interface/api/cors.dart';
import 'package:shutdownserver/interface/api/exceptions.dart';
import 'package:shutdownserver/interface/api/shutdown.dart';

@injectable
class Api {
  final Application app;
  final AuthMiddleware authMiddleware;
  final RouterPlus _router;

  Api(
    this.app,
    this.authMiddleware,
  ) : _router = Router().plus {
    _router.use(ExceptionHandlingMiddleware());
    _router.get('/health/live', health);

    _registerShutdown(authMiddleware);
  }

  void _registerShutdown(Middleware middleware) {
    final api = ShutdownApi(app);

    _router.delete('/shutdown', api.deleteShutdown, use: middleware);
    _router.put('/shutdown', api.putShutdown, use: middleware);
  }

  Response health(Request request) {
    return Response.ok(jsonEncode({'ok': true}));
  }

  Future<void> serve() async {
    await shelfRun(
      () => corsMiddleware().addHandler(_router),
      defaultBindAddress: InternetAddress.anyIPv4,
    );
  }
}
