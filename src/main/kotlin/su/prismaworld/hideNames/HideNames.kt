package su.prismaworld.hideNames

import org.bukkit.plugin.java.JavaPlugin
import su.prismaworld.hideNames.listener.PlayerConnectionListener
import su.prismaworld.hideNames.name.NameTagHider

class HideNames : JavaPlugin() {

    companion object {
        lateinit var instance: HideNames
            private set
    }

    private lateinit var nameTagHider: NameTagHider

    fun loadSettings() {
        saveDefaultConfig()
        reloadConfig()
    }

    override fun onEnable() {
        instance = this

        loadSettings()

        nameTagHider = NameTagHider().also { it.enable() }

        server.pluginManager.registerEvents(PlayerConnectionListener(nameTagHider), this)

        // Covers a reload of the plugin while players are online.
        server.onlinePlayers.forEach(nameTagHider::hide)
    }

    override fun onDisable() {
        if (::nameTagHider.isInitialized) {
            nameTagHider.disable()
        }
    }
}
