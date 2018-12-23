package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.loaders.EnvironmentConfigLoader
import com.jdiazcano.cfg4k.providers.CachedConfigProvider
import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.OverrideConfigProvider
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource
import com.jdiazcano.cfg4k.sources.FileConfigSource
import com.jdiazcano.cfg4k.yaml.YamlConfigLoader
import mu.KotlinLogging
import java.io.File

interface ServerConfig {
    val port: Int
    val token: String
}

interface ShutdownConfig {
    val enable: Boolean
    val time: Int
}

fun readConfig(filePath: String): ConfigProvider {
    val logger = KotlinLogging.logger {}

    val defaultsSource = ClasspathConfigSource("/config.yml")
    val defaultsLoader = YamlConfigLoader(defaultsSource)
    val defaultsProvider = DefaultConfigProvider(defaultsLoader)

    val configFile = File(filePath)
    val fileProvider = if (!configFile.isFile) {
        logger.warn { "Could not find config file '${configFile.name}'" }
        null
    } else {
        val fileSource = FileConfigSource(configFile)
        val fileLoader = YamlConfigLoader(fileSource)
        DefaultConfigProvider(fileLoader)
    }

    val envLoader = EnvironmentConfigLoader()
    val envProvider = DefaultConfigProvider(envLoader)

    val combinedProvider = if (fileProvider == null) {
        OverrideConfigProvider(envProvider, defaultsProvider)
    } else {
        OverrideConfigProvider(envProvider, fileProvider, defaultsProvider)
    }
    return CachedConfigProvider(combinedProvider)
}
