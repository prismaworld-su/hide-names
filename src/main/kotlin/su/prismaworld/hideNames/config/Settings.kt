package su.prismaworld.hideNames.config

import org.bukkit.configuration.file.FileConfiguration

/**
 * Snapshot of `config.yml`. Missing keys fall back to the defaults bundled in the jar.
 */
data class Settings(
    val actionBarFormat: String,
) {

    companion object {
        fun load(config: FileConfiguration): Settings = Settings(
            actionBarFormat = config.string("action-bar-format"),
        )

        private fun FileConfiguration.string(path: String): String = getString(path).orEmpty()
    }
}
