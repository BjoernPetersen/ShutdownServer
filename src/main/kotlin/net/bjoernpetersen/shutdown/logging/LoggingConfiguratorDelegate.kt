package net.bjoernpetersen.shutdown.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.LifeCycle
import java.io.File

internal class LoggingConfiguratorDelegate : ContextAwareBase, Configurator {

    private val consoleLevel: Level
    private val fileLevel: Level?
    private val logFile: File?

    constructor(consoleLevel: Level) : super() {
        this.consoleLevel = consoleLevel
        fileLevel = null
        logFile = null
    }

    constructor(consoleLevel: Level, fileLevel: Level, file: File) {
        this.consoleLevel = consoleLevel
        this.fileLevel = fileLevel
        this.logFile = file
    }

    override fun configure(ctx: LoggerContext) {
        val layout = TTLLLayout().configure {
            context = ctx
        }

        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        encoder.context = ctx
        encoder.layout = layout

        val console = ConsoleAppender<ILoggingEvent>().configure {
            context = ctx
            name = "console"
            this.encoder = encoder
            addFilter(ThresholdFilter().configure { setLevel(consoleLevel.levelStr) })
        }

        val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(console)

        if (fileLevel != null) {
            rootLogger.addAppender(
                FileAppender<ILoggingEvent>().configure {
                    context = ctx
                    name = "file"
                    this.encoder = encoder
                    addFilter(ThresholdFilter().configure { setLevel(fileLevel.levelStr) })
                    this.file = logFile!!.path
                }
            )
        }
    }
}

private fun <T : LifeCycle> T.configure(configure: T.() -> Unit): T = apply {
    configure()
    start()
}
