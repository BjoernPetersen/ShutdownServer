import 'dart:ffi' as ffi;

import 'package:ffi/ffi.dart';
import 'package:injectable/injectable.dart';
import 'package:shutdownserver/application/ports/shutdown_access.dart';
import 'package:shutdownserver/platform.dart';
import 'package:win32/win32.dart';

part 'windows.native.dart';

@windowsEnv
@Injectable(as: ShutdownAccess)
class WindowsShutdownAccess implements ShutdownAccess {
  late final GetLastError _getLastError;
  late final LookupPrivilegeValue _lookupPrivilegeValue;
  late final AdjustTokenPrivileges _adjustTokenPrivileges;

  WindowsShutdownAccess() {
    final advapi = ffi.DynamicLibrary.open('Advapi32.dll');
    _adjustTokenPrivileges =
        advapi.lookupFunction<AdjustTokenPrivilegesC, AdjustTokenPrivileges>(
      'AdjustTokenPrivileges',
    );
    _lookupPrivilegeValue =
        advapi.lookupFunction<LookupPrivilegeValueC, LookupPrivilegeValue>(
      'LookupPrivilegeValueW',
    );

    final kernel = ffi.DynamicLibrary.open('Kernel32.dll');
    _getLastError = kernel.lookupFunction<GetLastErrorC, GetLastError>(
      'GetLastError',
    );
  }

  void _runChecked(int Function() action) {
    final result = action();
    if (result == 0) {
      final errorCode = _getLastError();
      throw Exception(
        'Could not perform action. Got error code $errorCode',
      );
    }
  }

  void ensurePrivileges() {
    final currentProcess = GetCurrentProcess();

    final luidPointer = calloc<LUID>();
    final tokenPointer = calloc<ffi.IntPtr>();
    final tokenPrivileges = calloc<TokenPrivileges>();

    try {
      _runChecked(
        () => OpenProcessToken(
          currentProcess,
          TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY,
          tokenPointer,
        ),
      );
      _runChecked(
        () => _lookupPrivilegeValue(
          ffi.nullptr,
          'SeShutdownPrivilege'.toNativeUtf16(),
          luidPointer,
        ),
      );

      tokenPrivileges.ref.PrivilegeCount = 1;
      tokenPrivileges.ref.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
      tokenPrivileges.ref.Privileges[0].Luid = luidPointer;

      final result = _adjustTokenPrivileges(
        tokenPointer,
        false,
        tokenPrivileges,
        0,
        ffi.nullptr,
        ffi.nullptr,
      );

      if (result != ERROR_SUCCESS) {
        throw Exception('Could not adjust privileges, got code $result');
      }
    } finally {
      free(tokenPointer);
      free(tokenPrivileges);
      free(luidPointer);
    }
  }

  @override
  Future<void> shutDown(Duration delay) async {
    ensurePrivileges();

    final result = InitiateShutdown(
      ffi.nullptr,
      ffi.nullptr,
      delay.inSeconds,
      0x208,
      0x40000,
    );

    switch (result) {
      case ERROR_SUCCESS:
        return;
      case ERROR_ACCESS_DENIED:
        throw Exception('ERROR_ACCESS_DENIED');
      case ERROR_BAD_NETPATH:
        throw Exception('ERROR_BAD_NETPATH');
      case ERROR_INVALID_FUNCTION:
        throw Exception('ERROR_INVALID_FUNCTION');
      case ERROR_INVALID_PARAMETER:
        throw Exception('ERROR_INVALID_PARAMETER');
      case ERROR_SHUTDOWN_IN_PROGRESS:
        throw Exception('ERROR_SHUTDOWN_IN_PROGRESS');
      case ERROR_SHUTDOWN_IS_SCHEDULED:
        throw Exception('ERROR_SHUTDOWN_IS_SCHEDULED');
      case ERROR_SHUTDOWN_USERS_LOGGED_ON:
        throw Exception('ERROR_SHUTDOWN_USERS_LOGGED_ON');
      default:
        throw Exception('Unknown result code: $result');
    }
  }
}
