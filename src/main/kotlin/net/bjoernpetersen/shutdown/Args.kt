package net.bjoernpetersen.shutdown

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import dagger.Module
import dagger.Provides
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Singleton
import mu.KotlinLogging

class Args : CliktCommand() {
    private val logger = KotlinLogging.logger { }

    val root: Path by option(help = "Root directory for config files")
        .path(exists = true, fileOkay = false, readable = true)
        .default(Paths.get("."), "Working directory")

    override fun run() {
        logger.info { "Using root directory: $root" }
    }
}

@Module
class ArgsModule(private val rawArgs: Array<String>) {
    @Singleton
    @Provides
    fun provideArgs(): Args = Args().apply { main(rawArgs) }
}
