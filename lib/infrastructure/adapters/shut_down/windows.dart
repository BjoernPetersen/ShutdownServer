import 'dart:ffi' as ffi;

import 'package:injectable/injectable.dart';
import 'package:shutdownserver/application/ports/shutdown_access.dart';

typedef Win32ShutdownC = ffi.Bool Function(
  ffi.Pointer lpMachineName,
  ffi.Pointer lpMessage,
  ffi.Uint32 dwTimeout,
  ffi.Bool bForceAppsClosed,
  ffi.Bool bRebootAfterShutdown,
  ffi.Uint32 dwReason,
);
typedef Win32ShutdownDart = bool Function(
  ffi.Pointer lpMachineName,
  ffi.Pointer lpMessage,
  int dwTimeout,
  bool bForceAppsClosed,
  bool bRebootAfterShutdown,
  int dwReason,
);

typedef Win32GetLastErrorC = ffi.Uint32 Function();
typedef Win32GetLastErrorDart = int Function();

@Injectable(as: ShutdownAccess)
class WindowsShutdownAccess implements ShutdownAccess {
  @override
  Future<void> shutDown(Duration delay) async {
    final lib = ffi.DynamicLibrary.process();
    final shutdown = lib.lookupFunction<Win32ShutdownC, Win32ShutdownDart>(
      'InitiateSystemShutdownExW',
    );

    final result = shutdown(
      ffi.nullptr,
      ffi.nullptr,
      delay.inSeconds,
      false,
      false,
      0x00040000,
    )

    if (!result) {
      final getLastError =
          lib.lookupFunction<Win32GetLastErrorC, Win32GetLastErrorDart>(
        'GetLastError',
      );
      final errorCode = getLastError();
      throw Exception('Could not initiate shutdown, got error code $errorCode');
    }
  }
}
