import 'dart:io';

import 'package:json_annotation/json_annotation.dart';
import 'package:shelf/shelf.dart';
import 'package:shutdownserver/application/app.dart';
import 'package:shutdownserver/interface/api/util.dart';

part 'shutdown.g.dart';

class ShutdownApi {
  final Application app;

  ShutdownApi(this.app);

  Future<ShutdownResponse> putShutdown(Request request) async {
    final shutdownTime = await app.shutDown();
    return ShutdownResponse(
      shutdownTime: shutdownTime,
    );
  }

  Future<Response> deleteShutdown(Request request) async {
    await app.cancelShutdown();
    return Response(HttpStatus.noContent);
  }
}

@JsonSerializable(createFactory: false)
class ShutdownResponse {
  final DateTime shutdownTime;

  ShutdownResponse({
    required this.shutdownTime,
  });

  Json toJson() => _$ShutdownResponseToJson(this);
}
