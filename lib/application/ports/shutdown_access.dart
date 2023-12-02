abstract interface class ShutdownAccess {
  Future<void> shutDown(Duration delay);
}
