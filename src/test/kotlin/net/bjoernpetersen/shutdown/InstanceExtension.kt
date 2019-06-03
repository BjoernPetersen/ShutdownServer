package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import net.bjoernpetersen.shutdown.api.Api
import net.bjoernpetersen.shutdown.exec.KillerModule
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver
import java.nio.file.Paths

class InstanceExtension : ParameterResolver, BeforeEachCallback {
    private lateinit var instance: Instance

    override fun beforeEach(context: ExtensionContext?) {
        instance = DaggerInstance.builder()
            .argsModule(ArgsModule(emptyArray()))
            .configModule(ConfigModule(Paths.get("testConfig.yml")))
            .killerModule(KillerModule(TestKiller()))
            .build()
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Boolean {
        val type = parameterContext.parameter.type.kotlin
        return when (type) {
            Instance::class,
            Api::class,
            ConfigProvider::class,
            ServerConfig::class,
            ShutdownConfig::class -> true
            else -> false
        }
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Any {
        return when (parameterContext.parameter.type.kotlin) {
            Instance::class -> instance
            Api::class -> instance.api
            ConfigProvider::class -> instance.config
            ServerConfig::class -> instance.config.bind<ServerConfig>("server")
            ShutdownConfig::class -> instance.config.bind<ShutdownConfig>("shutdown")
            else -> throw ParameterResolutionException("Unsupported parameter type")
        }
    }
}
