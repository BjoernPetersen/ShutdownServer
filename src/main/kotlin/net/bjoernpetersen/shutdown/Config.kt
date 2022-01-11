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
import java.nio.file.Files
import java.nio.file.Path

interface ServerConfig {
    val port: Int
    val token: String
}

interface ShutdownConfig {
    val enable: Boolean
    val time: Int
}

fun readConfig(configPath: Path): ConfigProvider {
    val logger = KotlinLogging.logger {}

    val defaultsSource = ClasspathConfigSource("/config.yml")
    val defaultsLoader = YamlConfigLoader(defaultsSource)
    val defaultsProvider = DefaultConfigProvider(defaultsLoader)

    val fileProvider = if (!Files.isRegularFile(configPath)) {
        logger.warn { "Could not find config file '$configPath'" }
        null
    } else {
        val fileSource = FileConfigSource(configPath)
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
