package su.prismaworld.hideNames

import org.bukkit.plugin.java.JavaPlugin
import su.prismaworld.hideNames.config.Settings
import su.prismaworld.hideNames.listener.PlayerConnectionListener
import su.prismaworld.hideNames.listener.PlayerInspectListener
import su.prismaworld.hideNames.name.NameTagHider
import su.prismaworld.hideNames.name.PlayerNameFormatter

class HideNames : JavaPlugin() {

    companion object {
        lateinit var instance: HideNames
            private set
    }

    lateinit var settings: Settings
        private set

    private lateinit var nameTagHider: NameTagHider

    fun loadSettings() {
        saveDefaultConfig()
        reloadConfig()
        settings = Settings.load(config)
    }

    override fun onEnable() {
        instance = this

        loadSettings()

        nameTagHider = NameTagHider().also { it.enable() }

        val placeholderApiEnabled = server.pluginManager.isPluginEnabled("PlaceholderAPI")
        if (!placeholderApiEnabled) {
            logger.info("PlaceholderAPI не найден: плейсхолдеры PlaceholderAPI обрабатываться не будут.")
        }
        val formatter = PlayerNameFormatter(placeholderApiEnabled)

        server.pluginManager.registerEvents(PlayerConnectionListener(nameTagHider), this)
        server.pluginManager.registerEvents(PlayerInspectListener(this, formatter), this)

        // Covers a reload of the plugin while players are online.
        server.onlinePlayers.forEach(nameTagHider::hide)
    }

    override fun onDisable() {
        if (::nameTagHider.isInitialized) {
            nameTagHider.disable()
        }
    }
}
