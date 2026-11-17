package su.prismaworld.hideNames.config

import org.bukkit.configuration.file.FileConfiguration

/**
 * Snapshot of `config.yml`. Missing keys fall back to the defaults bundled in the jar.
 */
data class Settings(
    val actionBarFormat: String,
    val messages: Messages,
) {

    data class Messages(
        val espEnabled: String,
        val espDisabled: String,
        val playersOnly: String,
        val reloaded: String,
    )

    companion object {
        fun load(config: FileConfiguration): Settings = Settings(
            actionBarFormat = config.string("action-bar-format"),
            messages = Messages(
                espEnabled = config.string("messages.esp-enabled"),
                espDisabled = config.string("messages.esp-disabled"),
                playersOnly = config.string("messages.players-only"),
                reloaded = config.string("messages.reloaded"),
            ),
        )

        private fun FileConfiguration.string(path: String): String = getString(path).orEmpty()
    }
}
