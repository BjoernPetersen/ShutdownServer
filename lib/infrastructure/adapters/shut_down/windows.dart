import 'dart:ffi' as ffi;

import 'package:injectable/injectable.dart';
import 'package:shutdownserver/application/ports/shutdown_access.dart';

typedef GetLastErrorC = ffi.Uint32 Function();
typedef GetLastErrorDart = int Function();
typedef GetCurrentProcessC = ffi.Pointer Function();
typedef GetCurrentProcessDart = ffi.Pointer Function();

typedef OpenProcessTokenC = ffi.Bool Function(
  ffi.Pointer,
  ffi.Uint32,
  ffi.Pointer,
);
typedef OpenProcessTokenDart = bool Function(
  ffi.Pointer processHandle,
  int desiredAccess,
  ffi.Pointer tokenHandle,
);

typedef ShutDownC = ffi.Bool Function(
  ffi.Pointer,
  ffi.Pointer,
  ffi.Uint32,
  ffi.Bool,
  ffi.Bool,
  ffi.Uint32,
);
typedef ShutDownDart = bool Function(
  ffi.Pointer lpMachineName,
  ffi.Pointer lpMessage,
  int dwTimeout,
  bool bForceAppsClosed,
  bool bRebootAfterShutdown,
  int dwReason,
);

@Injectable(as: ShutdownAccess)
class WindowsShutdownAccess implements ShutdownAccess {
  late final GetCurrentProcessDart _getCurrentProcess;
  late final GetLastErrorDart _getLastError;

  late final OpenProcessTokenDart _openProcessToken;
  late final ShutDownDart _shutDown;

  WindowsShutdownAccess() {
    final kernel = ffi.DynamicLibrary.open('Kernel32.dll');
    _getCurrentProcess =
        kernel.lookupFunction<GetCurrentProcessC, GetCurrentProcessDart>(
      'GetCurrentProcess',
    );
    _getLastError = kernel.lookupFunction<GetLastErrorC, GetLastErrorDart>(
      'GetLastError',
    );

    final advapi = ffi.DynamicLibrary.open('Advapi32.dll');
    _openProcessToken =
        advapi.lookupFunction<OpenProcessTokenC, OpenProcessTokenDart>(
      'OpenProcessToken',
    );
    _shutDown = advapi.lookupFunction<ShutDownC, ShutDownDart>(
      'InitiateSystemShutdownExW',
    );
  }

  void ensurePrivileges() {
    final currentProcess = _getCurrentProcess();
    final token = ffi.Pointer;
  }

  @override
  Future<void> shutDown(Duration delay) async {
    final result = _shutDown(
      ffi.nullptr,
      ffi.nullptr,
      delay.inSeconds,
      false,
      false,
      0x00040000,
    );

    if (!result) {
      final errorCode = _getLastError();
      throw Exception('Could not initiate shutdown, got error code $errorCode');
    }
  }
}
