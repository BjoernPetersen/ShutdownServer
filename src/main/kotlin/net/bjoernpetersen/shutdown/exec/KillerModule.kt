package net.bjoernpetersen.shutdown.exec

import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton
import mu.KotlinLogging

@Module
class KillerModule(val override: Killer? = null) {

    private val logger = KotlinLogging.logger { }

    @Singleton
    @Provides
    fun killer(): Killer {
        if (override != null) return override
        val osName = System.getProperty("os.name")
        val base = when {
            osName.startsWith("win", ignoreCase = true) -> WinKiller()
            else -> LinuxKiller()
        }

        val customFile = File("shutdown.yml")
        return if (customFile.isFile) {
            logger.debug { "Loading custom shutdown file..." }
            customKiller(customFile, base)
        } else {
            logger.debug { "No custom shutdown file found." }
            base
        }
    }
}
