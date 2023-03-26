import 'package:shelf/shelf.dart';

class ExceptionHandlingMiddleware {
  Future<Response> _handle(Request request, Handler next) async {
    try {
      return await next(request);
    } on UnauthorizedException {
      return Response.unauthorized(null);
    }
  }

  Handler call(Handler next) {
    return (request) => _handle(request, next);
  }
}

class UnauthorizedException implements Exception {}
