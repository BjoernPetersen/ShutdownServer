import 'package:injectable/injectable.dart';
import 'package:shelf_plus/shelf_plus.dart';
import 'package:shutdownserver/config.dart';
import 'package:shutdownserver/interface/api/exceptions.dart';

String _getTokenFromHeader(Request request) {
  final authHeader = request.headers['Authorization'];
  final prefixLength = 'Bearer '.length;
  if (authHeader == null || authHeader.length < prefixLength) {
    throw UnauthorizedException();
  }
  return authHeader.substring(prefixLength);
}

@injectable
class AuthMiddleware {
  final String _token;

  AuthMiddleware(Config config) : _token = config.token;

  Future<Response> _authenticate(Request request, Handler next) async {
    final token = _getTokenFromHeader(request);

    if (token != _token) {
      throw UnauthorizedException();
    }

    return await next(request);
  }

  Handler call(Handler next) {
    return (request) => _authenticate(request, next);
  }
}
