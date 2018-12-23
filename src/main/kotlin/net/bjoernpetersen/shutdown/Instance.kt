package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import dagger.Component
import net.bjoernpetersen.shutdown.api.Api
import javax.inject.Singleton

@Singleton
@Component(modules = [ConfigModule::class], dependencies = [Killer::class])
interface Instance {

    val api: Api
    val config: ConfigProvider
    val killer: Killer
}
