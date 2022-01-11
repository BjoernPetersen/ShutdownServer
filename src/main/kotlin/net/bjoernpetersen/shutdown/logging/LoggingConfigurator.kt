package net.bjoernpetersen.shutdown.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.Configurator
import java.io.File

class LoggingConfigurator : Configurator by LoggingConfiguratorDelegate(
    Level.INFO,
    Level.INFO,
    File("shutdownserver.log")
)
