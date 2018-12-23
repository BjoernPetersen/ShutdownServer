package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConfigModule(private val path: String = "config.yml") {

    @Singleton
    @Provides
    fun provider(): ConfigProvider {
        return readConfig(path)
    }

    @Singleton
    @Provides
    fun server(config: ConfigProvider): ServerConfig = config.bind("server")

    @Singleton
    @Provides
    fun shutdown(config: ConfigProvider): ShutdownConfig = config.bind("shutdown")
}
