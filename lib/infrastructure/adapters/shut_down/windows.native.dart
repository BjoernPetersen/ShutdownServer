// ignore_for_file: non_constant_identifier_names, constant_identifier_names

part of 'windows.dart';

/// ```
/// BOOL LookupPrivilegeValueW(
///   [in, optional] LPCWSTR lpSystemName,
///   [in]           LPCWSTR lpName,
///   [out]          PLUID   lpLuid
/// );
/// ```
typedef LookupPrivilegeValueC = ffi.Int Function(
  ffi.Pointer,
  ffi.Pointer<Utf16>,
  ffi.Pointer<LUID>,
);
typedef LookupPrivilegeValue = int Function(
  ffi.Pointer,
  ffi.Pointer<Utf16>,
  ffi.Pointer<LUID>,
);

typedef AdjustTokenPrivilegesC = ffi.Int Function(
  ffi.Pointer<ffi.IntPtr>,
  ffi.Bool,
  ffi.Pointer<TokenPrivileges>,
  ffi.Uint32,
  ffi.Pointer<TokenPrivileges>,
  ffi.Pointer<ffi.Uint32>,
);
typedef AdjustTokenPrivileges = int Function(
  ffi.Pointer<ffi.IntPtr> tokenHandle,
  bool disableAllPrivileges,
  ffi.Pointer<TokenPrivileges> newState,
  int bufferLength,
  ffi.Pointer<TokenPrivileges> previousState,
  ffi.Pointer<ffi.Uint32> returnLength,
);

typedef GetLastErrorC = ffi.Uint32 Function();
typedef GetLastError = int Function();

final class LuidAndAttributes extends ffi.Struct {
  external ffi.Pointer<LUID> Luid;
  @ffi.Uint32()
  external int Attributes;
}

final class TokenPrivileges extends ffi.Struct {
  @ffi.Uint32()
  external int PrivilegeCount;

  @ffi.Array(1)
  external ffi.Array<LuidAndAttributes> Privileges;
}

const SE_PRIVILEGE_ENABLED = 0x00000002;
const ERROR_SHUTDOWN_IN_PROGRESS = 1115;
const ERROR_SHUTDOWN_IS_SCHEDULED = 1190;
const ERROR_SHUTDOWN_USERS_LOGGED_ON = 1191;
