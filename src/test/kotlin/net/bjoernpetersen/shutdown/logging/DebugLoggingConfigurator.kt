package net.bjoernpetersen.shutdown.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.Configurator

class DebugLoggingConfigurator : Configurator by LoggingConfiguratorDelegate(Level.DEBUG)
