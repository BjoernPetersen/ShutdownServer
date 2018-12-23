package net.bjoernpetersen.shutdown

import com.jdiazcano.cfg4k.loaders.PropertyConfigLoader
import com.jdiazcano.cfg4k.providers.DefaultConfigProvider
import com.jdiazcano.cfg4k.providers.get
import com.jdiazcano.cfg4k.sources.ClasspathConfigSource

object Version {
    fun get(): String {
        val source = ClasspathConfigSource("/version.properties")
        val loader = PropertyConfigLoader(source)
        val provider = DefaultConfigProvider(loader)
        return provider.get("version")
    }
}
