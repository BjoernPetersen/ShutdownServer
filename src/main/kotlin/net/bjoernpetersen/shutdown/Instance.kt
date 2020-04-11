package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.providers.ConfigProvider
import dagger.Component
import javax.inject.Singleton
import net.bjoernpetersen.shutdown.api.Api
import net.bjoernpetersen.shutdown.exec.Killer
import net.bjoernpetersen.shutdown.exec.KillerModule

@Singleton
@Component(modules = [ArgsModule::class, ConfigModule::class, KillerModule::class])
interface Instance {

    val api: Api
    val config: ConfigProvider
    val killer: Killer
}
