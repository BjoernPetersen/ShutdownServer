package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import com.jdiazcano.cfg4k.providers.bind
import dagger.Module
import dagger.Provides
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Singleton

@Module
class ConfigModule(private val path: Path = Paths.get("config.yml")) {

    @Singleton
    @Provides
    fun provider(args: Args): ConfigProvider {
        return readConfig(args.root.resolve(path))
    }

    @Singleton
    @Provides
    fun server(config: ConfigProvider): ServerConfig = config.bind("server")

    @Singleton
    @Provides
    fun shutdown(config: ConfigProvider): ShutdownConfig = config.bind("shutdown")
}
