package coyote.config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.Item
import com.uchuhimo.konf.source.hocon.toHocon


object CoreConfig : ConfigSpec("Coyote") {
    object Api : ConfigSpec() {
        val port by optional(3333)
        val threads by optional(2)
    }
}

private var coreConf = Config { addSpec(CoreConfig) }
        .from.json.string(ConfigFactory.load().root().render(ConfigRenderOptions.concise()))

fun addConfig(conf: Config) {
    coreConf += conf
}

fun getConfigAsHocon() = coreConf.toHocon.toText()

val <T> Item<T>.get: T
    get() = coreConf[this]
